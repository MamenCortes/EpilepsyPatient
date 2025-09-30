package ceu.biolab.bouncingBall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

public class SwingThread2 {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frame = new FrameRebotar();
                frame.setVisible(true);
            }
        });
    }
}

class FrameRebotar extends JFrame {

    List<Ball2> balls = new LinkedList<Ball2>();
    private JPanel panelForDrawing;

    public FrameRebotar() {
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = getContentPane();
        panelForDrawing = new JPanel();
        contentPane.add(panelForDrawing, BorderLayout.CENTER);
        JPanel southPanel = new JPanel();
        JButton launchButton = new JButton("Launch");
        southPanel.add(launchButton);
        launchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Ball2 pelotita = new Ball2(panelForDrawing, Color.black);
                balls.add(pelotita);
                pelotita.start();
            }
        });
        JButton botonDetener = new JButton("Stop");
        southPanel.add(botonDetener);
        botonDetener.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                for (Ball2 ball : balls) {
                    ball.parar();
                }
            }
        });
        contentPane.add(southPanel, BorderLayout.SOUTH);
    }
}

class Ball2 extends Thread {

    private JPanel panel;
    private final int ballXSize = 10;
    private final int ballYSize = 10;
    private int x = 0;
    private int y = 0;
    private int xDelta = 2;
    private int yDelta = 2;
    private Color color;
    private boolean stop = false;

    public Ball2(JPanel panel, Color color) {
        this.panel = panel;
        this.color = color;
    }

    public void move() {
        Graphics2D g2d = (Graphics2D) panel.getGraphics();
        g2d.setColor(panel.getBackground());
        //We paint on the old coordinates, thus erasing the ball,
        g2d.fillOval(x, y, ballXSize, ballYSize);
        //We calculate the new coordinates
        x += xDelta;
        y += yDelta;
        Dimension d = panel.getSize();
        //If the coordinate x is less than 0 we reverse the increase in the x coordinate
        if (x < 0) {
            x = 0;
            xDelta = -xDelta;
        }
        //If the ball is out of the panel by the right we do that "bounce" leftward
        if (x + ballXSize >= d.width) {
            x = d.width - ballXSize;
            xDelta = -xDelta;
        }
        //If the y coordinate is less than 0 we reverse the increase in the y coordinate
        if (y < 0) {
            y = 0;
            yDelta = -yDelta;
        }
        //If the ball is out of the panel bottom do that "bounce" up
        if (y + ballYSize >= d.height) {
            y = d.height - ballYSize;
            yDelta = -yDelta;
        }
        g2d.setColor(color);
        g2d.fillOval(x, y, ballXSize, ballYSize);
        g2d.dispose();
    }

    @Override
    public void run() {
        for (int i = 1; (i <= 100000 && !stop); i++) {

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    move();
                }
            });
            try {
                sleep(5);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void parar() {
        stop = true;
    }
}
