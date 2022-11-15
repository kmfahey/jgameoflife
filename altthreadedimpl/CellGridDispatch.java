package com.kmfahey.jgameoflife.altthreadedimpl;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.Objects;

/**
 * This class implements a worker-threads-manager that instances and maintains
 * communication with (currently) 16 worker threads each of which is running
 * a CellGridSection object's run() method. Monitor objects are used to
 * notifyAll() the worker threads into action, and then wait() on their notify()
 * calls; but the actual communication of task states is managed using 16
 * 1-capacity ArrayBlockingQueue&lt;Integer&gt; objects which hold signal values
 * that are passed back and forth. This object is instanced by the controlling
 * CellGrid object, and uses the CellGridSection objects that it suppplies to
 * this class's constructor.
 *
 * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid
 * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridSection
 */
public class CellGridDispatch {

    /** This variable stores the CellGridSection objects instantiated by the
     *  CellGrid object and passed to this object as the argument to the
     *  constructor. */
    private CellGridSection[] cellGridSections;

    /** This variable stores the (currently) 16 Thread objects that embody the
     *  worker threads that this object in the main thread delegates tasks to. */
    private Thread[] threadArray;

    /** This variable holds the monitor object that worker threads wait()
     *  on and this object in the main thread notifyAll()s on. This thread doesn't
     *  wait() on it and the worker threads don't notify() on it. */
    private final Object mainToThreadsMonitor = new Object();

    /** This variable holds the monitor object that worker threads notify()
     *  on and this object in the main thread wait()s on. This thread doesn't
     *  notify() on it and the worker threads don't wait() on it. */
    private final Object threadsToMainMonitor = new Object();

    /** This variable stores the ArrayBlockingQueue&lt;Integer&gt; objects
     *  used to communicate signal values to each thread. There is one for
     *  every thread in the threadArray and every CellGridSection object in the
     *  cellGridSections array, at the same indexes. */
    private volatile ArrayList<ArrayBlockingQueue<Integer>> modeFlagQueues;

    /**
     * This method initializes the object and set instance variables. It's
     * called with a 2d array of CellGridSection objects, which it copies to a
     * single array and discards. It sets monitor objects and a queue object
     * on each CellGridSection objects, then instances a Thread around each
     * CellGridSection object and start()s it.
     *
     * @param sectionObjs A 2d array of CellGridSection objects, which this
     *                    object will delegate tasks to.
     * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGrid
     * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridSection
     */
    public CellGridDispatch(final CellGridSection[][] sectionObjs) {
        /* This iterator variable is used across two for loops looping on other
           variables in order to keep track of the insertion point in the
           cellGridSections array. */
        int cellGridSectionsIndex = 0;

        cellGridSections = new CellGridSection[sectionObjs.length * sectionObjs[0].length];
        threadArray = new Thread[cellGridSections.length];
        modeFlagQueues = new ArrayList<ArrayBlockingQueue<Integer>>();

        /* This loop populates the modeFlagQueue object with
           ArrayBlockingQueue<Integer> objects. */
        for (int index = 0; index < cellGridSections.length; index++) {
            modeFlagQueues.add(new ArrayBlockingQueue<Integer>(1));
        }

        /* This loop copies the contents of the 2d sectionObjs array to a single
           array cellGridSections. */
        for (int horizIndex = 0; horizIndex < sectionObjs.length; horizIndex++) {
            for (int vertIndex = 0; vertIndex < sectionObjs[0].length; vertIndex++) {
                cellGridSections[cellGridSectionsIndex] = sectionObjs[horizIndex][vertIndex];
                cellGridSectionsIndex++;
            }
        }

        /* This loop sets two monitor objects on each CellGridSection object,
           and also supplies its designated ArrayBlockingQueue<Integer> object
           out of the array of same. */
        for (int index = 0; index < cellGridSections.length; index++) {
            cellGridSections[index].setMonitors(mainToThreadsMonitor, threadsToMainMonitor);
            cellGridSections[index].setModeFlagQueue(modeFlagQueues.get(index));
        }

        /* This loop instantiates a Thread object around each CellGridSection
           object in the array of them, sets a name, and then start()s it. Its
           run() method is immediately called, but because its queue is empty,
           it goes directly to wait()ing. */
        for (int index = 0; index < threadArray.length; index++) {
            threadArray[index] = new Thread(cellGridSections[index]);
            threadArray[index].setName(String.valueOf(index));
            threadArray[index].start();
        }
    }

    /**
     * This method is shorthand for calling delegateRunMode with the MODE_SEED
     * flag to cause the composite cells grid to be populated with randomly
     * assigned 'live' cells.
     */
    public void seedSections() {
        delegateRunMode(CellGridSection.MODE_SEED);
    }

    /**
     * This method is shorthand for calling delegateRunMode with the MODE_CLEAR
     * flag to cause the composite cells grid to be cleared of all 'live' cells.
     */
    public void clearSections() {
        delegateRunMode(CellGridSection.MODE_CLEAR);
    }

    /**
     * This method is shorthand for calling delegateRunMode with the MODE_UPDATE
     * and MODE_DISPLAY flags to effect a single step in the game of life
     * algorithm.
     */
    public void sectionsRunAlgorithm() {
        delegateRunMode(CellGridSection.MODE_UPDATE);
        delegateRunMode(CellGridSection.MODE_DISPLAY);
    }

    /**
     * This method accepts a CellGridSection constant execution mode flag and
     * distributes it to worker threads to execute the matching method on all
     * (currently) 16 CellGridSection objects. More spewcifically, it uses
     * 1-capacity ArrayBlockingQueue&lt;Integer&gt; queues to distribute it to
     * the CellGridSection.run() methods running in worker threads, notifyAll()s
     * to wake them up, wait()s untl the queues have been repopulated with
     * CellGridSection.FINISHED flags, collects them and completes.
     *
     * @param runMode An integer flag, one of CellGridSection.MODE_CLEAR,
     *                CellGridSection.MODE_SEED, CellGridSection.MODE_DISPLAY,
     *                CellGridSection.MODE_or UPDATE.
     * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridSection
     */
    public void delegateRunMode(final int runMode) {
        final int sleepIntervalMillis = 10;

        synchronized (modeFlagQueues) {
            /* The fancy one-liner that's the conditional for this while loop
               filters modeFlagQueues using a lambda expression and returns true
               if at least one of the 1-capacity queues doesn't contain the
               runMode flag yet. It's used to repeat the operation that inserts
               the runMode in each queue until all queues have been populated,
               in case an InterruptedException breaks the execution of that
               process. */
            alldlgtd:
            while (modeFlagQueues.stream()
                   .filter(abq -> !Objects.equals(abq.peek(), runMode))
                   .findFirst().isPresent()) {
                for (int index = 0; index < modeFlagQueues.size(); index++) {
                    try {
                        if (Objects.isNull(modeFlagQueues.get(index).peek())) {
                            modeFlagQueues.get(index).put(runMode);
                        }
                    } catch (InterruptedException exception) {
                        continue alldlgtd;
                    }
                }
            }
        }
        /* The one-liner that's the conditional for this while loop filters
           modeFlagQueues using a lambda expression and returns true if at
           least one of the 1-capacity queues still contains the runMode flag
           that was put in it. This loop repeatedly send notifyAll(); the
           CellGridSection.run() method that's waiting on the signal responds
           by removing the runMode flag from the queue, which signals that it's
           started the task. This loop repeats until all the threads have so
           signalled. */
        while (modeFlagQueues.stream()
               .filter(abq -> Objects.equals(abq.peek(), runMode))
               .findFirst().isPresent()) {
            int notifiedCount = 0;
            synchronized (mainToThreadsMonitor) {
                mainToThreadsMonitor.notifyAll();
            }
            try {
                Thread.sleep(sleepIntervalMillis);
            } catch (InterruptedException exception) {
                continue;
            }
        }
        /* The one-liner that's the conditional for this while loop filters
           modeFlagQueues using a lambda expression and returns true
           if at least one of the 1-capacity queues doesn't contain a
           CellGridSection.FINISHED signal value. The body of the loop wait()s
           on signals from the worker threads. CellGridSection.run() inserts a
           FINISHED value in the queue and then notify()s this thread. The loop
           repeats until all (currently) 16 worker threads have so signalled. */
        allfinshd:
        while (modeFlagQueues.stream()
                          .filter(abq -> !Objects.equals(abq.peek(), CellGridSection.FINISHED))
                          .findFirst().isPresent()) {
            int notifiedCount = 0;
            synchronized (threadsToMainMonitor) {
                try {
                    threadsToMainMonitor.wait();
                } catch (InterruptedException exception) {
                    continue allfinshd;
                }
            }
        }
        /* The one-liner that's the conditional for this while loop filters
           modeFlagQueues using a lambda expression and returns true if at
           least one of the 1-capacity queues still has a nonzero size.
           The body of the loop attempts to empty each queue, but since an
           InterruptedException can upset that operation, a while loop is used
           to ensure the loop is restarted as many times as needed to empty all
           16 of them. */
        emptyqs:
        while (modeFlagQueues.stream()
                        .filter(abq -> abq.size() == 1).findFirst().isPresent()) {
            for (int index = 0; index < modeFlagQueues.size(); index++) {
                if (modeFlagQueues.get(index).size() == 0) {
                    continue;
                }
                try {
                    modeFlagQueues.get(index).take();
                } catch (InterruptedException exception) {
                    continue emptyqs;
                }
            }
        }
    }
}
