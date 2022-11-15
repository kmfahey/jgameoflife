package com.kmfahey.jgameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.concurrent.ThreadLocalRandom;
import java.util.NoSuchElementException;
import java.util.Iterator;

/**
 * This class implements the cells grid of Conway's Game of Life as a subclass
 * of JComponent. It implements the ActionListener interface so a Timer object
 * can prompt individual algorithm steps on a regular period so as to effect
 * the Game of Life animation. It also implements the MouseListener interface
 * so that it can detect and respond to mouse clicks in the component area by
 * inverting the value of the cell clicked on.
 *
 * @see com.kmfahey.jgameoflife.GameOfLife
 * @see javax.swing.JComponent
 * @see javax.swing.Timer
 * @see java.awt.event.MouseListener
 * @see java.awt.event.ActionListener
 */
public class CellGrid extends JComponent implements ActionListener, MouseListener {

    /** This variable holds the length of time in milliseconds that the Timer
        object is instructed to wait between "repaint" events. */
    private final int STEP = 333;

    /** This variable holds the default color of the component, as used by
        paintComponent when it renders the component. */
    private final Color FIELD_COLOR = Color.WHITE;

    /** This variable is used to store the width of the component, in pixels. */
    private int canvasWidth;

    /** This variable is used to store the height of the component, in pixels. */
    private int canvasHeight;

    /** This variable is used to store the horizontal dimension of the cell
        grid, in cells. */
    private int cellGridHorizDim;

    /** This variable is used to store the vertical dimension of the cell grid,
        in cells. */
    private int cellGridVertDim;

    /** This variable is used to store the active cellular automata grid that is
        displayed when the component is rendered. */
    private int[][] displayGrid;

    /** This variable is used to store the next generation of the cellular
        automata while it's being calculated. */
    private int[][] updateGrid;

    /** This boolean is used to track whether the automata animation is running or not. */
    private boolean automataRunning = false;

    /** This Timer object is used to send regular "repaint" events to actionPerformed(). */
    private Timer animationTimer;

    /**
     * This constructor initializes the CellGrid object. It initializes the
     * instance variables and calls clearCellGrid() to set every cell in the
     * cell grid to 0.
     *
     * @param cellGridDims A Dimension object that holds the width and height of
     *                     the component; the size of the cell grid is set from
     *                     these values.
     */
    public CellGrid(final Dimension cellGridDims) {
        final int CELL_WIDTH = 10;
        final int CELL_HEIGHT = 10;

        canvasWidth = (int) cellGridDims.getWidth();
        canvasHeight = (int) cellGridDims.getHeight();
        cellGridHorizDim = canvasWidth / CELL_WIDTH;
        cellGridVertDim = canvasHeight / CELL_HEIGHT;
        displayGrid = new int[cellGridHorizDim][cellGridVertDim];
        updateGrid = new int[cellGridHorizDim][cellGridVertDim];

        clearCellGrid();

        //System.out.println("X dimension: " + cellGridHorizDim + "; Y dimension: " + cellGridVertDim);
    }

    /**
     * This method is used to set all cells in the cell grid to 0, wiping out
     * all alife forms and resetting the cellular automata to its starting
     * state. It is called by the Clear button defined in the GameOfLife class.
     */
    public void clearCellGrid() {
        for (int horizIndex = 0; horizIndex < cellGridHorizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < cellGridVertDim; vertIndex++) {
                displayGrid[horizIndex][vertIndex] = 0;
            }
        }
    }

    /**
     * This method is called when paint() or repaint() is called. It overrides
     * JComponent.paintComponent(). displayGrid cells grid and sets cells in the
     * JComponent area to black or white to render the cells grid on-screen.
     * According to a profiler, 87% of the work done by this program is done by
     * fillRect() calls in this method.
     *
     * @param graphics A graphics object that is used to draw in the component.
     * @see javax.swing.JComponent
     * @see java.awt.Graphics
     */
    protected void paintComponent(final Graphics graphics) {
        final int CELL_WIDTH = 10;
        final int CELL_HEIGHT = 10;

        graphics.setColor(FIELD_COLOR);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.setColor(Color.BLACK);
        /* These nested for loops iterate over the displayGrid 2d array and use
           Graphics.fillRect to draw a 10px black square in the appropriate
           place in the component area for every 1 value in the cell grid. */
        for (int horizIndex = 0; horizIndex < cellGridHorizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < cellGridVertDim; vertIndex++) {
                if (displayGrid[horizIndex][vertIndex] == 1) {
                    graphics.fillRect(horizIndex * CELL_WIDTH, vertIndex * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                }
            }
        }
        /* I'm displeased by having to do this, but it seems
           necessary. I was dealing with a weird bug where <a
           href="https://conwaylife.com/wiki/File:Blinker.gif">blinkers</a> --
           and only blinkers -- would hang and not update while the rest of the
           automata continued to update. I dug into the update algorithm and
           found the correct transformations were being done for the blinkers --
           so it was a display issue.

           I found that if I implemented this nested loop, which paints a 10px
           *white* square in the component area for every *0* cell, the issue
           went away. This is a major processing slowdown but I don't know any
           other way to ensure the blinkers issue goes away. */
        graphics.setColor(Color.WHITE);
        for (int horizIndex = 0; horizIndex < cellGridHorizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < cellGridVertDim; vertIndex++) {
                if (displayGrid[horizIndex][vertIndex] == 0) {
                    graphics.fillRect(horizIndex * CELL_WIDTH, vertIndex * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                }
            }
        }
    }

    /**
     * This method populates the cellGrid by randomly setting 0 cells to 1. On
     * average it sets 1/8th of the cells to 1. It is called by the Seed button
     * defined in the GameOfLife class.
     *
     */
    public void seedCellGrid() {
        /* ThreadLocalRandom is used to provide a stream of random ints that
           are used to determine whether a cell should be set to 1 or not. */
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Iterator<Integer> randomInts = rng.ints((long) cellGridHorizDim * (long) cellGridVertDim).iterator();
        for (int horizIndex = 0; horizIndex < cellGridHorizDim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < cellGridVertDim; vertIndex++) {
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
                    displayGrid[horizIndex][vertIndex] = 1;
                }
            }
        }
    }

    /**
     * This method is used to begin or resume the cellular automata
     * animation. It's called by the Start button in the GameOfLife class. It
     * either instantiated a Timer object and starts it, or if one already
     * exists it restarts it. The Timer object sends a "repaint" event to
     * actionPerformed() every STEP milliseconds (optimistically).
     *
     */
    public void startCellularAutomata() {
        if (!automataRunning) {
            System.out.println("Starting automata!");
            automataRunning = true;
        }
        if (animationTimer == null) {
            animationTimer = new Timer(STEP, this);
            animationTimer.setActionCommand("repaint");
            animationTimer.setRepeats(true);
            animationTimer.start();
        } else if (!animationTimer.isRunning()) {
            animationTimer.restart();
        }
    }

    /**
     * This method is used to suspend the cellular automata animation. It's
     * called by the Stop button defined in the GameOfLife class. It stops
     * the Timer object that is used to send regular repaint events to
     * actionPerformed().
     */
    public void stopCellularAutomata() {
        if (automataRunning) {
            automataRunning = false;
        }
        animationTimer.stop();
    }

    /**
     * This method is called by the Timer object set by startCellularAutomata()
     * above, every STEP milliseconds (optimistically). A single step of the
     * Conway's Game of Life algorithm is executed here.
     *
     * @param event The event sent to this method by the Timer object that this
     *              class uses to regularly prompt steps of the algorithm so the
     *              cellular automata animates. Its getActionCommand() method
     *              should equal "repaint".
     * @see javax.swing.Timer
     */
    public void actionPerformed(final ActionEvent event) {
        /* This 2d array stores the offsets used in the algorithm to mod the
           cell coordinates into coordinates of the neighboring cells. This is
           used in preference to two nested for loops in order to avoid having
           to test for and skip the horizDelta=0, vertDelta=0 case. */
        int[][] deltaPairs = new int[][] {new int[] {-1, -1}, new int[] {-1, 0}, new int[] {-1, +1},
                                          new int[] {0, -1},                     new int[] {0, +1},
                                          new int[] {+1, -1}, new int[] {+1, 0}, new int[] {+1, +1}};
        int sumOfNeighbors = 0;
        int moddedHorizIndex;
        int moddedVertIndex;
        int horizIndex;
        int vertIndex;
        int horizIndexDelta;
        int vertIndexDelta;

        if (event.getActionCommand().equals("repaint")) {
            /* The for loops iterate across the horizIndex and vertIndex of
               every cell in the cell grid. */
            for (horizIndex = 0; horizIndex < cellGridHorizDim; horizIndex++) {
                for (vertIndex = 0; vertIndex < cellGridVertDim; vertIndex++) {
                    sumOfNeighbors = 0;
                    /* The coordinate delta values in deltaPairs are used to
                       compute from the cell coordinates into the coordinates of
                       every neighboring cell. */
                    for (int[] deltaPair : deltaPairs) {
                        moddedHorizIndex = horizIndex + deltaPair[0];
                        moddedVertIndex = vertIndex + deltaPair[1];
                        /* The cell grid has wrap-around borders. That is
                           achieved by replacing a neighbor cooordinate value of
                           -1 as the max value for that ordinate, and a neighbor
                           coordinate of the max ordinate value + 1 as 0. */
                        moddedHorizIndex = (moddedHorizIndex == -1) ? cellGridHorizDim - 1
                                               : (moddedHorizIndex == cellGridHorizDim) ? 0
                                               : moddedHorizIndex;
                        moddedVertIndex = (moddedVertIndex == -1) ? cellGridVertDim - 1
                                               : (moddedVertIndex == cellGridVertDim) ? 0 : moddedVertIndex;
                        sumOfNeighbors += displayGrid[moddedHorizIndex][moddedVertIndex];
                    }
                    /* The cell in the grid is set to 1 if the neighboring cells
                       sum to 3, or to its existing value if they sum to 2, or
                       otherwise it's set to 0. */
                    updateGrid[horizIndex][vertIndex] = (sumOfNeighbors == 3) ? 1 : (sumOfNeighbors == 2) ? updateGrid[horizIndex][vertIndex] : 0;
                }
            }

            /* Two grids are used: displayGrid is the 2d array that's used to
               update the JComponent's cell display. updateGrid is the offscreen
               buffer object that updated values are computed into. Two grids
               must be used since the calculations would go awry if displayCell
               values were being updated even as the summing operation was
               running from a mixture of old and updated values.

               In this step I iterate across displayGrid and update it from
               matching values in updateGrid. */
            for (horizIndex = 0; horizIndex < cellGridHorizDim; horizIndex++) {
                for (vertIndex = 0; vertIndex < cellGridVertDim; vertIndex++) {
                    displayGrid[horizIndex][vertIndex] = updateGrid[horizIndex][vertIndex];
                }
            }

            repaint();
        }
    }

    /**
     * An implementation of MouseListener.mouseClicked, as part of the
     * MouseListener interface. The pixel X and Y values are collected from the
     * event and used to determine a cell in the cell grid. If that cell is 1,
     * it's set to 0; if 0, it's set to 1.
     *
     * @param event The event to be processed.
     * @see java.awt.event.MouseListener
     */
    public void mouseClicked(final MouseEvent event) {
        final double CELL_WIDTH = 10D;
        final double CELL_HEIGHT = 10D;
        int horizCoord;
        int vertCoord;

        /* The cells are 10 pixels on a side, so the X and Y values on the
           MouseEvent object are interpreted to coordinates in the cell grid by
           dividing them by 10 and rounding down. */
        horizCoord = (int) Math.floor((double) event.getX() / CELL_WIDTH);
        vertCoord = (int) Math.floor((double) event.getY() / CELL_HEIGHT);

        /* The cell at those coordinates is inverted from 1 to 0, or 0 to 1. */
        if (displayGrid[horizCoord][vertCoord] == 1) {
            displayGrid[horizCoord][vertCoord] = 0;
        } else {
            displayGrid[horizCoord][vertCoord] = 1;
        }

        /* repaint() is called, which will trigger paintComponent above. */
        repaint();
    }

    /**
     * An implementation of MouseListener.mouseEntered, required because I
     * implement that interface. I ignore the event, so 'assert true' is used as
     * a filler line.
     *
     * @param event The event to be processed.
     * @see java.awt.event.MouseListener
     */
    public void mouseEntered(final MouseEvent event) {
        assert true;
        //System.out.println("Mouse entered event!");
    }

    /**
     * An implementation of MouseListener.mouseExited, required because I
     * implement that interface. I ignore the event, so 'assert true' is used as
     * a filler line.
     *
     * @param event The event to be processed.
     * @see java.awt.event.MouseListener
     */
    public void mouseExited(final MouseEvent event) {
        assert true;
        //System.out.println("Mouse exited event!");
    }

    /**
     * An implementation of MouseListener.mousePressed, required because I
     * implement that interface. I ignore the event, so 'assert true' is used as
     * a filler line.
     *
     * @param event The event to be processed.
     * @see java.awt.event.MouseListener
     */
    public void mousePressed(final MouseEvent event) {
        assert true;
        //System.out.println("Mouse pressed event!");
    }

    /**
     * An implementation of MouseListener.mouseReleased, required because I
     * implement that interface. I ignore the event, so 'assert true' is used as
     * a filler line.
     *
     * @param event The event to be processed.
     * @see java.awt.event.MouseListener
     */
    public void mouseReleased(final MouseEvent event) {
        assert true;
        //System.out.println("Mouse released event!");
    }
}
