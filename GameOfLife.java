package jgameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOfLife extends JFrame {

    GridBagLayout gameLayout = new GridBagLayout();
    JPanel gamePane = new JPanel(gameLayout);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Canvas canvas;

    public GameOfLife() {
        super("Conway's Game of Life");

        JButton startButton;
        int reducedWidth = (int) Math.floor(0.8D * (double) screenSize.getWidth());
        int reducedHeight = (int) Math.floor(0.8D * (double) screenSize.getHeight());
        int widthFifth = (int) ((float) reducedWidth * 0.2F);
        int heightTenth = (int) Math.floor(0.1D * (double) reducedHeight);
        int heightNineTenths = reducedHeight - heightTenth;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gameLayout.columnWidths = new int[] { widthFifth, widthFifth, widthFifth, widthFifth, widthFifth };
        gameLayout.rowHeights = new int[] { heightNineTenths, heightTenth };

        GridBagConstraints canvasConstraints = buildCanvasConstraints(0, 0, 1, 5, reducedWidth, heightNineTenths);
        int modifiedHeight = reducedHeight - canvasConstraints.insets.top - canvasConstraints.insets.bottom;
        int modifiedWidth = reducedWidth - canvasConstraints.insets.left - canvasConstraints.insets.right;
        canvas = new Canvas(modifiedWidth, modifiedHeight);
        gamePane.add(canvas, buildCanvasConstraints(0, 0, 1, 5, reducedWidth, heightNineTenths));

        startButton = buildStartButton();

        gamePane.add(buildClearButton(startButton), buildButtonConstraints(1, 2, 1, 1, widthFifth, heightTenth));
        gamePane.add(buildSeedButton(), buildButtonConstraints(1, 3, 1, 1, widthFifth, heightTenth));
        gamePane.add(startButton, buildButtonConstraints(1, 4, 1, 1, widthFifth, heightTenth));

        setContentPane(gamePane);
        setSize(reducedWidth, reducedHeight);
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

    private GridBagConstraints buildCanvasConstraints(int row, int col, int rowspan, int colspan, int containerWidth, int containerHeight) {
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
        int widthThird = (int) ((float) containerWidth / 3.0F);
        int heightThird = (int) ((float) containerHeight / 3.0F);
        gameConstraints = buildConstraints(row, col, rowspan, colspan);
        gameConstraints.insets = new Insets(heightThird, widthThird, heightThird, widthThird);
        return gameConstraints;
    }

    private JButton buildStartButton() {
        JButton button = new JButton("Start");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (button.getText() == "Start") {
                    canvas.startCellularAutomata();
                    button.setText("Stop");
                } else {
                    canvas.stopCellularAutomata();
                    button.setText("Start");
                }
                canvas.repaint();
            }
        });
        return button;
    }

    private JButton buildSeedButton() {
        JButton button = new JButton("Seed");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.seedCellGrid();
                canvas.repaint();
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
                canvas.clearCellGrid();
                canvas.repaint();
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
