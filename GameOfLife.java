package jgameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOfLife extends JFrame {

    GridBagLayout gameLayout = new GridBagLayout();
    JPanel gamePane = new JPanel(gameLayout);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    Canvas canvas = new Canvas();

    public GameOfLife() {
        super("Conway's Game of Life");

        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        int reducedWidth = (int) Math.floor(0.8D * (double) screenWidth);
        int reducedHeight = (int) Math.floor(0.8D * (double) screenHeight);
        int widthFifth = (int) ((float) reducedWidth * 0.2F);
        int heightTenth = (int) Math.floor(0.1D * (double) reducedHeight);
        int heightNineTenths = reducedHeight - heightTenth;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gameLayout.columnWidths = new int[] { widthFifth, widthFifth, widthFifth, widthFifth, widthFifth };
        gameLayout.rowHeights = new int[] { heightNineTenths, heightTenth };

        gamePane.add(canvas, buildCanvasConstraints(0, 0, 1, 5, reducedWidth, heightNineTenths));
        gamePane.add(buildStartButton(), buildButtonConstraints(1, 4, 1, 1, widthFifth, heightTenth));

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
        int topInset = 30 + (int) Math.floor((double) widthExtra / 2.0);
        int bottomInset = 0 + (int) Math.ceil((double) widthExtra / 2.0);
        int leftInset = 30 + (int) Math.floor((double) heightExtra / 2.0);
        int rightInset = 30 + (int) Math.ceil((double) heightExtra / 2.0);
        gameConstraints = buildConstraints(row, col, rowspan, colspan);
        gameConstraints.insets = new Insets(topInset, leftInset, bottomInset, rightInset);
        return gameConstraints;
    }

    private GridBagConstraints buildButtonConstraints(int row, int col, int rowspan, int colspan, int containerWidth, int containerHeight) {
        GridBagConstraints gameConstraints;
        int widthThird = (int) ((float) containerWidth / 3.0F);
        int heightThird = (int) ((float) containerHeight / 3.0F);
        int topInset = heightThird;
        int leftInset = widthThird * 2;
        int bottomInset = (int) ((float) heightThird * 1.5F);
        int rightInset = (int) ((float) widthThird * 0.75F);
        gameConstraints = buildConstraints(row, col, rowspan, colspan);
        gameConstraints.insets = new Insets(topInset, leftInset, bottomInset, rightInset);
        return gameConstraints;
    }

    private JButton buildStartButton() {
        JButton button = new JButton("Start");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button.setText(button.getText() == "Start" ? "Stop" : "Start");
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
