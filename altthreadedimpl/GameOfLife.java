package com.kmfahey.jgameoflife.altthreadedimpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOfLife extends JFrame {

    GridBagLayout gameLayout = new GridBagLayout();
    JPanel gamePane = new JPanel(gameLayout);
    CellGrid cellGrid;

    public GameOfLife() {
        super("Conway's Game of Life");

        /*
         * These calculations are misleading. windowDims width and height
         * are derived from the screen's dimensions, and used to setSize()
         * of the game window. Then buttonRegionDims and cellGridRegionDims
         * are calculated as percentages of those values. HOWEVER, the given
         * window width and height *include* the window decorations (border
         * and titlebar), and the actual usable window area is smaller. If
         * this isn't corrected for, the end-state window will have elements
         * oversized for the area.
         *
         * I didn't find a straightforward way to get the window decoration
         * sizes before the window is made visible. Instead, a call to
         * JFrame.pack() at the end of the constructor is used to resize the
         * window to accommodate the cumulative colummn widths and row heights.
         */
        Dimension screenDims = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowDims = new Dimension((int) Math.floor(0.9D * (double) screenDims.getWidth()),
                                             (int) Math.floor(0.9D * (double) screenDims.getHeight()));

        setContentPane(gamePane);
        setSize(windowDims);

        Dimension buttonRegionDims = new Dimension((int) (windowDims.getWidth() * 0.2D),
                                                   (int) Math.floor(0.1D * windowDims.getHeight()));
        Dimension cellGridRegionDims = new Dimension((int) windowDims.getWidth(),
                                                     (int) (windowDims.getHeight() - buttonRegionDims.getHeight()));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gameLayout.columnWidths = new int[] { (int) buttonRegionDims.getWidth(),
                                              (int) buttonRegionDims.getWidth(),
                                              (int) buttonRegionDims.getWidth(),
                                              (int) buttonRegionDims.getWidth(),
                                              (int) buttonRegionDims.getWidth()};
        gameLayout.rowHeights = new int[] { (int) cellGridRegionDims.getHeight(),
                                            (int) buttonRegionDims.getHeight() };

        GridBagConstraints cellGridConstraints = buildCellGridConstraints(0, 0, 1, 5, cellGridRegionDims);
        Dimension cellGridDims = new Dimension((int) cellGridRegionDims.getWidth() - cellGridConstraints.insets.left - cellGridConstraints.insets.right,
                                               (int) cellGridRegionDims.getHeight() - cellGridConstraints.insets.top - cellGridConstraints.insets.bottom);
        cellGrid = new CellGrid(cellGridDims);
        cellGrid.addMouseListener(cellGrid);
        gamePane.add(cellGrid, cellGridConstraints);

        GridBagConstraints startButtonGridConstraints = buildButtonConstraints(1, 4, 1, 1, buttonRegionDims);
        JButton startButton = buildStartButton();
        gamePane.add(startButton, startButtonGridConstraints);

        GridBagConstraints clearButtonGridConstraints = buildButtonConstraints(1, 2, 1, 1, buttonRegionDims);
        JButton clearButton = buildClearButton(startButton);
        gamePane.add(clearButton, clearButtonGridConstraints);

        GridBagConstraints seedButtonGridConstraints = buildButtonConstraints(1, 3, 1, 1, buttonRegionDims);
        JButton seedButton = buildSeedButton();
        gamePane.add(seedButton, seedButtonGridConstraints);

        validate();
        pack();
        System.out.println(windowDims.getWidth() + " x " + windowDims.getHeight());
        Dimension newWindowDims = getSize();
        System.out.println(newWindowDims.getWidth() + " x " + newWindowDims.getHeight());
    }

    private GridBagConstraints buildConstraints(int row, int col, int rowspan, int colspan) {
        GridBagConstraints gameConstraints = new GridBagConstraints();
        gameConstraints.fill = GridBagConstraints.BOTH;
        gameConstraints.gridy = row;
        gameConstraints.gridx = col;
        gameConstraints.gridheight = rowspan;
        gameConstraints.gridwidth = colspan;
        return gameConstraints;
    }

    private GridBagConstraints buildCellGridConstraints(int row, int col, int rowspan, int colspan, Dimension cellGridRegionDims) {
        GridBagConstraints gameConstraints;
        int widthExtra = (int) (cellGridRegionDims.getWidth() % 10D);
        int heightExtra = (int) (cellGridRegionDims.getHeight() % 10D);
        int topInset = 20 + (int) Math.floor((double) heightExtra / 2.0);
        int bottomInset = 0 + (int) Math.ceil((double) heightExtra / 2.0);
        int leftInset = 20 + (int) Math.floor((double) widthExtra / 2.0);
        int rightInset = 20 + (int) Math.ceil((double) widthExtra / 2.0);
        System.out.println(topInset + ", " + leftInset + ", " + bottomInset + ", " + rightInset);
        System.out.println((cellGridRegionDims.getWidth() - leftInset - rightInset) + ", " + (cellGridRegionDims.getHeight() - topInset - bottomInset));
        gameConstraints = buildConstraints(row, col, rowspan, colspan);
        gameConstraints.insets = new Insets(topInset, leftInset, bottomInset, rightInset);
        return gameConstraints;
    }

    private GridBagConstraints buildButtonConstraints(int row, int col, int rowspan, int colspan, Dimension buttonRegionDims) {
        GridBagConstraints gameConstraints;
        int widthFifth = (int) Math.floor((double) buttonRegionDims.getWidth() / 5.0D);
        int heightFifth = (int) Math.floor((double) buttonRegionDims.getHeight() / 5.0D);
        gameConstraints = buildConstraints(row, col, rowspan, colspan);
        gameConstraints.insets = new Insets(heightFifth, widthFifth, heightFifth, widthFifth);
        return gameConstraints;
    }

    private JButton buildStartButton() {
        JButton button = new JButton("Start");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (button.getText() == "Start") {
                    cellGrid.startCellularAutomata();
                    button.setText("Stop");
                } else {
                    cellGrid.stopCellularAutomata();
                    button.setText("Start");
                }
                cellGrid.repaint();
            }
        });
        return button;
    }

    private JButton buildSeedButton() {
        JButton button = new JButton("Seed");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cellGrid.seedCellGrid();
                cellGrid.repaint();
            }
        });
        return button;
    }

    private JButton buildClearButton(JButton startButton) {
        JButton button = new JButton("Clear");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startButton.getText() == "Stop") {
                    startButton.doClick();
                }
                cellGrid.clearCellGrid();
                cellGrid.repaint();
            }
        });
        return button;
    }

    public static void main(String[] args) {
        GameOfLife game = new GameOfLife();
        game.setVisible(true);
        game.setLocationRelativeTo(null);
    }
}
