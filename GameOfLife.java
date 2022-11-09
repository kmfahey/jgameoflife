package jgameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOfLife extends JFrame {

    GridBagLayout gameLayout = new GridBagLayout();
    GridBagConstraints gameConstraints = new GridBagConstraints();
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
        int widthDiff = (int) ((float) reducedWidth - (5F * (float) widthFifth));
        int heightTenth = (int) Math.floor(0.1D * (double) reducedHeight);
        int heightNineTenths = reducedHeight - heightTenth;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gameLayout.columnWidths = new int[] { widthFifth, widthFifth, widthFifth, widthFifth, widthFifth + widthDiff };
        gameLayout.rowHeights = new int[] { heightNineTenths, heightTenth };

        gamePane.add(canvas, buildConstraints(0, 0, 1, 5));
        gamePane.add(buildStartButton(), buildConstraints(1, 4, 1, 1));

        setContentPane(gamePane);
        setSize(reducedWidth, reducedHeight);
    }

    private GridBagConstraints buildConstraints(int row, int col, int rowspan, int colspan) {
        gameConstraints.fill = GridBagConstraints.BOTH;
        gameConstraints.gridy = row;
        gameConstraints.gridx = col;
        gameConstraints.gridheight = rowspan;
        gameConstraints.gridwidth = colspan;
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
