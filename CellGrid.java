package jgameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Iterator;

public class CellGrid extends JComponent implements ActionListener, MouseListener {
    final int STEP = 250;
    Color fieldColor = Color.WHITE;
    int canvasWidth;
    int canvasHeight;
    int cellGridXdim;
    int cellGridYdim;
    int[][][] cellsGrid;

    boolean automataRunning = false;
    Timer animationTimer;

    public CellGrid(Dimension cellGridDims) {
        canvasWidth = (int) cellGridDims.getWidth();
        canvasHeight = (int) cellGridDims.getHeight();
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
        graphics.setColor(Color.WHITE);
        for (int xIndex = 0; xIndex < cellGridXdim; xIndex++) {
            for (int yIndex = 0; yIndex < cellGridYdim; yIndex++) {
                if (cellsGrid[xIndex][yIndex][0] == 0) {
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
            int sumOfNeighborhood = 0;
            int moddedXIndex, moddedYIndex;
            int xIndex, yIndex;
            int xIndexDelta, yIndexDelta;
            for (xIndex = 0; xIndex < cellGridXdim; xIndex++) {
                for (yIndex = 0; yIndex < cellGridYdim; yIndex++) {
                    sumOfNeighborhood = 0;
                    for (xIndexDelta = -1; xIndexDelta <= 1; xIndexDelta++) {
                        for (yIndexDelta = -1; yIndexDelta <= 1; yIndexDelta++) {
                            moddedXIndex = xIndex + xIndexDelta;
                            moddedYIndex = yIndex + yIndexDelta;
                            if (moddedXIndex == -1) {
                                moddedXIndex = cellGridXdim - 1;
                            } else if (moddedXIndex == cellGridXdim) {
                                moddedXIndex = 0;
                            }
                            if (moddedYIndex == -1) {
                                moddedYIndex = cellGridYdim - 1;
                            } else if (moddedYIndex == cellGridYdim) {
                                moddedYIndex = 0;
                            }
                            sumOfNeighborhood += cellsGrid[moddedXIndex][moddedYIndex][0];
                        }
                    }
                    if (sumOfNeighborhood == 3) {
                        cellsGrid[xIndex][yIndex][1] = 1;
                    } else if (sumOfNeighborhood != 4) {
                        cellsGrid[xIndex][yIndex][1] = 0;
                    }
                }
            }

            for (xIndex = 0; xIndex < cellGridXdim; xIndex++) {
                for (yIndex = 0; yIndex < cellGridYdim; yIndex++) {
                    cellsGrid[xIndex][yIndex][0] = cellsGrid[xIndex][yIndex][1];
                }
            }

            repaint();
        }
    }

    public void mouseClicked(MouseEvent event) {
        int xCoord, yCoord;

        //System.out.println("Mouse clicked event!");

        xCoord = (int) Math.floor((double) event.getX() / 10D);
        yCoord = (int) Math.floor((double) event.getY() / 10D);

        if (cellsGrid[xCoord][yCoord][0] == 1) {
            cellsGrid[xCoord][yCoord][0] = 0;
        } else {
            cellsGrid[xCoord][yCoord][0] = 1;
        }
        
        repaint();
    }

    public void mouseEntered(MouseEvent event) {
        assert true;
        //System.out.println("Mouse entered event!");
    }

    public void mouseExited(MouseEvent event) {
        assert true;
        //System.out.println("Mouse exited event!");
    }

    public void mousePressed(MouseEvent event) {
        assert true;
        //System.out.println("Mouse pressed event!");
    }

    public void mouseReleased(MouseEvent event) {
        assert true;
        //System.out.println("Mouse released event!");
    }
}
