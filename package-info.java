/**
 * This package implements Conway's Game of Life in a Swing GUI. In an attempt
 * to speed up the execution, an alternative threaded implementation was also
 * devised, found in altthreadedimpl, but it didn't prove to make a difference.
 * Work with a profiler showed that the processor-intensive work is happening in
 * Graphics.fillRect-- fully 87% of the execution by processor use-- which is
 * called for each cell in the field. The code resists optimization and can't be
 * improved, and is preserved here as a historical curiosity.
 *
 * @since 0.9
 * @author Kerne M. Fahey
 * @version 0.9
 */
package com.kmfahey.jgameoflife;
