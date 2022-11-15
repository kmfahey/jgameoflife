package com.kmfahey.jgameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class implements the GUI frontend that provides an interface to this
 * program for the user. Every GUI element is defined in its constructor with
 * the exception of the JComponent subclass CellGrid which implements the
 * viewable area where the cellular automata run.
 *
 * @see com.kmfahey.jgameoflife.CellGrid
 * @see javax.swing.JFrame
 */
public class GameOfLife extends JFrame {

    /** This CellGrid object, a JComponent subclass, that implements the
        viewable area where the cellular automata runs. */
    private CellGrid cellGrid;

    /**
     * This method initializes the GameOfLife object that implements the GUI
     * used by this program. It calculates all the dimensions used by the
     * GridBagLayout object that defines the GUI's layout, instances the
     * JComponent subclass CellGrid &amp; the JButton objects, and attaches them
     * to the JPanel that holds the GUI elements.
     *
     * @see java.awt.GridBagLayout
     * @see javax.swing.JFrame
     */
    public GameOfLife() {
        super("Conway's Game of Life");

        /* Basic setup of this JFrame subclass. */
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        /* These calculations are misleading. windowDims width and height
           are derived from the screen's dimensions, and used to setSize()
           of the game window. Then buttonRegionDims and cellGridRegionDims
           are calculated as percentages of those values. HOWEVER, the given
           window width and height *include* the window decorations (border
           and titlebar), and the actual usable window area is smaller. If
           this isn't corrected for, the end-state window will have elements
           oversized for the area.
          
           I didn't find a straightforward way to get the window decoration
           sizes before the window is made visible. Instead, a call to
           JFrame.pack() at the end of the constructor is used to resize the
           window to accommodate the cumulative colummn widths and row heights. */
        Dimension screenDims = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowDims = new Dimension((int) Math.floor(0.9D * (double) screenDims.getWidth()),
                                             (int) Math.floor(0.9D * (double) screenDims.getHeight()));

        /* This instances the layout and panel objects that define the GUI and
           sets the dimension of the panel. */
        GridBagLayout gameLayout = new GridBagLayout();
        JPanel gamePanel = new JPanel(gameLayout);
        setContentPane(gamePanel);
        setSize(windowDims);

        /* These calculations derive the dimensions of the button-region (the
           area of the GridBagLayout where one button is located) and the
           dimensions of the cell-grid-region (the area of the GridBagLayout
           where the viewable area that display the cellular automata is
           located) in terms of proportions of the (slightly incorrect)
           dimensions of the GUI window. */
        Dimension buttonRegionDims = new Dimension((int) (windowDims.getWidth() * 0.2D),
                                                   (int) Math.floor(0.1D * windowDims.getHeight()));
        Dimension cellGridRegionDims = new Dimension((int) windowDims.getWidth(),
                                                     (int) (windowDims.getHeight() - buttonRegionDims.getHeight()));

        /* These arrays define the grid element widths and heights that the
           GridBagLayout object uses to define the cells where GUI elements are
           located. */
        gameLayout.columnWidths = new int[] { (int) buttonRegionDims.getWidth(),
                                              (int) buttonRegionDims.getWidth(),
                                              (int) buttonRegionDims.getWidth(),
                                              (int) buttonRegionDims.getWidth(),
                                              (int) buttonRegionDims.getWidth()};
        gameLayout.rowHeights = new int[] { (int) cellGridRegionDims.getHeight(),
                                            (int) buttonRegionDims.getHeight() };

        
        /* This code defines the GridBagConstraints for the cell grid, then
           extracts the insets that buildCellGridConstraints defined, uses
           them to calculate the width &amp; height of the actual cell grid,
           and passes those values in a Dimensions object to the CellGrid
           constructor. It adds cellGrid as a mouse listener to itself and
           attaches the cell grid with its constraints to the JPanel object.  */
        GridBagConstraints cellGridConstraints = buildCellGridConstraints(0, 0, 1, 5, cellGridRegionDims);
        Dimension cellGridDims = new Dimension((int) cellGridRegionDims.getWidth() - cellGridConstraints.insets.left
                                                                                   - cellGridConstraints.insets.right,
                                               (int) cellGridRegionDims.getHeight() - cellGridConstraints.insets.top
                                                                                    - cellGridConstraints.insets.bottom);
        cellGrid = new CellGrid(cellGridDims);
        cellGrid.addMouseListener(cellGrid);
        gamePanel.add(cellGrid, cellGridConstraints);

        /* These three blocks of code define the GridBagConstraints for each
           button using buildButtonConstraints(), instances the button with the
           appropriate build*Button() method and attaches it and its constraints
           to the JPanel. */
        GridBagConstraints startButtonGridConstraints = buildButtonConstraints(1, 4, 1, 1, buttonRegionDims);
        JButton startButton = buildStartButton();
        gamePanel.add(startButton, startButtonGridConstraints);

        GridBagConstraints clearButtonGridConstraints = buildButtonConstraints(1, 2, 1, 1, buttonRegionDims);
        JButton clearButton = buildClearButton(startButton);
        gamePanel.add(clearButton, clearButtonGridConstraints);

        GridBagConstraints seedButtonGridConstraints = buildButtonConstraints(1, 3, 1, 1, buttonRegionDims);
        JButton seedButton = buildSeedButton();
        gamePanel.add(seedButton, seedButtonGridConstraints);

        /* These steps confirm the layout that I've defined, and pack()
           attends to the error in the dimensions introduced when I used
           Toolkit.getDefaultToolkit().getScreenSize().getHeight() (which
           includes the title bar) as a basis for the layout calculations that I
           used to define the row heights in the GridBagLayout. */
        validate();
        pack();

        //Dimension newWindowDims = getSize();
        //System.out.println(windowDims.getWidth() + " x " + windowDims.getHeight());
        //System.out.println(newWindowDims.getWidth() + " x " + newWindowDims.getHeight());
    }

    /**
     * This method is used by buildCellGridConstraints and
     * buildButtonConstraints to build the basic GridBagConstraints object
     * that those two methods expand upon. It passes all its arguments to the
     * GridBagConstraints constructor.
     * 
     * @param row     The row argument to GridBagConstraints.
     * @param col     The col argument to GridBagConstraints.
     * @param rowspan The rowspan argument to GridBagConstraints.
     * @param colspan The colspan argument to GridBagConstraints.
     * @return        A GridBagConstraints object constructed to order.
     * @see java.awt.GridBagConstraints
     * @see java.awt.GridBagLayout
     * @see java.awt.Insets
     * @see javax.swing.JComponent
     */
    private GridBagConstraints buildConstraints(int row, int col, int rowspan, int colspan) {
        GridBagConstraints gameConstraints = new GridBagConstraints();
        gameConstraints.fill = GridBagConstraints.BOTH;
        gameConstraints.gridy = row;
        gameConstraints.gridx = col;
        gameConstraints.gridheight = rowspan;
        gameConstraints.gridwidth = colspan;
        return gameConstraints;
    }

    /**
     * This method instances a GridBagConstraints object appropriate for the
     * JComponent subclass CellGrid that implements the viewable cellular
     * automata area of the GUI. It sets the grid-bag location, and devises
     * appropriate insets based on the specified button region dimensions.
     *
     * @param row              The row argument to GridBagConstraints.
     * @param col              The col argument to GridBagConstraints.
     * @param rowspan          The rowspan argument to GridBagConstraints.
     * @param colspan          The colspan argument to GridBagConstraints.
     * @param buttonRegionDims A Dimensions object that specifies the width and
     *                         height, in pixels, of the region of the
     *                         GridBagLayout where the button will appear.
     * @return                 A GridBagConstraints object constructed to order.
     * @see java.awt.GridBagConstraints
     * @see java.awt.GridBagLayout
     * @see java.awt.Insets
     * @see javax.swing.JComponent
     */
    private GridBagConstraints buildCellGridConstraints(final int row, final int col,
                                                        final int rowspan, final int colspan,
                                                        final Dimension cellGridRegionDims) {
        final double CELL_WIDTH = 10D;
        final double CELL_HEIGHT = 10D;
        GridBagConstraints gameConstraints;
        int widthExtra = (int) (cellGridRegionDims.getWidth() % CELL_WIDTH);
        int heightExtra = (int) (cellGridRegionDims.getHeight() % CELL_HEIGHT);
        int topInset = 20 + (int) Math.floor((double) heightExtra / 2.0);
        int bottomInset = 0 + (int) Math.ceil((double) heightExtra / 2.0);
        int leftInset = 20 + (int) Math.floor((double) widthExtra / 2.0);
        int rightInset = 20 + (int) Math.ceil((double) widthExtra / 2.0);
        //System.out.println(topInset + ", " + leftInset + ", " + bottomInset + ", " + rightInset);
        /*System.out.println((cellGridRegionDims.getWidth() - leftInset - rightInset) + ", " +
                             (cellGridRegionDims.getHeight() - topInset - bottomInset)); */
        gameConstraints = buildConstraints(row, col, rowspan, colspan);
        gameConstraints.insets = new Insets(topInset, leftInset, bottomInset, rightInset);
        return gameConstraints;
    }

    /**
     * This method instances a GridBagConstraints object appropriate for a
     * button in the GUI effected by this class. It sets the grid-bag location,
     * and devises appropriate insets based on the specified button region
     * dimensions.
     *
     * @param row              The row argument to GridBagConstraints.
     * @param col              The col argument to GridBagConstraints.
     * @param rowspan          The rowspan argument to GridBagConstraints.
     * @param colspan          The colspan argument to GridBagConstraints.
     * @param buttonRegionDims A Dimensions object that specifies the width and
     *                         height, in pixels, of the region of the
     *                         GridBagLayout where the button will appear.
     * @return                 A GridBagConstraints object constructed to order.
     * @see java.awt.GridBagConstraints
     * @see java.awt.GridBagLayout
     * @see java.awt.Insets
     * @see javax.swing.JButton
     */
    private GridBagConstraints buildButtonConstraints(final int row, final int col,
                                                      final int rowspan, final int colspan,
                                                      final Dimension buttonRegionDims) {
        GridBagConstraints gameConstraints;
        int widthFifth = (int) Math.floor((double) buttonRegionDims.getWidth() / 5.0D);
        int heightFifth = (int) Math.floor((double) buttonRegionDims.getHeight() / 5.0D);
        gameConstraints = buildConstraints(row, col, rowspan, colspan);
        gameConstraints.insets = new Insets(heightFifth, widthFifth, heightFifth, widthFifth);
        return gameConstraints;
    }

    /** 
     * This method is used to construct a JButton with an actionListener that
     * starts or stops the automata when clicked. The button begins as a "Start"
     * button, with that text, and when clicked it starts the automata. After
     * being clicked it changes its text to "Stop" and when clicked it stops
     * the automata. After that it resets its text to "Start" again, and the
     * behavior continues in that pattern.
     *
     * @return The JButton constructed by the method.
     */
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

    /** 
     * This method is used to construct a JButton with an actionListener that
     * populates the CellGrid with random live cells when clicked.
     *
     * @return The JButton constructed by the method.
     */
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

    /**
     * This method is used to construct a JButton with an actionListener that
     * clears the CellGrid object's viewable area when clicked. It accepts the
     * start button as an argument so that, if the automata is running, it can
     * also click that button and end the animation.
     *
     * @param startButton The existing JButton that implements the Start/Stop
     *                    functions.
     * @return            The JButton constructed by the method. */
    private JButton buildClearButton(final JButton startButton) {
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

    /**
     * This method is called when the class is run as the frontend to the
     * program. It instances the class into a GameOfLife object and sets some
     * instance variables appropriately. The rest of the execution occurs when
     * the user interacts with the GUI this class implements.
     *
     * @param args The argument string array composed from the commandline
     *             arguments, if any.
     */
    public static void main(String[] args) {
        GameOfLife game = new GameOfLife();
        game.setVisible(true);
        game.setLocationRelativeTo(null);
    }
}
