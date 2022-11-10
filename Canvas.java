package jgameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Canvas extends JComponent implements ActionListener {

    Color fieldColor = Color.WHITE;

    protected void paintComponent(Graphics graphics) {
        graphics.setColor(fieldColor);
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("repaint")) {
            System.out.println("Repaint event!");
            repaint();
        }
    }

}
