#### Basic Usage

To run the main Game of Life implementation, compile all the java files to class
files, and then load the GameOfLife class as main to run the program. To run the
alternative threaded implementation, change directory to ./altthreadedimpl/,
compile those java files to class files, and load that GameOfLife class as main
to run the program.

#### Implementation Issues

There is no meaningful difference in performance between the two
implementations. The first implementation was found to chug and be laggy, so
I tried a threaded implementation that used distributed processing across 16
worker threads (since my machine has a 16-core processor).

This didn't prove to be a solution; I got hold of a Java profiler and examined
execution, discovering that the bulk of the processing-- 87% of processor
usage-- was happening in the java.awt.Graphics.fillRect() method that was being
called 10,000+ times per step of the algorithm to paint every single live or
dead cell to the viewable area where the cell grid was displayed. Due to a
tricky bug the number of calls to fillRect() could not be reduced; within the
limits of my Java knowledge, Java doesn't run fast enough to implement Conway's
Game of Life at a satisfactory pace with smooth animation.

#### Judgment

I consider this project a failed one, due to the optimization issues. I preserve
both implementations here for posterity; in particular the threaded distributed
processing implemented in the alternate threaded implementation is a good
example of that kind of solution, and I save it here for reference for the next
time I'm solving a problem like that.
