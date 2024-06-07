package proyecto;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.*;

public class Snake implements Notifiable {

    private Thread thread = null;
    private JPanel panel;
    // private long[][] board = new long[20][20];
    private ArrayList<Vector<Integer>> snake = new ArrayList<>();
    private ArrayList<Vector<Integer>> apples = new ArrayList<>();

    private int dirX = 1;
    private int dirY = 0;
    private int snakeLength = 1;
    private int snakeX = 5;
    private int snakeY = 5;
    private boolean alive = true;

    public JPanel getPanel() {
        return panel;
    }

    public Snake() {
        Notifier.register(this);
        panel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(120 * 2, 120 * 2);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
                Color[] pixels = new Color[20 * 20];
                for (int i = 0; i < 20 * 20; i++) {
                    int x = i % 20;
                    int y = i / 20;
                    // fill board with color
                    pixels[i] = new Color(255, 100, 255);
                    // pixels[i] = board[x][y] > 0 ? new Color(0, 0, 0)
                    // : board[x][y] == 0 ? new Color(255, 100, 255) : new Color(0, 255, 0);
                }
                for (Vector<Integer> v : snake) {
                    pixels[v.get(0) + v.get(1) * 20] = new Color(0, 255, 0);
                }
                for (Vector<Integer> v : apples) {
                    pixels[v.get(0) + v.get(1) * 20] = new Color(255, 0, 0);
                }
                for (int i = 0; i < 20 * 20; i++) {
                    img.setRGB(i % 20, i / 20, pixels[i].getRGB());
                }

                g.drawImage(img, 0, 0, 120 * 2, 120 * 2, null);
                if (!alive) {
                    g.setColor(Color.RED);
                    g.drawString("Game Over", 70, 70);
                }
            }
        };
        panel.setFocusable(true);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    dirX = 0;
                    dirY = -1;
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    dirX = 0;
                    dirY = 1;
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    dirX = -1;
                    dirY = 0;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    dirX = 1;
                    dirY = 0;
                }
                return false;
            }
        });
    }

    public void acceptEvent(String type, String value) {
        if (type.equals("start")) {
            if (value.equals("snake")) {
                System.out.println("Starting snake");
                start();
            } else {
                if (thread != null) {
                    thread.interrupt();
                }
                panel.setVisible(false);
            }
        }
    }

    private void start() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        snakeLength = 1;
        snakeX = 5;
        snakeY = 5;
        alive = true;
        snake.clear();
        apples.clear();

        thread = new Thread(this::run);
        thread.start();
        panel.setVisible(true);

    }

    public void run() {

        while (true) {
            if (!alive) {
                continue;
            }
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            snakeX += dirX;
            snakeY += dirY;
            Vector<Integer> currPos = new Vector<Integer>()
            {
                {
                    add(snakeX);
                    add(snakeY);
                }
            };

            if (snakeX < 0 || snakeX >= 20 || snakeY < 0 || snakeY >= 20) {
                break;
            }
            if (apples.contains(currPos)) {
                snakeLength++;
                apples.remove(currPos);
            }
            if (snake.contains(currPos)) {
                break;
            }
            snake.add(0, currPos);
            if (snake.size() > snakeLength) {
                snake.remove(snake.size() - 1);
            }

            if (Math.random() < 0.025) {
                int x = (int) (Math.random() * 20);
                int y = (int) (Math.random() * 20);
                Vector<Integer> apple = new Vector<Integer>()
                {
                    {
                        add(x);
                        add(y);
                    }
                };
                if (!snake.contains(apple) && !apples.contains(apple)) {
                    apples.add(apple);
                }
            }
            panel.repaint();
        }
        System.out.println("Game over");
        alive = false;
        panel.repaint();

        // panel.setVisible(false);

    }

}
