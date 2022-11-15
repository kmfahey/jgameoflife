package com.kmfahey.jgameoflife.altthreadedimpl;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CyclicBarrier;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * This class implements a portion of the composite cell grid maintained by
 * the CellGrid object. It stores instance variables that enable it to report
 * where in the composite cell grid its region is located, and it stores all
 * eight of its neighboring CellGridSection objects so that it can complete
 * calculations of the sum of neighboring cells for cells located on the edge or
 * in the corner of the region of the cells grid that it maintains.
 *
 * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid
 * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridDispatch
 */
public class CellGridSection implements Runnable {

    /** This int constant is a signal value used to signify the compass
        direction north. */
    public static final int NORTH = 0;

    /** This int constant is a signal value used to signify the compass
        direction northeast. */
    public static final int NORTHEAST = 1;

    /** This int constant is a signal value used to signify the compass
        direction east. */
    public static final int EAST = 2;

    /** This int constant is a signal value used to signify the compass
        direction southeast. */
    public static final int SOUTHEAST = 3;

    /** This int constant is a signal value used to signify the compass
        direction south. */
    public static final int SOUTH = 4;

    /** This int constant is a signal value used to signify the compass
        direction southwest. */
    public static final int SOUTHWEST = 5;

    /** This int constant is a signal value used to signify the compass
        direction west. */
    public static final int WEST = 6;

    /** This int constant is a signal value used to signify the compass
        direction northwest. */
    public static final int NORTHWEST = 7;

    /** This int constant is a signal value used to signify that a distributed
        processing task is complete. */
    public static final int FINISHED = 0;

    /** This int constant is a signal value used to signify a distributed
        processing task that calls clearCellGrid(). */
    public static final int MODE_CLEAR = 1;

    /** This int constant is a signal value used to signify a distributed
        processing task that calls seedCellGrid(). */
    public static final int MODE_SEED = 2;

    /** This int constant is a signal value used to signify a distributed
        processing task that calls algorithmUpdateStep(). */
    public static final int MODE_UPDATE = 3;

    /** This int constant is a signal value used to signify a distributed
        processing task that calls algorithmDisplayStep(). */
    public static final int MODE_DISPLAY = 4;

    /** This ArrayBlockingQueue&lt;Integer&gt; object holds the 1-capacity queue
        used to pass signal values back and forth between this object running
        in a worker thread and the CellGridDispatch object running in the main
        thread. */
    public volatile ArrayBlockingQueue<Integer> modeFlagQueue;

    /** This CellGridSection object is the one that neighbors this object
        to the north.
        @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid */
    private CellGridSection northNeighbor;

    /** This CellGridSection object is the one that neighbors this object
        to the northeast.
        @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid */
    private CellGridSection northEastNeighbor;

    /** This CellGridSection object is the one that neighbors this object
        to the east.
        @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid */
    private CellGridSection eastNeighbor;

    /** This CellGridSection object is the one that neighbors this object
        to the southeast.
        @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid */
    private CellGridSection southEastNeighbor;

    /** This CellGridSection object is the one that neighbors this object
        to the south.
        @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid */
    private CellGridSection southNeighbor;

    /** This CellGridSection object is the one that neighbors this object
        to the southwest.
        @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid */
    private CellGridSection southWestNeighbor;

    /** This CellGridSection object is the one that neighbors this object
        to the west.
        @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid */
    private CellGridSection westNeighbor;

    /** This CellGridSection object is the one that neighbors this object
        to the northwest.
        @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid */
    private CellGridSection northWestNeighbor;

    /** This Object is the monitor object this object running in a worker thread
        wait()s on, and the main thread notifyAll()s on. This object does not
        notify() on it.
        @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridDispatch */
    private Object mainToThreadsMonitor;

    /** This Object is the monitor object used by this object running in a
        worker thread to notify() the main thread. This object does not wait()
        on it.
        @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridDispatch */
    private Object threadsToMainMonitor;

    /** This int[][] array is the alternate hidden cell grid used in
        algorithmUpdateStep() and algorithmDisplayStep() to temporarily hold the
        computed values of the next generation of the cellular automata before
        they're copied back into the main int[][] displayCells array. */
    private volatile int[][] updateCells;

    /** This int[][] array is the cell grid used to store the 0 and 1 values
        that represent 'dead' and 'live' cells, and is the array consulted by
        CellGrid when it's rendering the cells grid to the viewable area of the
        GUI.
        @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid */
    private volatile int[][] displayCells;

    /** This int is the number of cells in the vertical dimension of the
        displayCells array. */
    private int horizDim;

    /** This int is the number of cells in the vertical dimension of the
        displayCells array. */
    private int vertDim;

    /** This int is the maximum value of the horizontal dimension of the
        displayCells array. */
    private int maxHoriz;

    /** This int is the maximum value of the vertical dimension of the
        displayCells array. */
    private int maxVert;

    /** This int is the horizontal coordinate of the upper left corner of
        this object's cells grid in the composite cells grid it is a part of. */
    private int originHorizCoord;

    /** This int is the vertical coordinate of the upper left corner of
        this object's cells grid in the composite cells grid it is a part of. */
    private int originVertCoord;

    /**
     * This method initializes the CellGridSection object, setting instance
     * variables.
     *
     * @param cellsWidth          The width of the portion of the cells grid
     *                            that's been delegated to this object, in
     *                            cells.
     * @param cellsHeight         The height of the portion of the cells grid
     *                            that's been delegated to this object, in
     *                            cells.
     * @param originHorizCoordVal The horizontal coordinate of the upper left
     *                            corner of this object's cells grid in the
     *                            composite cells grid it is a part of.
     * @param originVertCoordVal  The vertical coordinate of the upper left
     *                            corner of this object's cells grid in the
     *                            composite cells grid it is a part of.
     * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid
     */
    public CellGridSection(final int cellsWidth, final int cellsHeight,
                           final int originHorizCoordVal, final int originVertCoordVal) {
        horizDim = cellsWidth;
        vertDim = cellsHeight;
        maxHoriz = horizDim - 1;
        maxVert = vertDim - 1;
        originHorizCoord = originHorizCoordVal;
        originVertCoord = originVertCoordVal;
        displayCells = new int[horizDim][vertDim];
        updateCells = new int[horizDim][vertDim];
    }

    /**
     * This method is used by CellGridDispatch to set the modeFlagQueue instance
     * variable. This 1-capacity queue is used by CellGridDispatch to convey one
     * of the constants MODE_CLEAR, MODE_SEED, MODE_UPDATE, or MODE_DISPLAY.
     * The run() method removes that value from the queue to signal the work
     * has begun, and replaces it with the constant FINISHED when the task is
     * complete. The CellGridDispatch object in the main thread removes that
     * value while it is wrapping up the execution of the task,
     *
     * @param modeFlagQueueVar The 1-capacity queue used to communicate mode
     *                         flags back and forth between this object's run()
     *                         method in a worker thread and CellGridDispatch
     *                         running in the main thread.
     * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridDispatch
     */
    public void setModeFlagQueue(final ArrayBlockingQueue<Integer> modeFlagQueueVar) {
        modeFlagQueue = modeFlagQueueVar;
    }

    /**
     * This method is used to set the two monitor objects that are used to
     * signal and be signalled by the CellGridDispatch object running in the
     * main thread.
     *
     * @param mainToThreadsMonitorObj The monitor object that CellGridDispatch
     *                                in the main thread calls notifyAll() on
     *                                and this object in a worker thread calls
     *                                wait() on.
     * @param threadsToMainMonitorObj The monitor object that CellGridDispatch
     *                                in the main thread calls wait() on and
     *                                this object in a worker thread calls
     *                                notify() on.
     * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridDispatch
     */
    public void setMonitors(final Object mainToThreadsMonitorObj, final Object threadsToMainMonitorObj) {
        mainToThreadsMonitor = mainToThreadsMonitorObj;
        threadsToMainMonitor = threadsToMainMonitorObj;
    }

    /**
     * This method is used to set one of the 8 {compassDirection}Neighbor
     * instance variables to another CellGridSection object. When used for
     * all 8 compass directions, this enables this CellGridSection object to
     * keep track of its neighbors so it can complete the cellular automata"s
     * neighbors calculation when the center cell lies on an edge or in a corner
     * of this object's portion of the composite cells grid.
     *
     * @param neighborGrid The neighboring CellGridSection object.
     * @param dirFlag      One of the constants NORTH, NORTHEAST, EAST,
     *                     SOUTHEAST, SOUTH, SOUTHWEST, WEST, or NORTHWEST.
     * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridDispatch
     */
    public void setNeighbor(final CellGridSection neighborGrid, final int dirFlag) {
        switch (dirFlag) {
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

    /**
     * This method implements the run() method called for by the Runnable
     * interface, and is the method that's called by the Thread object that's
     * instantiated around this CellGridSection object by CellGridDispatch. In
     * order to keep the thread persistent during the indefinite period of the
     * automata, it contains an infinite loop and never returns. During the
     * execution, it uses a pair of monitor objects to start and stop execution,
     * and passes signal int values back and forth with the main thread run from
     * CellGridDispatch using an ArrayBlockingQueue&lt;Integer&gt;.
     */
    public void run() {
        while (true) {
            int runMode = -1;
            if (modeFlagQueue.size() > 0) {
                synchronized (modeFlagQueue) {
                    while (modeFlagQueue.size() > 0) {
                        while (runMode == -1) {
                            try {
                                runMode = modeFlagQueue.take();
                            } catch (InterruptedException exception) {
                                continue;
                            }
                        }
                        switch (runMode) {
                            case MODE_CLEAR:
                                clearCellGrid();
                                break;
                            case MODE_SEED:
                                seedCellGrid();
                                break;
                            case MODE_UPDATE:
                                algorithmUpdateStep();
                                break;
                            case MODE_DISPLAY:
                                algorithmDisplayStep();
                                break;
                        }
                    }
                }
                while (modeFlagQueue.size() == 0) {
                    try {
                        modeFlagQueue.put(FINISHED);
                    } catch (InterruptedException exception) {
                        continue;
                    }
                }
                synchronized (threadsToMainMonitor) {
                    threadsToMainMonitor.notify();
                }
            }
            try {
                synchronized (mainToThreadsMonitor) {
                    mainToThreadsMonitor.wait();
                }
            } catch (InterruptedException exception) {
                assert true;
            }
        }
    }

    /**
     * This method clears the displayCells array by setting every value in it to
     * 0.
     */
    public void clearCellGrid() {
        for (int horizIndex = 0; horizIndex < horizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < vertDim; vertIndex++) {
                displayCells[horizIndex][vertIndex] = 0;
            }
        }
    }

    /**
     * This method populates the displayCells array with randomly chosen 1
     * values in order to spontaneously create cellular automata before or
     * during the automata execution. Roughly 1 in 8 cells are set to 1,
     * regardless of their previous setting.
     */
    public void seedCellGrid() {
        /* ThreadLocalRandom is used to provide a stream of random ints that
           are used to determine whether a cell should be set to 1 or not. */
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Iterator<Integer> randomInts = rng.ints((long) horizDim * (long) vertDim).iterator();
        for (int horizIndex = 0; horizIndex < horizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < vertDim; vertIndex++) {
                /* The int that is returned is in the range [0,
                   Integer.MAX_VALUE]. That value is divided by
                   Integer.MAX_VALUE and then multiplied by 8 to rescale it to
                   (practically) [0,7]. The cell is set to 1 if the value equals
                   0, a 1-in-8 chance.

                   This statement uses Iterator.next() without checking
                   hasNext(). But the length of the stream is set to
                   cellGridHorizDim * cellGridVertDim, which is exactly the
                   number of cells in the grid, so the iterator lasts exactly
                   the number of iterations in the combined loops. */
                if ((int) Math.floor((double) randomInts.next() / (double) Integer.MAX_VALUE * 8) == 0) {
                    displayCells[horizIndex][vertIndex] = 1;
                }
            }
        }
    }

    /**
     * This method is an accessor for the private horizDim variable.
     *
     * @return The int maxHoriz, the maximum value of the horizontal dimension
     *         of the displayCells array.
     */
    public int getHorizDim() {
        return horizDim;
    }

    /**
     * This method is an accessor for the private originHorizCoord variable.
     *
     * @return The int maxHoriz, the maximum value of the horizontal dimension
     *         of the displayCells array.
     */
    public int getOriginHorizCoord() {
        return originHorizCoord;
    }

    /**
     * This method is an accessor for the private originVertCoord variable.
     *
     * @return The int maxHoriz, the maximum value of the horizontal dimension
     *         of the displayCells array.
     */
    public int getOriginVertCoord() {
        return originVertCoord;
    }

    /**
     * This method is an accessor for the private vertDim variable.
     *
     * @return The int maxHoriz, the maximum value of the horizontal dimension
     *         of the displayCells array.
     */
    public int getVertDim() {
        return vertDim;
    }

    /**
     * This method is an accessor for the private maxHoriz variable.
     *
     * @return The int maxHoriz, the maximum value of the horizontal dimension
     *         of the displayCells array.
     */
    public int getMaxHoriz() {
        return maxHoriz;
    }

    /**
     * This method is an accessor for the private maxVert variable.
     *
     * @return The int maxVert, the maximum value of the vertical dimension of
     *         the displayCells array.
     */
    public int getMaxVert() {
        return maxVert;
    }

    /**
     * This method is an accessor for the private displayCells variable.
     *
     * @return The int[][] displayCells comprising the region of the composite
     *         cells grid that this object maintains.
     */
    public int[][] getDisplayCells() {
        return displayCells;
    }

    /**
     * This method completes the first step of the 2-step algorithm execution
     * process, computing the updateCells cell grid array's value from the
     * neighbors of each cell in the displayCells cell grid array.
     */
    public void algorithmUpdateStep() {
        /* This int[][] array of length-2 arrays stores the eight sets of
           increments & decrements used to adjust the (horizIndex, vertIndex)
           values to one of eight (moddedHorizIndex, moddedVertIndex) values
           that can be used to locate a neighbor of the value at (horizIndex,
           vertIndex) while computing its sumOfNeighbors value. This is done
           in preference to using 2 further nested for loops in order to avoid
           having to skip the (0,0) case. */
        final int[][] deltaPairs = new int[][] {new int[] {-1, -1}, new int[] {-1, 0},
                                                new int[] {-1, +1}, new int[] {0, -1},
                                                new int[] {0, +1}, new int[] {+1, -1},
                                                new int[] {+1, 0}, new int[] {+1, +1}};

        /* This big conditional chain handles all 8 cases where the neigbhoring
           cells we're looking for are off the edge of this object's cellGrid
           and are on the edge or in the corner of a neighboring CellGridSection
           object's displayCells array. It tests for the modded indexes
           equalling -1 or the dimension of the array (which is 1 above its max
           index on that dimension). */
        for (int horizIndex = 0; horizIndex < horizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < vertDim; vertIndex++) {
                int sumOfNeighbors = 0;
                for (int[] deltaPair : deltaPairs) {
                    int moddedHorizIndex = horizIndex + deltaPair[0];
                    int moddedVertIndex = vertIndex + deltaPair[1];

                    /* For each of 8 possible neigbhring cellGrids, the correct
                       indexes in *that* displayCells array are computed and
                       used to access the appropriate neighboring cell to add
                       its value to sumOfNeighbors. */
                    if (moddedHorizIndex == -1 && moddedVertIndex == -1) {
                        synchronized (northWestNeighbor.getDisplayCells()) {
                            sumOfNeighbors += northWestNeighbor.getDisplayCells()[northWestNeighbor.getMaxHoriz()][northWestNeighbor.getMaxVert()];
                        }
                    } else if (moddedHorizIndex == -1 && moddedVertIndex == vertDim) {
                        synchronized (southWestNeighbor.getDisplayCells()) {
                            sumOfNeighbors += southWestNeighbor.getDisplayCells()[southWestNeighbor.getMaxHoriz()][0];
                        }
                    } else if (moddedHorizIndex == -1) { /* -1 < moddedVertIndex < vertDim */
                        synchronized (westNeighbor.getDisplayCells()) {
                            sumOfNeighbors += westNeighbor.getDisplayCells()[westNeighbor.getMaxHoriz()][moddedVertIndex];
                        }
                    } else if (moddedHorizIndex == horizDim && moddedVertIndex == -1) {
                        synchronized (northEastNeighbor.getDisplayCells()) {
                            sumOfNeighbors += northEastNeighbor.getDisplayCells()[0][northEastNeighbor.getMaxVert()];
                        }
                    } else if (moddedHorizIndex == horizDim && moddedVertIndex == vertDim) {
                        synchronized (southEastNeighbor.getDisplayCells()) {
                            sumOfNeighbors += southEastNeighbor.getDisplayCells()[0][0];
                        }
                    } else if (moddedHorizIndex == horizDim) { /* -1 < moddedVertIndex < vertDim */
                        synchronized (eastNeighbor.getDisplayCells()) {
                            sumOfNeighbors += eastNeighbor.getDisplayCells()[0][moddedVertIndex];
                        }
                    } else if (moddedVertIndex == -1) { /* -1 < moddedHorizIndex < horizDim && */
                        synchronized (northNeighbor.getDisplayCells()) {
                            sumOfNeighbors += northNeighbor.getDisplayCells()[moddedHorizIndex][northNeighbor.getMaxVert()];
                        }
                    } else if (moddedVertIndex == vertDim) { /* -1 < moddedHorizIndex < horizDim && */
                        synchronized (southNeighbor.getDisplayCells()) {
                            sumOfNeighbors += southNeighbor.getDisplayCells()[moddedHorizIndex][0];
                        }
                    } else { /* -1 < moddedHorizIndex < horizDim && -1 < moddedVertIndex < vertDim */
                        synchronized (displayCells) {
                            sumOfNeighbors += displayCells[moddedHorizIndex][moddedVertIndex];
                        }
                    }
                }

                /* With sumOfNeighbors computed, the actual algorithm decision
                   is carried out. If the number of live cells neighboring this
                   cell is less than 2 or more than 3, the cell is set to 0. If
                   it equals 3, the cell is set to 1. If it equals 2, the cell
                   remains set to whatever value it already has (a no-op, so
                   this if/else cascade doesn't address that possibility. */
                if (sumOfNeighbors < 2) {
                    updateCells[horizIndex][vertIndex] = 0;
                } else if (sumOfNeighbors == 3) {
                    updateCells[horizIndex][vertIndex] = 1;
                } else if (sumOfNeighbors >= 4) {
                    updateCells[horizIndex][vertIndex] = 0;
                }
            }
        }
    }

    /**
     * This method completes the two-step algorithm by copying every cell from
     * the buffer array updateCells into the display array displayCells.
     */
    public void algorithmDisplayStep() {
        /* This nested for loop iterates across the 2d displayCells array and
           copies every value in from the equivalent value in the updateCells
           array. */
        for (int horizIndex = 0; horizIndex < horizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < vertDim; vertIndex++) {
                displayCells[horizIndex][vertIndex] = updateCells[horizIndex][vertIndex];
            }
        }
    }
}
