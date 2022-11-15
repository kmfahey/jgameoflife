package com.kmfahey.jgameoflife.altthreadedimpl;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

/**
 * This class is a JComponent subclass that implements the viewable area in
 * the GUI where the cellular automata runs. It divides the cell grid up into
 * a (currently) 4 x 4 subdivision of portions and allocates each one to a
 * CellGridSection object. CellGridSection implements the Runnable interface.
 * A CellGridDispatch object is instanced with the 2d array of CellGridSection
 * objects so that it can allocate each into its own thread and then use
 * inter-thread communication to dispatch cell grid alteration directives to
 * them.
 *
 * @see com.kmfahey.jgameoflife.altthreadedimpl.GameOfLife
 * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridDispatch
 * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridSection
 */
public class CellGrid extends JComponent implements ActionListener, MouseListener {

    /** This variable sets the horizontal dimension of the 2d cellGridSection
        array that will store subdivisions of the cell grid. */
    private final double horizSectionsDim = 4D;

    /** This variable sets the vertical dimension of the 2d cellGridSection
        array that will store subdivisions of the cell grid. */
    private final double vertSectionsDim = 4D;

    /** This variable holds the length of time in milliseconds that the Timer
        object is instructed to wait between "repaint" events. */
    private final int stepLengthMillis = 333;

    /** This variable holds the default color of the component, as used by
        paintComponent when it renders the component. */
    private final Color fieldColor = Color.WHITE;

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

    /** This 2d array is used to store the CellGridSection objects that divvy up
        the cell grid defined in this class to handle its tasks in a distributed
        fashion. */
    private CellGridSection[][] cellGridSections;

    /** This variable is used to store the horizontal dimensions of each
        subdivision of the cell grid that is apportioned to the CellGridSection
        objects instantiated in the CellGrid constructor. */
    private int[] sectionsHorizDims;

    /** This variable is used to store the vertical dimensions of each
        subdivision of the cell grid that is apportioned to the CellGridSection
        objects instantiated in the CellGrid constructor. */
    private int[] sectionsVertDims;

    /** This variable is used to store the CellGridDispatch object that handles
      * dispatching tasks to the CellGridSections used by this class to handle
      * individual portions of the cell grid. */
    private CellGridDispatch cellGridDispatch;

    /** This boolean is used to track whether the automata animation is running or not. */
    private boolean automataRunning = false;

    /** This Timer object is used to send regular "repaint" events to actionPerformed(). */
    private Timer animationTimer;

    /** This variable stores the default width in pixels of a single cell in the
        cell grid display. */
    private final int cellWidth = 10;

    /** This variable stores the default height in pixels of a single cell in the
        cell grid display. */
    private final int cellHeight = 10;

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

        /* The Dimensions object holding the width and height of this JComponent
           subclass are passed as an argument. I am guaranteed that these
           values are already both divisible by 10 since GameOfLife goes to
           some trouble to ensure that the insets surrounding this component
           correctly pad it down to the pixel such that the area the component
           renders in measured in values that are divisible by 10. */
        canvasWidth = (int) cellGridDims.getWidth();
        canvasHeight = (int) cellGridDims.getHeight();
        cellGridHorizDim = canvasWidth / cellWidth;
        cellGridVertDim = canvasHeight / cellHeight;

        instantiateCellGridSectionObjects();

        setCellGridSectionNeighbors();

        /* Lastly, a CellGridDispatch object is instantiated with the array of
           CellGridSection objects to set up for dispatching tasks to. */
        cellGridDispatch = new CellGridDispatch(cellGridSections);
    }

    /**
     * This method computes the constructor arguments for each of the
     * CellGridSection objects that this object will need in order to distribute
     * its cell grid to subordinate objects, and populates the instance variable
     * cellGridSections with them. * It * was refactored out of the constructor
     * in order to shorten its length and * make its logic easier to read.
     *
     * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridSection
     */
    private void instantiateCellGridSectionObjects() {

        /* These arrays will be populated by the dimensions of each cell grid
           subdivision that will be apportioned to the CellGridSection object
           stored at the same indices in the cellGridSections array, so that its
           constructor will be called with the correct 3rd and 4th arguments. */
        sectionsHorizDims = new int[(int) horizSectionsDim];
        sectionsVertDims = new int[(int) vertSectionsDim];

        int originCumulativeHorizCoord = 0;
        int originCumulativeVertCoord = 0;

        /* These values are used in computing the values for the above array. */
        int remainingHorizCells = cellGridHorizDim;
        int remainingVertCells = cellGridVertDim;

        cellGridSections = new CellGridSection[(int) horizSectionsDim][(int) vertSectionsDim];

        /* These two for loops determine the horizIndex and vertIndex at
           which the cell grid will be partitioned in order to assign each
           CellGridSection object a roughly equal section of the cell grid.
           They alternate between Math.floor and Math.ceil to round alternately
           up or down, and then for the last section in the line they allocate
           the entire remaining dimension, which should be roughly equal to the
           previously computed values. */
        for (int horizIndex = 0; horizIndex < cellGridSections.length; horizIndex++) {
            if (horizIndex == cellGridSections.length - 1) {
                sectionsHorizDims[horizIndex] = remainingHorizCells;
            } else {
                if (horizIndex % 2 == 0) {
                    sectionsHorizDims[horizIndex] = (int) Math.floor((double) cellGridHorizDim / horizSectionsDim);
                } else {
                    sectionsHorizDims[horizIndex] = (int) Math.ceil((double) cellGridHorizDim / horizSectionsDim);
                }
                remainingHorizCells -= sectionsHorizDims[horizIndex];
            }
        }
        for (int vertIndex = 0; vertIndex < cellGridSections[0].length; vertIndex++) {
            if (vertIndex == cellGridSections[0].length - 1) {
                sectionsVertDims[vertIndex] = remainingVertCells;
            } else {
                if (vertIndex % 2 == 0) {
                    sectionsVertDims[vertIndex] = (int) Math.floor((double) cellGridVertDim / vertSectionsDim);
                } else {
                    sectionsVertDims[vertIndex] = (int) Math.ceil((double) cellGridVertDim / vertSectionsDim);
                }
                remainingVertCells -= sectionsVertDims[vertIndex];
            }
        }

        /* The CellGridSection constructor accepts as its arguments the
           horiz and vert coordinates of its upper left corner, then the
           horizontal and vertical dimensions of its cell grid section. With the
           coordinates computed and the dimensions derivable, I can now instance
           each CellGridSection. */
        for (int horizIndex = 0; horizIndex < cellGridSections.length; horizIndex++) {
            originCumulativeVertCoord = 0;
            for (int vertIndex = 0; vertIndex < cellGridSections[0].length; vertIndex++) {
                cellGridSections[horizIndex][vertIndex] = new CellGridSection(sectionsHorizDims[horizIndex],
                                                                              sectionsVertDims[vertIndex],
                                                                              originCumulativeHorizCoord,
                                                                              originCumulativeVertCoord);
                originCumulativeVertCoord += sectionsVertDims[vertIndex];
            }
            originCumulativeHorizCoord += sectionsHorizDims[horizIndex];
        }
    }

    /**
     * This method computes the eight neighbors of each CellGridSection object
     * and assigns them to that object using CellGridSection.setNeighbor(). It
     * was refactored out of the constructor in order to shorten its length and
     * make its logic easier to read.
     *
     * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridSection
     */
    private void setCellGridSectionNeighbors() {

        /* This 2d array is used to store the -1/+0/+1 values that are used
           to modify a cell coordinate pair into each of the eight neighbors'
           coordinate pairs. I do it this way rather than using a for loop in
           order to avoid having to skip the (+0, +0) case. */
        int[][] deltaPairs = new int[][] {new int[] {-1, -1}, new int[] {-1, 0},
                                          new int[] {-1, +1}, new int[] {0, -1},
                                          new int[] {0, +1}, new int[] {+1, -1},
                                          new int[] {+1, 0}, new int[] {+1, +1}};

        /* This 2d array is used to associate coordinate pairs in the above
           array with (incrementing each index by 1) the correct compass
           direction argument to use when calling CellGridSection.setNeighbor()
           with the neighboring CellGridSection object reached using the above
           coordinates. */
        int[][] dirFlagsByDeltas = new int[][] {new int[] {CellGridSection.NORTHWEST,
                                                           CellGridSection.WEST,
                                                           CellGridSection.SOUTHWEST},
                                                new int[] {CellGridSection.NORTH,
                                                           -1,
                                                           CellGridSection.SOUTH},
                                                new int[] {CellGridSection.NORTHEAST,
                                                           CellGridSection.EAST,
                                                           CellGridSection.SOUTHEAST}};

        /* Each CellGridSection object needs to have stored references to each
           of its height lateral and diagonal neighbors, associated with compass
           direction constants defined in the CellGridSection class, so that
           algorithm computations sited at the border or coner of its cell grid
           can be completed with reference to the correct neighboring cell grid.

           These nested loops are devoted to discerning for each CellGridSection
           object what its eight neighbors are, associating each one
           with its compass direction constant, and setting it with
           CellGridSection.setNeighbor(). */

        for (int horizIndex = 0; horizIndex < cellGridSections.length; horizIndex++) {
            for (int vertIndex = 0; vertIndex < cellGridSections.length; vertIndex++) {
                for (int[] deltaPair : deltaPairs) {
                    /* moddedHorizIndex and moddedVertIndex are derived values
                       that are computed from (horizIndex, vertIndex) by adding
                       a pair between (-1,-1) and (+1,+1) (excluding (0,0))
                       to arrive at the coordinates of a CellGridSection that
                       neighbors the CellGridSection at (horizIndex, vertIndex). */
                    int moddedHorizIndex = horizIndex + deltaPair[0];
                    int moddedVertIndex = vertIndex + deltaPair[1];
                    int dirFlag = -1;
                    if (moddedHorizIndex == -1) {
                        moddedHorizIndex = cellGridSections.length - 1;
                    } else if (moddedHorizIndex == cellGridSections.length) {
                        moddedHorizIndex = 0;
                    }
                    if (moddedVertIndex == -1) {
                        moddedVertIndex = cellGridSections[0].length - 1;
                    } else if (moddedVertIndex == cellGridSections[0].length) {
                        moddedVertIndex = 0;
                    }

                    /* The dirFlag can be any one of eight compass direction
                       flags stored as constants on CellGridSection. Rather
                       than use an if/then cascade or a switch statement,
                       dirFlagsByDeltas stores the compass dir constants keyed
                       to indexes equal to the matching values in deltaPair each
                       incremented by 1. */
                    dirFlag = dirFlagsByDeltas[deltaPair[0] + 1][deltaPair[1] + 1];

                    /* With the correct dirFlag, the CellGridSection object
                       at (moddedHorizIndex, moddedVertIndex) can be set as
                       the neighbor at that compass dir on the CellGridSection
                       object at (horizIndex,vertIndex). */
                    cellGridSections[horizIndex][vertIndex]
                        .setNeighbor(
                            cellGridSections[moddedHorizIndex][moddedVertIndex], dirFlag);
                }
            }
        }
    }

    /**
     * This method is used to set all cells in the cell grid to 0, wiping out
     * all alife forms and resetting the cellular automata to its starting
     * state. It is called by the Clear button defined in the GameOfLife class.
     */
    public void clearCellGrid() {
        cellGridDispatch.clearSections();
    }

    /**
     * This method populates the cellGrid by randomly setting 0 cells to 1. On
     * average it sets 1/8th of the cells to 1. It is called by the Seed button
     * defined in the GameOfLife class.
     *
     */
    public void seedCellGrid() {
        cellGridDispatch.seedSections();
    }

    /**
     * This method calls CellGridDispatch.sectionsRunAlgorithm() to execute a
     * single step of the game of life algorithm.
     */
    public void sectionsRunAlgorithm() {
        cellGridDispatch.sectionsRunAlgorithm();
    }

    /**
     * This method is called when paint() or repaint() is called. It overrides
     * JComponent.paintComponent(). It iterates over the CellGridSections,
     * therewithin iterating across each one's cell grid and setting cells in
     * the JComponent area to black or white to render the composite cells grid
     * on-screen. According to a profiler, 87% of the work done by this program
     * is done by fillRect() calls in this method.
     *
     * @param graphics A graphics object that is used to draw in the component.
     * @see javax.swing.JComponent
     * @see java.awt.Graphics
     */
    protected void paintComponent(final Graphics graphics) {

        /* This array of 2-element int arrays stores coordinates that were not
           set to black so they can later be set to white without iterating over
           the CellGridSections object and each one's cell grid again. It is set
           to cellGridHorizDim * cellGridVertDim in length so it can hold up to
           the total cell grid size in cells of white coordinates. */
        int[][] whiteCoords = new int[cellGridHorizDim * cellGridVertDim][2];
        int whiteCoordsIndex = 0;

        graphics.setColor(fieldColor);
        graphics.fillRect(0, 0, canvasWidth, canvasHeight);
        graphics.setColor(Color.BLACK);

        /* These nested for loops iterate over the 2d array of CellGridSections,
           and then iterate over each CellGridSection object's cell grid to find
           1 values which mean a black cell can be drawn in the viewable area.
           In order to correct for a display bug, the coordinates of 0 values
           are stored to int[][] whiteCoords, which is later used to paint white
           cells without needing another quadruply nested four loop. */
        for (int outerHorizIndex = 0; outerHorizIndex < cellGridSections.length; outerHorizIndex++) {
            for (int outerVertIndex = 0; outerVertIndex < cellGridSections[0].length; outerVertIndex++) {

                /* These values are stored so they can be used without a full
                   call path. */
                int originHorizCoord = cellGridSections[outerHorizIndex][outerVertIndex].originHorizCoord;
                int originVertCoord = cellGridSections[outerHorizIndex][outerVertIndex].originVertCoord;

                for (int innerHorizIndex = 0;
                     innerHorizIndex < cellGridSections[outerHorizIndex][outerVertIndex].horizDim;
                     innerHorizIndex++) {
                    for (int innerVertIndex = 0;
                         innerVertIndex < cellGridSections[outerHorizIndex][outerVertIndex].vertDim;
                         innerVertIndex++) {
                        if (cellGridSections[outerHorizIndex][outerVertIndex]
                                .getDisplayCells()[innerHorizIndex][innerVertIndex] == 1) {
                            graphics.fillRect((originHorizCoord + innerHorizIndex) * cellWidth,
                                              (originVertCoord + innerVertIndex) * cellHeight,
                                              cellWidth, cellHeight);
                        } else {
                            whiteCoords[whiteCoordsIndex][0] = originHorizCoord + innerHorizIndex + 1;
                            whiteCoords[whiteCoordsIndex][1] = originVertCoord + innerVertIndex + 1;
                            whiteCoordsIndex++;
                        }
                    }
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

           I found that if I implemented this loop, which paints a 10px *white*
           square in the component area for every *0* cell, the issue went away.
           This is a major processing slowdown but I don't know any
           other way to ensure the blinkers issue goes away. */
        graphics.setColor(Color.WHITE);
        for (int[] coordsPair : whiteCoords) {
            /* The coordsPair array's 2-element arrays are initialized to 0.
               The coordinates are always stored with +1 added to each one, so
               valid 2-element arrays never contain a 0. When the first 0 value
               is encountered, I know I've reached the beginning of the unused
               pairs in the array and the loop can exit. */
            if (coordsPair[0] == 0) {
                break;
            }
            /* When coordinates are stored to this array, they're incremented
               by 1 so that 0 values are never stored and 0 can be treated as
               a signal value that means this loop has come to the end of the
               used portion of the array. So here the values are decremented to
               recover the original coordinate values. */
            graphics.fillRect((coordsPair[0] - 1) * cellWidth, (coordsPair[1] - 1) * cellHeight,
                               cellWidth, cellHeight);
        }
    }

    /**
     * This method is used to begin or resume the cellular automata
     * animation. It's called by the Start button in the GameOfLife class. It
     * either instantiated a Timer object and starts it, or if one already
     * exists it restarts it. The Timer object sends a "repaint" event to
     * actionPerformed() every stepLengthMillis milliseconds (optimistically).
     *
     */
    public void startCellularAutomata() {
        if (!automataRunning) {
            automataRunning = true;
        }
        if (animationTimer == null) {
            animationTimer = new Timer(stepLengthMillis, this);
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
     * above, every stepLengthMillis milliseconds (optimistically). It calls
     * cellGridDispatch.sectionsRunAlgorithm() to execute a single step of the
     * algorithm.
     *
     * @param event The event sent to this method by the Timer object that this
     *              class uses to regularly prompt steps of the algorithm so the
     *              cellular automata animates. Its getActionCommand() method
     *              should equal "repaint".
     * @see javax.swing.Timer
     * @see com.kmfahey.jgameoflife.altthreadedimpl.CellGridDispatch
     */
    public void actionPerformed(final ActionEvent event) {
        if (event.getActionCommand().equals("repaint")) {
            cellGridDispatch.sectionsRunAlgorithm();
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

        int moddedHorizCoord = 0;
        int moddedVertCoord = 0;
        int horizIndex = 0;
        int vertIndex = 0;

        /* These arrays are used to store the bounds at which each
           CellGridSection's portion of the cell grid begins and ends. */
        int[] sectionHorizOrdinateBounds = new int[cellGridSections.length + 1];
        int[] sectionVertOrdinateBounds = new int[cellGridSections[0].length + 1];

        /* The cells are 10 pixels on a side, so the X and Y values on the
           MouseEvent object are interpreted to coordinates in the cell grid by
           dividing them by 10 and rounding down. */
        int horizCoord = (int) Math.floor((double) event.getX() / cellWidth);
        int vertCoord = (int) Math.floor((double) event.getY() / cellHeight);

        /* sectionHorizOrdinateBounds is populated with the .originHorizCoord
           values from each CellGridSection in order, followed by
           cellGridHorizDim + 1 to mark the edge of the cell grid. */
        for (horizIndex = 0; horizIndex < cellGridSections.length; horizIndex++) {
            sectionHorizOrdinateBounds[horizIndex] = cellGridSections[horizIndex][vertIndex].originHorizCoord;
        }
        sectionHorizOrdinateBounds[horizIndex] = cellGridHorizDim;

        /* sectionVertOrdinateBounds is populated with the .originVertCoord
           values from each CellGridSection in order, followed by
           cellGridVertDim + 1 to mark the edge of the cell grid. */
        for (vertIndex = 0; vertIndex < cellGridSections[0].length; vertIndex++) {
            sectionVertOrdinateBounds[vertIndex] = cellGridSections[0][vertIndex].originVertCoord;
        }
        sectionVertOrdinateBounds[vertIndex] = cellGridVertDim;

        /* These two for loops iterate across the ordinate-bounds arrays, each
           looking for an index that points to a lower boundary that the coord
           value equals or exceeds, and that index + 1 points to an upper
           boundary that the coord value falls short of. Those indexes will
           point to the CellGridSection that the click falls within. Once that's
           determined, a modded coordinate value is computed that is the cell
           coordinate in that CellGridSection's cell grid. */
        for (horizIndex = 0; horizIndex < cellGridSections.length; horizIndex++) {
            if (sectionHorizOrdinateBounds[horizIndex] <= horizCoord
                    && horizCoord < sectionHorizOrdinateBounds[horizIndex + 1]) {
                moddedHorizCoord = horizCoord - sectionHorizOrdinateBounds[horizIndex];
                break;
            }
        }
        for (vertIndex = 0; vertIndex < cellGridSections.length; vertIndex++) {
            if (sectionVertOrdinateBounds[vertIndex] <= vertCoord
                    && vertCoord < sectionVertOrdinateBounds[vertIndex + 1]) {
                moddedVertCoord = vertCoord - sectionVertOrdinateBounds[vertIndex];
                break;
            }
        }

        /* Having determined which horizIndex and vertIndex point to the
           CellGridSection where the click falls, and the moddedHorizCoord and
           moddedVertCoord that point to that cell in its cell grid, the cell at
           that point is set to 0 if it's 1 or 1 if it's 0. */
        synchronized (cellGridSections[horizIndex][vertIndex].getDisplayCells()) {
            if (cellGridSections[horizIndex][vertIndex].getDisplayCells()[moddedHorizCoord][moddedVertCoord] == 1) {
                cellGridSections[horizIndex][vertIndex].getDisplayCells()[moddedHorizCoord][moddedVertCoord] = 0;
            } else {
                cellGridSections[horizIndex][vertIndex].getDisplayCells()[moddedHorizCoord][moddedVertCoord] = 1;
            }
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
    }
}
