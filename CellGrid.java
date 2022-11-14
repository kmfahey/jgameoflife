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
    final int STEP = 333;
    Color fieldColor = Color.WHITE;
    int canvasWidth;
    int canvasHeight;
    int cellGridHorizdim;
    int cellGridVertdim;
    int[][] displayGrid;
    int[][] updateGrid;
    int[][] bufferGrid;

    boolean automataRunning = false;
    Timer animationTimer;

    public CellGrid(Dimension cellGridDims) {
        canvasWidth = (int) cellGridDims.getWidth();
        canvasHeight = (int) cellGridDims.getHeight();
        cellGridHorizdim = canvasWidth / 10;
        cellGridVertdim = canvasHeight / 10;
        displayGrid = new int[cellGridHorizdim][cellGridVertdim];
        updateGrid = new int[cellGridHorizdim][cellGridVertdim];
        bufferGrid = new int[cellGridHorizdim][cellGridVertdim];

        for (int horizIndex = 0; horizIndex < cellGridHorizdim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < cellGridVertdim; vertIndex++) {
                displayGrid[horizIndex][vertIndex] = 0;
            }
        }

        System.out.println("X dimension: " + cellGridHorizdim + "; Y dimension: " + cellGridVertdim);
    }

    public void clearCellGrid() {
        for (int horizIndex = 0; horizIndex < cellGridHorizdim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < cellGridVertdim; vertIndex++) {
                displayGrid[horizIndex][vertIndex] = 0;
            }
        }
    }

    protected void paintComponent(Graphics graphics) {
        graphics.setColor(fieldColor);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.setColor(Color.BLACK);
        for (int horizIndex = 0; horizIndex < cellGridHorizdim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < cellGridVertdim; vertIndex++) {
                if (displayGrid[horizIndex][vertIndex] == 1) {
                    graphics.fillRect(horizIndex * 10, vertIndex * 10, 10, 10);
                }
            }
        }
        graphics.setColor(Color.WHITE);
        for (int horizIndex = 0; horizIndex < cellGridHorizdim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < cellGridVertdim; vertIndex++) {
                if (displayGrid[horizIndex][vertIndex] == 0) {
                    graphics.fillRect(horizIndex * 10, vertIndex * 10, 10, 10);
                }
            }
        }
    }

    public void seedCellGrid() {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Iterator<Integer> randomInts = rng.ints((long) cellGridHorizdim * (long) cellGridVertdim).iterator();
        for (int horizIndex = 0; horizIndex < cellGridHorizdim; horizIndex++) {
            for (int vertIndex = 0; vertIndex < cellGridVertdim; vertIndex++) {
                if ((int) Math.floor((double) randomInts.next() / (double) Integer.MAX_VALUE * 8) == 0) {
                    displayGrid[horizIndex][vertIndex] = 1;
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
            int[][] deltaPairs = new int[][] { new int[] { -1, -1 }, new int[] { -1, 0 }, new int[] { -1, +1 },
                                               new int[] { 0, -1 },                       new int[] { 0, +1 },
                                               new int[] { +1, -1 }, new int[] { +1, 0 }, new int[] { +1, +1 } };
            int sumOfNeighborhood = 0;
            int moddedHorizIndex, moddedVertIndex;
            int horizIndex, vertIndex;
            int horizIndexDelta, vertIndexDelta;
            for (horizIndex = 0; horizIndex < cellGridHorizdim; horizIndex++) {
                for (vertIndex = 0; vertIndex < cellGridVertdim; vertIndex++) {
                    sumOfNeighborhood = 0;
                    for (int[] deltaPair : deltaPairs) {
                        moddedHorizIndex = horizIndex + deltaPair[0];
                        moddedVertIndex = vertIndex + deltaPair[1];
                        moddedHorizIndex = (moddedHorizIndex == -1) ? cellGridHorizdim - 1 : (moddedHorizIndex == cellGridHorizdim) ? 0 : moddedHorizIndex;
                        moddedVertIndex = (moddedVertIndex == -1) ? cellGridVertdim - 1 : (moddedVertIndex == cellGridVertdim) ? 0 : moddedVertIndex;
                        sumOfNeighborhood += displayGrid[moddedHorizIndex][moddedVertIndex];
                    }
                    updateGrid[horizIndex][vertIndex] = (sumOfNeighborhood == 3) ? 1 : (sumOfNeighborhood == 2) ? updateGrid[horizIndex][vertIndex] : 0;
                }
            }

            for (horizIndex = 0; horizIndex < cellGridHorizdim; horizIndex++) {
                for (vertIndex = 0; vertIndex < cellGridVertdim; vertIndex++) {
                    displayGrid[horizIndex][vertIndex] = updateGrid[horizIndex][vertIndex];
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

        if (displayGrid[xCoord][yCoord] == 1) {
            displayGrid[xCoord][yCoord] = 0;
        } else {
            displayGrid[xCoord][yCoord] = 1;
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
