package jgameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Iterator;

public class CellGrid extends JComponent implements ActionListener {
    final int STEP = 250;
    Color fieldColor = Color.WHITE;
    int canvasWidth;
    int canvasHeight;
    int cellGridXdim;
    int cellGridYdim;
    int[][][] cellsGrid;

    boolean automataRunning = false;
    Timer animationTimer;

    public CellGrid(int canvasWidthVal, int canvasHeightVal) {
        assert canvasWidth % 10 == 0;
        assert canvasHeight % 10 == 0;
        canvasWidth = canvasWidthVal;
        canvasHeight = canvasHeightVal;
        cellGridXdim = canvasWidth / 10;
        cellGridYdim = canvasHeight / 10;
        cellsGrid = new int[cellGridXdim][cellGridYdim][2];

        for (int xIndex = 0; xIndex < cellGridXdim; xIndex++) {
            for (int yIndex = 0; yIndex < cellGridYdim; yIndex++) {
                cellsGrid[xIndex][yIndex][0] = 0;
            }
        }

        System.out.println("X dimension: " + cellGridXdim + "; Y dimension: " + cellGridYdim);
    }

    public void clearCellGrid() {
        for (int xIndex = 0; xIndex < cellGridXdim; xIndex++) {
            for (int yIndex = 0; yIndex < cellGridYdim; yIndex++) {
                cellsGrid[xIndex][yIndex][0] = 0;
            }
        }
    }

    protected void paintComponent(Graphics graphics) {
        graphics.setColor(fieldColor);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.setColor(Color.BLACK);
        for (int xIndex = 0; xIndex < cellGridXdim; xIndex++) {
            for (int yIndex = 0; yIndex < cellGridYdim; yIndex++) {
                if (cellsGrid[xIndex][yIndex][0] == 1) {
                    graphics.fillRect(xIndex * 10, yIndex * 10, 10, 10);
                }
            }
        }
    }

    public void seedCellGrid() {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Iterator<Integer> randomInts = rng.ints((long) cellGridXdim * (long) cellGridYdim).iterator();
        for (int xIndex = 0; xIndex < cellGridXdim; xIndex++) {
            for (int yIndex = 0; yIndex < cellGridYdim; yIndex++) {
                if ((int) Math.floor((double) randomInts.next() / (double) Integer.MAX_VALUE * 8) == 0) {
                    cellsGrid[xIndex][yIndex][0] = 1;
                }
            }
        }
    }

    public void startCellularAutomata() {
        if (! automataRunning) {
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

    public void stopCellularAutomata() {
        if (automataRunning) {
            automataRunning = false;
        }
        animationTimer.stop();
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("repaint")) {
            for (int xIndex = 0; xIndex < cellGridXdim; xIndex++) {
                for (int yIndex = 0; yIndex < cellGridYdim; yIndex++) {
                    int sumOfNeighbors = 0;
                    int moddedXIndex;
                    int moddedYIndex;

                    for (int xIndexDelta = -1; xIndexDelta <= 1; xIndexDelta++) {
                        for (int yIndexDelta = -1; yIndexDelta <= 1; yIndexDelta++) {
                            moddedXIndex = xIndex + xIndexDelta;
                            moddedYIndex = yIndex + yIndexDelta;
                            if (xIndexDelta == 0 && yIndexDelta == 0
                                    || moddedXIndex == -1 || moddedXIndex == cellGridXdim
                                    || moddedYIndex == -1 || moddedYIndex == cellGridYdim) {
                                continue;
                            }
                            sumOfNeighbors += cellsGrid[moddedXIndex][moddedYIndex][0];
                        }
                    }

                    if (cellsGrid[xIndex][yIndex][0] == 1) {
                        if (sumOfNeighbors < 2 || sumOfNeighbors > 3) {
                            cellsGrid[xIndex][yIndex][1] = 0;
                        } else {
                            cellsGrid[xIndex][yIndex][1] = 1;
                        }
                    } else if (sumOfNeighbors == 3) {
                        cellsGrid[xIndex][yIndex][1] = 1;
                    } else {
                        cellsGrid[xIndex][yIndex][1] = 0;
                    }
                }
            }

            for (int xIndex = 0; xIndex < cellGridXdim; xIndex++) {
                for (int yIndex = 0; yIndex < cellGridYdim; yIndex++) {
                    cellsGrid[xIndex][yIndex][0] = cellsGrid[xIndex][yIndex][1];
                }
            }

            repaint();
        }
    }

    private Point[] neighboringCoords(int xIndex, int yIndex) {
        Point[] points;
        int pointsArrayIndex = 0;
        int moddedXIndex;
        int moddedYIndex;

        if (xIndex == 0 || xIndex == cellGridXdim - 1) {
            if (yIndex == 0 || yIndex == cellGridYdim - 1) {
                points = new Point[3];
            } else {
                points = new Point[5];
            }
        } else {
            if (yIndex == 0 || yIndex == cellGridYdim - 1) {
                points = new Point[5];
            } else {
                points = new Point[8];
            }
        }

        for (int xIndexDelta = -1; xIndexDelta <= 1; xIndexDelta++) {
            for (int yIndexDelta = -1; yIndexDelta <= 1; yIndexDelta++) {
                moddedXIndex = xIndex + xIndexDelta;
                moddedYIndex = yIndex + yIndexDelta;
                if (xIndexDelta == 0 && yIndexDelta == 0
                        || moddedXIndex == -1 || moddedXIndex == cellGridXdim
                        || moddedYIndex == -1 || moddedYIndex == cellGridYdim) {
                    continue;
                }
                points[pointsArrayIndex] = new Point(moddedXIndex, moddedYIndex);
                pointsArrayIndex++;
            }
        }

        return points;
    }
}
