package com.kmfahey.jgameoflife.altthreadedimpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Iterator;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CellGrid extends JComponent implements ActionListener, MouseListener {
    final int STEP = 333;
    Color fieldColor = Color.WHITE;
    int canvasWidth;
    int canvasHeight;
    int cellGridHorizDim;
    int cellGridVertDim;
    CellGridSection[][] cellGridSections;
    int[] sectionsHorizDims;
    int[] sectionsVertDims;
    CellGridDispatch cellGridDispatch;

    boolean automataRunning = false;
    Timer animationTimer;

    public CellGrid(Dimension cellGridDims) {
        int originCumulativeHorizCoord = 0;
        int originCumulativeVertCoord = 0;

        canvasWidth = (int) cellGridDims.getWidth();
        canvasHeight = (int) cellGridDims.getHeight();
        cellGridHorizDim = canvasWidth / 10;
        cellGridVertDim = canvasHeight / 10;
        cellGridSections = new CellGridSection[4][4];
        sectionsHorizDims = new int[4];
        sectionsVertDims = new int[4];
        int remainingHorizCells = cellGridHorizDim;
        int remainingVertCells = cellGridVertDim;
        for (int horizIndex = 0; horizIndex < cellGridSections.length; horizIndex++) {
            if (horizIndex == cellGridSections.length - 1) {
                sectionsHorizDims[horizIndex] = remainingHorizCells;
            } else if (horizIndex % 2 == 0) {
                sectionsHorizDims[horizIndex] = (int) Math.floor((double) cellGridHorizDim / 4D);
                remainingHorizCells -= sectionsHorizDims[horizIndex];
            } else {
                sectionsHorizDims[horizIndex] = (int) Math.ceil((double) cellGridHorizDim / 4D);
                remainingHorizCells -= sectionsHorizDims[horizIndex];
            }
        }
        for (int vertIndex = 0; vertIndex < cellGridSections[0].length; vertIndex++) {
            if (vertIndex == cellGridSections[0].length - 1) {
                sectionsVertDims[vertIndex] = remainingVertCells;
            } else if (vertIndex % 2 == 0) {
                sectionsVertDims[vertIndex] = (int) Math.floor((double) cellGridVertDim / 4D);
                remainingVertCells -= sectionsVertDims[vertIndex];
            } else {
                sectionsVertDims[vertIndex] = (int) Math.ceil((double) cellGridVertDim / 4D);
                remainingVertCells -= sectionsVertDims[vertIndex];
            }
        }
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
        for (int horizIndex = 0; horizIndex < cellGridSections.length; horizIndex++) {
            for (int vertIndex = 0; vertIndex < cellGridSections.length; vertIndex++) {
                for (int horizIndexDelta = -1; horizIndexDelta <= 1; horizIndexDelta++) {
                    for (int vertIndexDelta = -1; vertIndexDelta <= 1; vertIndexDelta++) {
                        String dirStr = "";
                        int moddedHorizIndex = horizIndex + horizIndexDelta;
                        int moddedVertIndex = vertIndex + vertIndexDelta;
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
                        switch (horizIndexDelta) {
                            case -1:
                                switch (vertIndexDelta) {
                                    case -1: dirFlag = CellGridSection.NORTHWEST; dirStr = "NORTHWEST"; break;
                                    case 0: dirFlag = CellGridSection.WEST; dirStr = "WEST"; break;
                                    case 1: dirFlag = CellGridSection.SOUTHWEST; dirStr = "SOUTHWEST"; break;
                                };
                                break;
                            case 0:
                                switch (vertIndexDelta) {
                                    case -1: dirFlag = CellGridSection.NORTH; dirStr = "NORTH"; break;
                                    case 0: dirFlag = -1; break;
                                    case 1: dirFlag = CellGridSection.SOUTH; dirStr = "SOUTH"; break;
                                };
                                break;
                            case 1:
                                switch (vertIndexDelta) {
                                    case -1: dirFlag = CellGridSection.NORTHEAST; dirStr = "NORTHEAST"; break;
                                    case 0: dirFlag = CellGridSection.EAST; dirStr = "EAST"; break;
                                    case 1: dirFlag = CellGridSection.SOUTHEAST; dirStr = "SOUTHEAST"; break;
                                };
                                break;
                        }
                        if (dirFlag == -1) {
                            continue;
                        }
                        /* statusLineWIsoTime("main", "direction " + dirStr + " on section object " +
                                           "at (" + horizIndex + ", " + vertIndex + ") set to value that "
                                           + (Objects.isNull(cellGridSections[moddedHorizIndex][moddedVertIndex])
                                              ? "is" : "is not") + " null"); */
                        cellGridSections[horizIndex][vertIndex].setNeighbor(cellGridSections[moddedHorizIndex][moddedVertIndex], dirFlag);
                    }
                }
            }
        }
        cellGridDispatch = new CellGridDispatch(cellGridSections);
    }

    public static void statusLineWIsoTime(String threadName, String statusLine) {
        System.out.println(ZonedDateTime.now().format( DateTimeFormatter.ISO_INSTANT ) + ": Thread #" + threadName + ": " + statusLine);
    }

    public void clearCellGrid() {
        cellGridDispatch.clearSections();
    }

    public void seedCellGrid() {
        cellGridDispatch.seedSections();
    }

    public void sectionsRunAlgorithm() {
        cellGridDispatch.sectionsRunAlgorithm();
    }

    protected void paintComponent(Graphics graphics) {
        int[][] whiteCoords = new int[cellGridHorizDim * cellGridVertDim][2];
        int whiteCoordsIndex = 0;
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, canvasWidth, canvasHeight);
        graphics.setColor(Color.BLACK);
        for (int outerHorizIndex = 0; outerHorizIndex < cellGridSections.length; outerHorizIndex++) {
            for (int outerVertIndex = 0; outerVertIndex < cellGridSections[0].length; outerVertIndex++) {
                int originHorizCoord = cellGridSections[outerHorizIndex][outerVertIndex].originHorizCoord;
                int originVertCoord = cellGridSections[outerHorizIndex][outerVertIndex].originVertCoord;
//                //CellGrid.statusLineWIsoTime("main", "originHorizCoord = " + originHorizCoord + "; originVertCoord = " + originVertCoord);
                for (int innerHorizIndex = 0; innerHorizIndex < cellGridSections[outerHorizIndex][outerVertIndex].horizDim; innerHorizIndex++) {
                    for (int innerVertIndex = 0; innerVertIndex < cellGridSections[outerHorizIndex][outerVertIndex].vertDim; innerVertIndex++) {
                        if (cellGridSections[outerHorizIndex][outerVertIndex].displayCells[innerHorizIndex][innerVertIndex] == 1) {
                            CellGrid.statusLineWIsoTime("main", "setting (" + (originHorizCoord + innerHorizIndex) + ", " + (originVertCoord + innerVertIndex) + ") to black");
                            graphics.fillRect((originHorizCoord + innerHorizIndex) * 10, (originVertCoord + innerVertIndex) * 10, 10, 10);
                        } else {
                            whiteCoords[whiteCoordsIndex][0] = originHorizCoord + innerHorizIndex + 1;
                            whiteCoords[whiteCoordsIndex][1] = originVertCoord + innerVertIndex + 1;
                            whiteCoordsIndex++;
                        }
                    }
                }
            }
        }
        graphics.setColor(Color.WHITE);
        for (int[] coordsPair : whiteCoords) {
            if (coordsPair[0] == 0) {
                break;
            }
            graphics.fillRect((coordsPair[0] - 1) * 10, (coordsPair[1] - 1) * 10, 10, 10);
        }
    }

    public void startCellularAutomata() {
        if (! automataRunning) {
//            CellGrid.statusLineWIsoTime("main", "starting automata!");
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

    public void stopCellularAutomata() {
        if (automataRunning) {
            automataRunning = false;
        }
        animationTimer.stop();
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("repaint")) {
            cellGridDispatch.sectionsRunAlgorithm();
            repaint();
        }
    }

    public void mouseClicked(MouseEvent event) {
        int xCoord, yCoord;

//        CellGrid.statusLineWIsoTime("main", "mouse clicked event!");

        int[] sectionHorizOrdinateBounds = new int[cellGridSections.length + 1];
        int[] sectionVertOrdinateBounds = new int[cellGridSections[0].length + 1];
        int horizCoord = (int) Math.floor((double) event.getX() / 10D);
        int vertCoord = (int) Math.floor((double) event.getY() / 10D);
//        CellGrid.statusLineWIsoTime("main", "X value " + event.getX() + " computed to horizCoord " + horizCoord);
//        CellGrid.statusLineWIsoTime("main", "Y value " + event.getY() + " computed to vertCoord " + vertCoord);
        int moddedHorizCoord = 0;
        int moddedVertCoord = 0;
        int horizIndex = 0;
        int vertIndex = 0;

//        CellGrid.statusLineWIsoTime("main", "sectionHorizOrdinateBounds.length = " + sectionHorizOrdinateBounds.length);
        for (horizIndex = 0; horizIndex < cellGridSections.length; horizIndex++) {
            sectionHorizOrdinateBounds[horizIndex] = cellGridSections[horizIndex][vertIndex].originHorizCoord;
        }
        sectionHorizOrdinateBounds[horizIndex] = cellGridHorizDim;
//        CellGrid.statusLineWIsoTime("main", "sectionVertOrdinateBounds.length = " + sectionVertOrdinateBounds.length);
        for (vertIndex = 0; vertIndex < cellGridSections[0].length; vertIndex++) {
            sectionVertOrdinateBounds[vertIndex] = cellGridSections[0][vertIndex].originVertCoord;
        }
        sectionVertOrdinateBounds[vertIndex] = cellGridVertDim;

        for (horizIndex = 0; horizIndex < cellGridSections.length; horizIndex++) {
            if (sectionHorizOrdinateBounds[horizIndex] < horizCoord && horizCoord < sectionHorizOrdinateBounds[horizIndex + 1]) {
//                CellGrid.statusLineWIsoTime("main", "horizCoord " + horizCoord + " is bracketed by " + sectionHorizOrdinateBounds[horizIndex] + " and " + sectionHorizOrdinateBounds[horizIndex + 1] + " at horizIndex " + horizIndex);
                moddedHorizCoord = horizCoord - sectionHorizOrdinateBounds[horizIndex];
//                CellGrid.statusLineWIsoTime("main", "moddedHorizCoord " + moddedHorizCoord);
                break;
            }
        }
        for (vertIndex = 0; vertIndex < cellGridSections.length; vertIndex++) {
            if (sectionVertOrdinateBounds[vertIndex] < vertCoord && vertCoord < sectionVertOrdinateBounds[vertIndex + 1]) {
//                CellGrid.statusLineWIsoTime("main", "vertCoord " + vertCoord + " is bracketed by " + sectionVertOrdinateBounds[vertIndex] + " and " + sectionVertOrdinateBounds[vertIndex + 1] + " at vertIndex " + vertIndex);
                moddedVertCoord = vertCoord - sectionVertOrdinateBounds[vertIndex];
//                CellGrid.statusLineWIsoTime("main", "moddedVertCoord " + moddedVertCoord);
                break;
            }
        }

//        CellGrid.statusLineWIsoTime("main", "using moddedHorizCoord " + moddedHorizCoord + " and moddedVertCoord " + moddedVertCoord);
        synchronized (cellGridSections[horizIndex][vertIndex].displayCells) {
            if (cellGridSections[horizIndex][vertIndex].displayCells[moddedHorizCoord][moddedVertCoord] == 1) {
                cellGridSections[horizIndex][vertIndex].displayCells[moddedHorizCoord][moddedVertCoord] = 0;
            } else {
                cellGridSections[horizIndex][vertIndex].displayCells[moddedHorizCoord][moddedVertCoord] = 1;
            }
        }
        repaint();
    }

    public void mouseEntered(MouseEvent event) {
        assert true;
//        //CellGrid.statusLineWIsoTime("main", "Mouse entered event!");
    }

    public void mouseExited(MouseEvent event) {
        assert true;
//        //CellGrid.statusLineWIsoTime("main", "Mouse exited event!");
    }

    public void mousePressed(MouseEvent event) {
        assert true;
//        //CellGrid.statusLineWIsoTime("main", "Mouse pressed event!");
    }

    public void mouseReleased(MouseEvent event) {
        assert true;
//        //CellGrid.statusLineWIsoTime("main", "Mouse released event!");
    }
}
