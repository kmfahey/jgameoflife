/**
 * This package implements Conway's Game of Life in a Swing GUI using a threaded
 * implementation that divides the cell grid into 16 subdivisions and uses a
 * thread apiece in order to distribute and parallelize the work of the game of
 * life algorithm. Because the slowdown that was making the base implementation
 * chug laid in the 10,000+ Graphics.fillRect() calls per step of the algorithm,
 * rather than in any code I'd written, this clever &amp; overdesigned solution
 * failed to remedy the slowdowns in execution.
 * <p>
 * It was ripped out of the main code, since it's a needless overcomplication
 * that would obstruct further development (if any), but because it was a good
 * example of threaded divide-and-conquer of a computing task, I've preserved
 * it here for my own memory in case I need to implement a comparable solution
 * again. It makes use of monitor objects, interthread signalling via wait(),
 * notify(), notifyAll(), and passing signal values back and forth using
 * per-thread java.util.concurrent.ArrayBlockingQueue&lt;Integer&gt; objects.
 *
 * @since 0.9
 * @author Kerne M. Fahey
 * @version 0.9
 */
package com.kmfahey.jgameoflife.altthreadedimpl;
