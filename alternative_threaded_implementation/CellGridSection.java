package jgameoflife;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CyclicBarrier;
import java.util.Iterator;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class CellGridSection implements Runnable {
    public static final int NORTH = 0;
    public static final int NORTHEAST = 1;
    public static final int EAST = 2;
    public static final int SOUTHEAST = 3;
    public static final int SOUTH = 4;
    public static final int SOUTHWEST = 5;
    public static final int WEST = 6;
    public static final int NORTHWEST = 7;

    public static final int FINISHED = 0;
    public static final int MODE_CLEAR = 1;
    public static final int MODE_SEED = 2;
    public static final int MODE_UPDATE = 3;
    public static final int MODE_DISPLAY = 4;

    public int horizDim;
    public int vertDim;
    public int maxHoriz;
    public int maxVert;
    public int originHoriz;
    public int originVert;
    public volatile int[][] displayCells;
    private int[][] updateCells;
    private int[][] bufferCells;
    CellGridSection northNeighbor;
    CellGridSection northEastNeighbor;
    CellGridSection eastNeighbor;
    CellGridSection southWestNeighbor;
    CellGridSection southNeighbor;
    CellGridSection southEastNeighbor;
    CellGridSection westNeighbor;
    CellGridSection northWestNeighbor;

    private CellGridDispatch cellGridDispatch;
    public int originHorizCoord;
    public int originVertCoord;
    private String threadName;
    private Thread mainThread;
    private Object mainToThreadsMonitor;
    private Object threadsToMainMonitor;
    private volatile ArrayBlockingQueue<Integer> modeFlagQueue;
    private volatile ReentrantLock suspendLock;
    private int algorithmStepCounter = 0;

    public CellGridSection(int cellsWidth, int cellsHeight, int originHorizCoordVal, int originVertCoordVal) {
        horizDim = cellsWidth;
        vertDim = cellsHeight;
        maxHoriz = horizDim - 1;
        maxVert = vertDim - 1;
        originHorizCoord = originHorizCoordVal;
        originVertCoord = originVertCoordVal;
        bufferCells = new int[horizDim][vertDim]
        displayCells = new int[horizDim][vertDim];
        updateCells = new int[horizDim][vertDim];
        for (int horizIndex = 0; horizIndex < horizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < vertDim; vertIndex++) {
                displayCells[horizIndex][vertIndex] = 0;
            }
        }
    }

    public void setModeFlagQueue(ArrayBlockingQueue<Integer> modeFlagQueueVar) {
        modeFlagQueue = modeFlagQueueVar;
    }

    public void setThreadName(String threadNameStr) {
        threadName = threadNameStr;
    }

    public void setMonitors(Object mainToThreadsMonitorObj, Object threadsToMainMonitorObj) {
        mainToThreadsMonitor = mainToThreadsMonitorObj;
        threadsToMainMonitor = threadsToMainMonitorObj;
    }

    public void setDispatch(CellGridDispatch dispatchObj) {
        cellGridDispatch = dispatchObj;
    }

    public void setLock(ReentrantLock suspendLockVal) {
        suspendLock = suspendLockVal;
    }

    public void setNeighbor(CellGridSection neighborGrid, int direction) {
        switch (direction) {
            case NORTH:
                northNeighbor = neighborGrid;
                break;
            case NORTHEAST:
                northEastNeighbor = neighborGrid;
                break;
            case EAST:
                eastNeighbor = neighborGrid;
                break;
            case SOUTHEAST:
                southEastNeighbor = neighborGrid;
                break;
            case SOUTH:
                southNeighbor = neighborGrid;
                break;
            case SOUTHWEST:
                southWestNeighbor = neighborGrid;
                break;
            case WEST:
                westNeighbor = neighborGrid;
                break;
            case NORTHWEST:
                northWestNeighbor = neighborGrid;
                break;
        }
    }

    public void run() {
//        CellGrid.statusLineWIsoTime(threadName, "running");
        while (true) {
            int runMode = -1;
//            CellGrid.statusLineWIsoTime(threadName, "repeating while loop");
            if (modeFlagQueue.size() > 0) {
                synchronized (modeFlagQueue) {
                    while (modeFlagQueue.size() > 0) {
                        while (runMode == -1) {
                            try {
                                runMode = modeFlagQueue.take();
                            } catch (InterruptedException exception) {
//                                CellGrid.statusLineWIsoTime(threadName, exception.getMessage());
                                continue;
                            }
                        }
                        switch (runMode) {
                            case MODE_CLEAR:
//                                CellGrid.statusLineWIsoTime(threadName, "got MODE_CLEAR");
                                clearCellGrid();
                                break;
                            case MODE_SEED:
//                                CellGrid.statusLineWIsoTime(threadName, "got MODE_SEED");
                                seedCellGrid();
                                break;
                            case MODE_UPDATE:
//                                CellGrid.statusLineWIsoTime(threadName, "got MODE_UPDATE");
                                algorithmUpdateStep();
                                break;
                            case MODE_DISPLAY:
//                                CellGrid.statusLineWIsoTime(threadName, "got MODE_DISPLAY");
                                algorithmDisplayStep();
                                break;
                        }
                    }
                }
//                CellGrid.statusLineWIsoTime(threadName, "queueing FINISHED flag");
                while (modeFlagQueue.size() == 0) {
                    try {
                        modeFlagQueue.put(FINISHED);
                    } catch (InterruptedException exception) {
                        continue;
                    }
                }
//                CellGrid.statusLineWIsoTime(threadName, "notifying main thread");
                synchronized (threadsToMainMonitor) {
                    threadsToMainMonitor.notify();
                }
            }
            try {
//                CellGrid.statusLineWIsoTime(threadName, "waiting on main thread");
                synchronized (mainToThreadsMonitor) {
                    mainToThreadsMonitor.wait();
                }
            } catch (InterruptedException exception) {
//                CellGrid.statusLineWIsoTime(threadName, "wait() interrupted");
            }

        }
    }

    public void clearCellGrid() {
        for (int horizIndex = 0; horizIndex < horizDim; horizIndex++)
            for (int vertIndex = 0; vertIndex < vertDim; vertIndex++)
                displayCells[horizIndex][vertIndex] = 0;
    }

    public void seedCellGrid() {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Iterator<Integer> randomInts = rng.ints((long) horizDim * (long) vertDim).iterator();
//        CellGrid.statusLineWIsoTime(threadName, "iterating across horizDim " + horizDim + " and vertDim " + vertDim);
        for (int horizIndex = 0; horizIndex < horizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < vertDim; vertIndex++) {
                if ((int) Math.floor((double) randomInts.next() / (double) Integer.MAX_VALUE * 8) == 0) {
//                    CellGrid.statusLineWIsoTime(threadName, "setting (" + (originHorizCoord + horizIndex) + ", " + (originVertCoord + vertIndex) + ") to 1");
                    displayCells[horizIndex][vertIndex] = 1;
                }
            }
        }
    }

    public void algorithmUpdateStep() {
        final int[][] deltaPairs = new int[][] { new int[] {-1, -1}, new int[] {-1, 0}, new int[] {-1, +1},
                                                 new int[] {0, -1},                     new int[] {0, +1},
                                                 new int[] {+1, -1}, new int[] {+1, 0}, new int[] {+1, +1} };
        for (int horizIndex = 0; horizIndex < horizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < vertDim; vertIndex++) {
                int sumOfNeighbors = 0;
                for (int[] deltaPair : deltaPairs) {
                    int moddedHorizIndex = horizIndex + deltaPair[0];
                    int moddedVertIndex = vertIndex + deltaPair[1];
                    /* This big conditional chain handles all 8 cases where
                     * the neigbhoring displayCells we're looking for are off
                     * the edge of this object's cellGrid and are on the
                     * edge of a neighboring cellGrid's displayCells array. It
                     * tests for the modded indexes equalling -1 or the
                     * dimension of the array (which is 1 above its max
                     * index on that dimension). For each of 8 possible
                     * neigbhring cellGrids, the correct indexes in *that*
                     * displayCells array are computed and used to access the
                     * appropriate neighboring cell to add its value to
                     * sumOfNeighbors.
                     */
void nextgen(unsigned char u0[][W], unsigned char u1[][W+1]) {

    for (int horizIndex = 0; horizIndex < cellsWidth; horizIndex++) {
        bufferCells[horizIndex][0] = 0;
    }

    for (int i=0; i<=H; i++)
        u1[i][0] = 0 ;
    for (int j=0; j<=W; j++)
        u1[0][j] = 0 ;
    for (int i=0; i<H; i++)
        for (int j=0; j<W; j++)
            u1[i+1][j+1] = u0[i][j] + u1[i][j+1] + u1[i+1][j] - u1[i][j] ;
    for (int i=1; i+1<H; i++)
        for (int j=1; j+1<W; j++) {
            unsigned char n = u1[i+2][j+2] - u1[i+2][j-1] - u1[i-1][j+2] + u1[i-1][j-1] ;
            u0[i][j] = (n == 3 || (n == 4 && u0[i][j]))
        }
}













                    if (moddedHorizIndex == -1 && moddedVertIndex == -1) {
                        synchronized (northWestNeighbor.displayCells) {
                            sumOfNeighbors += northWestNeighbor.displayCells[northWestNeighbor.maxHoriz][northWestNeighbor.maxVert];
                        }
                    } else if (moddedHorizIndex == -1 && moddedVertIndex == vertDim) {
                        synchronized (southWestNeighbor.displayCells) {
                            sumOfNeighbors += southWestNeighbor.displayCells[southWestNeighbor.maxHoriz][0];
                        }
                    } else if (moddedHorizIndex == -1) { /* -1 < moddedVertIndex < vertDim */
                        synchronized (westNeighbor.displayCells) {
                            sumOfNeighbors += westNeighbor.displayCells[westNeighbor.maxHoriz][moddedVertIndex];
                        }
                    } else if (moddedHorizIndex == horizDim &&  moddedVertIndex == -1) {
                        synchronized (northEastNeighbor.displayCells) {
                            sumOfNeighbors += northEastNeighbor.displayCells[0][northEastNeighbor.maxVert];
                        }
                    } else if (moddedHorizIndex == horizDim &&  moddedVertIndex == vertDim) {
                        synchronized (southEastNeighbor.displayCells) {
                            sumOfNeighbors += southEastNeighbor.displayCells[0][0];
                        }
                    } else if (moddedHorizIndex == horizDim) { /* -1 < moddedVertIndex < vertDim */
                        synchronized (eastNeighbor.displayCells) {
                            sumOfNeighbors += eastNeighbor.displayCells[0][moddedVertIndex];
                        }
                    } else if (moddedVertIndex == -1) { /* -1 < moddedHorizIndex < horizDim && */
                        synchronized (northNeighbor.displayCells) {
                            sumOfNeighbors += northNeighbor.displayCells[moddedHorizIndex][northNeighbor.maxVert];
                        }
                    } else if (moddedVertIndex == vertDim) { /* -1 < moddedHorizIndex < horizDim && */ 
                        synchronized (southNeighbor.displayCells) {
                            sumOfNeighbors += southNeighbor.displayCells[moddedHorizIndex][0];
                        }
                    } else { /* -1 < moddedHorizIndex < horizDim && -1 < moddedVertIndex < vertDim */
                        sumOfNeighbors += displayCells[moddedHorizIndex][moddedVertIndex];
                    }
                }
                /* This variant implementation of the algorithm is documented at
                 * <https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life#Algorithms>,
                 * paragraph 3. It simplifies the conditionals by including the
                 * central cell in the math and testing for only two conditions:
                 * 
                 * "[I]f the sum of all nine fields in a given neighbourhood is
                 * three, the inner field state for the next generation will be
                 * life; if the all-field sum is four, the inner field retains
                 * its current state; and every other sum sets the inner field
                 * to death."
                 */
                updateCells[horizIndex][vertIndex] = 1;
                switch (sumOfNeighbors) {
                    case 2:
                        break;
                    case 3:
                        updateCells[horizIndex][vertIndex] = 1;
                        break;
                    default:
                        updateCells[horizIndex][vertIndex] = 0;
                        break;
                }
            }
        }
        algorithmStepCounter++;
    }

    public void algorithmDisplayStep() {
        for (int horizIndex = 0; horizIndex < horizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < vertDim; vertIndex++) {
                displayCells[horizIndex][vertIndex] = updateCells[horizIndex][vertIndex];
            }
        }
    }
}
