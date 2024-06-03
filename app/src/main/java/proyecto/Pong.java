package proyecto;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Pong implements Notifiable{
    private static String currentRom = null;
    private static int currX = 0;
    private static int ballX = 0;
    private static int ballY = 0;
    private static double ballDX = 1;
    private static double ballDY = 1;
    private Thread thread;
    private static boolean alive = true;
    private JPanel panel;
    private static int score = 0;

    public void setRom(String rom) {
        currentRom = rom;
    }

    public JPanel getPanel() {
        return panel;
    }

    public Pong() {
        Notifier.register(this);
        System.out.println("Pong instance created");

        panel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(160 * 2, 140 * 2 + 10);
            }

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                // width = 160, height = 144
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, 160 * 2, 144 * 2 +10);
                g.setColor(Color.WHITE);
                g.fillRect(Math.min(currX, 160 * 2 - 40), 144 * 2 - 10, 40, 10);
                g.fillOval(ballX, ballY, 10, 10);
                if (!alive) {
                    g.setColor(Color.RED);
                    g.drawString("Game Over", 160, 144);
                }
                g.setColor(Color.WHITE);
                g.drawString("Score: " + score, 5, 20);
            }
        };
        panel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // stub
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                currX = e.getX();
                panel.repaint();
            }
        });

    }

    public void start() {
        currX = 0;
        ballX = 0;
        ballY = 0;
        ballDX = 1;
        ballDY = 1;
        alive = true;
        // kill previous thread
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        } else {
            thread = new Thread(this::updateBall);
            thread.start();
        }
        panel.setVisible(true);

    }

    private void updateBall() {
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!alive)
                continue;
            ballX += ballDX;
            ballY += ballDY;
            if (ballX < 0 || ballX > 160 * 2) {
                ballDX = -ballDX;
            }
            if (ballY < 0 || ballY > 144 * 2) {
                ballDY = -ballDY;
            }
            if (ballY + 10 > 144 * 2 - 10 && ballX + 10 > currX && ballX - 10 < currX + 40) {
                ballDY = -ballDY; //+ .03;
                ballDX = ballDX;// + .03;
                score++;
            }
            if (ballY > 144 * 2) {
                alive = false;
            }

            panel.repaint();
        }
    }
    public void acceptEvent(String type, String value) {
        if (type.equals("start") ) {
            if (value.equals("pong")) {
                start();
            } else {
                // stop
                if (thread != null) {
                    thread.interrupt();
                }
                panel.setVisible(false);
            }
        } 
    }
}
