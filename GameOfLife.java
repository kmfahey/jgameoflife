package jgameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOfLife extends JFrame {

    GroupLayout gameLayout;
    JPanel gamePane = new JPanel();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Canvas canvas;

    public GameOfLife() {
        super("Conway's Game of Life");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        int windowWidth = (int) Math.floor(0.9D * (double) screenSize.getWidth());
        int windowHeight = (int) Math.floor(0.9D * (double) screenSize.getHeight());

        gamePane.setSize(windowWidth, windowHeight);

        int buttonRegionWidth = (int) ((float) windowWidth * 0.2F);
        int buttonRegionHeight = (int) Math.floor(0.1D * (double) windowHeight);
        int canvasRegionHeight = windowHeight - buttonRegionHeight;
        int canvasVertPadding = 20 + (int) Math.floor((double) canvasRegionHeight % 10D * 0.5D);
        int canvasHorizPadding = 20 + (int) Math.floor((double) windowWidth % 10D * 0.5D);
        int canvasWidth = windowWidth - canvasHorizPadding * 2;
        int canvasHeight = windowHeight - buttonRegionHeight - canvasVertPadding * 2;
        int buttonHorizPadding = (int) Math.floor((double) buttonRegionWidth * 0.25D);
        int buttonVertPadding = (int) Math.floor((double) buttonRegionHeight * 0.25D);
        int buttonWidth = buttonRegionWidth - 2 * buttonHorizPadding;
        int buttonHeight = buttonRegionHeight - 2 * buttonVertPadding;

        gameLayout = new GroupLayout(gamePane);
        gamePane.setLayout(gameLayout);

        JButton startButton = buildStartButton();
        JButton seedButton = buildSeedButton();
        JButton clearButton = buildClearButton(startButton);
        clearButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        seedButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        startButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        clearButton.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        seedButton.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        startButton.setMaximumSize(new Dimension(buttonWidth, buttonHeight));

        canvas = new Canvas(canvasWidth, canvasHeight);
        canvas.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
        canvas.setMaximumSize(new Dimension(canvasWidth, canvasHeight));

        int PREF_SZ = GroupLayout.PREFERRED_SIZE;

        gameLayout.setHorizontalGroup(gameLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                .addGap(canvasHorizPadding)
                                                .addGroup(gameLayout.createSequentialGroup()
                                                                    .addGap(canvasHorizPadding)
                                                                    .addComponent(canvas, PREF_SZ, PREF_SZ, PREF_SZ))
                                                .addGroup(gameLayout.createSequentialGroup()
                                                                    .addGap(buttonHorizPadding)
                                                                    .addGroup(gameLayout.createSequentialGroup()
                                                                                        .addGap(buttonHorizPadding)
                                                                                        .addComponent(clearButton, PREF_SZ, PREF_SZ, PREF_SZ))
                                                                    .addGroup(gameLayout.createSequentialGroup()
                                                                                        .addGap(buttonHorizPadding)
                                                                                        .addComponent(seedButton, PREF_SZ, PREF_SZ, PREF_SZ))
                                                                    .addGroup(gameLayout.createSequentialGroup()
                                                                                        .addGap(buttonHorizPadding)
                                                                                        .addComponent(startButton, PREF_SZ, PREF_SZ, PREF_SZ))));


        gameLayout.setVerticalGroup(gameLayout.createSequentialGroup()
                                              .addGap(canvasVertPadding)
                                              .addGroup(gameLayout.createSequentialGroup()
                                                                  .addComponent(canvas))
                                                                  .addGap(canvasVertPadding)
                                              .addGroup(gameLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                                  .addGap(buttonVertPadding)
                                                                  .addComponent(clearButton, PREF_SZ, PREF_SZ, PREF_SZ)
                                                                  .addComponent(seedButton, PREF_SZ, PREF_SZ, PREF_SZ)
                                                                  .addComponent(startButton, PREF_SZ, PREF_SZ, PREF_SZ)));

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
