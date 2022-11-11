package jgameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOfLife extends JFrame {

    GridBagLayout gameLayout = new GridBagLayout();
    JPanel gamePane = new JPanel(gameLayout);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    CellGrid cellGrid;

    public GameOfLife() {
        super("Conway's Game of Life");

        int windowWidth = (int) Math.floor(0.9D * (double) screenSize.getWidth());
        int windowHeight = (int) Math.floor(0.9D * (double) screenSize.getHeight());
        int buttonRegionWidth = (int) ((float) windowWidth * 0.2F);
        int buttonRegionHeight = (int) Math.floor(0.1D * (double) windowHeight);
        int cellGridRegionHeight = windowHeight - buttonRegionHeight;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gameLayout.columnWidths = new int[] { buttonRegionWidth, buttonRegionWidth, buttonRegionWidth, buttonRegionWidth, buttonRegionWidth };
        gameLayout.rowHeights = new int[] { cellGridRegionHeight, buttonRegionHeight };

        GridBagConstraints cellGridConstraints = buildCellGridConstraints(0, 0, 1, 5, windowWidth, cellGridRegionHeight);
        int cellGridHeight = windowHeight - buttonRegionHeight - cellGridConstraints.insets.top - cellGridConstraints.insets.bottom;
        int cellGridWidth = windowWidth - cellGridConstraints.insets.left - cellGridConstraints.insets.right;
        cellGrid = new CellGrid(cellGridWidth, cellGridHeight);
        Dimension cellGridDimensions = new Dimension(cellGridWidth, cellGridHeight);
        cellGrid.setMinimumSize(cellGridDimensions);
        cellGrid.setPreferredSize(cellGridDimensions);
        cellGrid.setMaximumSize(cellGridDimensions);
        gamePane.add(cellGrid, buildCellGridConstraints(0, 0, 1, 5, windowWidth, cellGridRegionHeight));

        GridBagConstraints startButtonGridConstraints = buildButtonConstraints(1, 4, 1, 1, buttonRegionWidth, buttonRegionHeight);
        int buttonHeight = buttonRegionHeight - startButtonGridConstraints.insets.top - startButtonGridConstraints.insets.bottom;
        int buttonWidth = buttonRegionWidth - startButtonGridConstraints.insets.left - startButtonGridConstraints.insets.right;
        Dimension buttonDimensions = new Dimension(buttonWidth, buttonHeight);
        JButton startButton = buildStartButton();
        startButton.setMinimumSize(buttonDimensions);
        startButton.setPreferredSize(buttonDimensions);
        startButton.setMaximumSize(buttonDimensions);
        gamePane.add(startButton, startButtonGridConstraints);

        GridBagConstraints clearButtonGridConstraints = buildButtonConstraints(1, 2, 1, 1, buttonRegionWidth, buttonRegionHeight);
        JButton clearButton = buildClearButton(startButton);
        clearButton.setMinimumSize(buttonDimensions);
        clearButton.setPreferredSize(buttonDimensions);
        clearButton.setMaximumSize(buttonDimensions);
        gamePane.add(clearButton, clearButtonGridConstraints);

        GridBagConstraints seedButtonGridConstraints = buildButtonConstraints(1, 3, 1, 1, buttonRegionWidth, buttonRegionHeight);
        JButton seedButton = buildSeedButton();
        seedButton.setMinimumSize(buttonDimensions);
        seedButton.setPreferredSize(buttonDimensions);
        seedButton.setMaximumSize(buttonDimensions);
        gamePane.add(seedButton, seedButtonGridConstraints);

        setContentPane(gamePane);
        setSize(windowWidth, windowHeight);
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

    private GridBagConstraints buildCellGridConstraints(int row, int col, int rowspan, int colspan, int containerWidth, int containerHeight) {
        GridBagConstraints gameConstraints;
        int widthExtra = containerWidth % 10;
        int heightExtra = containerHeight % 10;
        int topInset = 20 + (int) Math.floor((double) widthExtra / 2.0);
        int bottomInset = 0 + (int) Math.ceil((double) widthExtra / 2.0);
        int leftInset = 20 + (int) Math.floor((double) heightExtra / 2.0);
        int rightInset = 20 + (int) Math.ceil((double) heightExtra / 2.0);
        gameConstraints = buildConstraints(row, col, rowspan, colspan);
        gameConstraints.insets = new Insets(topInset, leftInset, bottomInset, rightInset);
        return gameConstraints;
    }

    private GridBagConstraints buildButtonConstraints(int row, int col, int rowspan, int colspan, int containerWidth, int containerHeight) {
        GridBagConstraints gameConstraints;
        int widthFifth = (int) ((float) containerWidth / 5.0F);
        int heightFifth = (int) ((float) containerHeight / 5.0F);
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
