/**
 * This package implements Conway's Game of Life in a Swing GUI in a
 * single-threaded implementation; unfortunately, it runs slowly, and the
 * animation of the cellular automata chugs and lags. In an attempt to speed
 * up the execution, an alternative threaded implementation was also devised,
 * stored in altthreadedimpl, but it didn't prove to make a difference. Work
 * with a profiler showed that the processor-intensive work was happening in
 * Graphics.fillRect()-- fully 87% of the execution by processor use-- which is
 * called for each cell in the field. I have no way to optimize this package;
 * the processor-intensive code causing the chugging is in the Swing toolkit I'm
 * using, not my code. As a result, the code resists optimization and can't be
 * improved; it is preserved here as a historical record. If I ever learn C, C++
 * or Rust, I'll return to the Game of Life toy problem and re-implement it in a
 * compiled language that will run fast enough for speedy and even execution of
 * the algorithm.
 *
 * @since 0.9
 * @author MagentaToBe
 * @version 0.9
 */
package org.kmfahey.jgameoflife;
