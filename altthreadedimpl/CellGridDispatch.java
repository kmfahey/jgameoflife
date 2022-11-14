package com.kmfahey.jgameoflife.altthreadedimpl;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.lang.Thread;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Objects;

public class CellGridDispatch {
    CellGridSection[] cellGridSections;
    int threadsCheckedIn;
    Thread[] threadArray;
    private final Object mainToThreadsMonitor = new Object();
    private final Object threadsToMainMonitor = new Object();
    private String threadName = Thread.currentThread().getName();
    private volatile ArrayList<ArrayBlockingQueue<Integer>> modeFlagQueues;
    private volatile ArrayList<ReentrantLock> suspendLocks;

    public CellGridDispatch(CellGridSection[][] sectionObjs) {
        int cellGridSectionsIndex = 0;
        cellGridSections = new CellGridSection[sectionObjs.length * sectionObjs[0].length];
        threadsCheckedIn = 0;
        suspendLocks = new ArrayList<ReentrantLock>();
        threadArray = new Thread[cellGridSections.length];
        modeFlagQueues = new ArrayList<ArrayBlockingQueue<Integer>>();
        for (int index = 0; index < cellGridSections.length; index++) {
            modeFlagQueues.add(new ArrayBlockingQueue<Integer>(1));
        }
        for (int horizIndex = 0; horizIndex < sectionObjs.length; horizIndex++) {
            for (int vertIndex = 0; vertIndex < sectionObjs[0].length; vertIndex++) {
                cellGridSections[cellGridSectionsIndex] = sectionObjs[horizIndex][vertIndex];
                cellGridSectionsIndex++;
            }
        }
        for (int index = 0; index < cellGridSections.length; index++) {
            cellGridSections[index].setDispatch(this);
            cellGridSections[index].setMonitors(mainToThreadsMonitor, threadsToMainMonitor);
            cellGridSections[index].setModeFlagQueue(modeFlagQueues.get(index));
        }
        for (int index = 0; index < threadArray.length; index++) {
            cellGridSections[index].setThreadName(String.valueOf(index));
            threadArray[index] = new Thread(cellGridSections[index]);
            threadArray[index].setName(String.valueOf(index));
            threadArray[index].start();
        }
    }

    public void seedSections() {
//        CellGrid.statusLineWIsoTime(threadName, "delegating seedSections() call to worker threads.");
        delegateRunMode(CellGridSection.MODE_SEED);
//        CellGrid.statusLineWIsoTime(threadName, "seedSections() call complete.");
    }

    public void clearSections() {
//        CellGrid.statusLineWIsoTime(threadName, "delegating clearSections() call to worker threads.");
        delegateRunMode(CellGridSection.MODE_CLEAR);
//        CellGrid.statusLineWIsoTime(threadName, "clearSections() call complete.");
    }

    public void sectionsRunAlgorithm() {
//        CellGrid.statusLineWIsoTime(threadName, "delegating sectionsRunAlgorithm() call to worker threads.");
        delegateRunMode(CellGridSection.MODE_UPDATE);
        delegateRunMode(CellGridSection.MODE_DISPLAY);
//        CellGrid.statusLineWIsoTime(threadName, "sectionsRunAlgorithm() call complete.");
    }

    public void delegateRunMode(int runMode) {
        switch (runMode) {
            case CellGridSection.MODE_CLEAR:
//                CellGrid.statusLineWIsoTime(threadName, "running MODE_CLEAR");
                break;
            case CellGridSection.MODE_SEED:
//                CellGrid.statusLineWIsoTime(threadName, "running MODE_SEED");
                break;
            case CellGridSection.MODE_UPDATE:
//                CellGrid.statusLineWIsoTime(threadName, "running MODE_UPDATE");
                break;
            case CellGridSection.MODE_DISPLAY:
//                CellGrid.statusLineWIsoTime(threadName, "running MODE_DISPLAY");
                break;
        }
        synchronized (modeFlagQueues) {
//            CellGrid.statusLineWIsoTime(threadName, "loading threads' queues with mode flag");
            while (modeFlagQueues.stream().filter(abq -> !Objects.equals(abq.peek(), runMode)).findFirst().isPresent()) {
                for (int index = 0; index < modeFlagQueues.size(); index++) {
                    try {
                        if (Objects.isNull(modeFlagQueues.get(index).peek())) {
                            modeFlagQueues.get(index).put(runMode);
                        }
                    } catch (InterruptedException exception) {
//                        CellGrid.statusLineWIsoTime(threadName, exception.getMessage());
                        continue;
                    }
                }
            }
        }
        while (modeFlagQueues.stream().filter(abq -> Objects.equals(abq.peek(), runMode)).findFirst().isPresent()) {
            int notifiedCount = 0;
            synchronized (mainToThreadsMonitor) {
//                CellGrid.statusLineWIsoTime(threadName, "some queues remain unemptied");
//                CellGrid.statusLineWIsoTime(threadName, "notifying all threads");
                mainToThreadsMonitor.notifyAll();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException exception) {
                continue;
            }
        }
        while (modeFlagQueues.stream().filter(abq -> !Objects.equals(abq.peek(), CellGridSection.FINISHED)).findFirst().isPresent()) {
            int notifiedCount = 0;
            synchronized (threadsToMainMonitor) {
//                CellGrid.statusLineWIsoTime(threadName, "some threads haven't posted FINISHED to queue yet");
//                CellGrid.statusLineWIsoTime(threadName, "waiting on threads");
                try {
                    threadsToMainMonitor.wait();
                } catch (InterruptedException exception) {
                    continue;
                }
            }
        }
//        CellGrid.statusLineWIsoTime(threadName, "all threads posted FINISHED; clearing queues");
        for (int index = 0; index < modeFlagQueues.size(); index ++) {
            try {
                modeFlagQueues.get(index).take();
            } catch (InterruptedException exception) {
                continue;
            }
        }
    }
}
