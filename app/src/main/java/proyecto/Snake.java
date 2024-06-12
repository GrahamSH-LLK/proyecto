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
    // private long[][] board = new long[240][240];
    private ArrayList<Vector<Integer>> snake = new ArrayList<>();
    private ArrayList<Integer> snakeBulges = new ArrayList<>();
    private ArrayList<Vector<Integer>> apples = new ArrayList<>();

    private int dirX = 1;
    private int dirY = 0;
    private int snakeLength = 10;
    private int snakeX = 5;
    private int snakeY = 5;
    private boolean alive = true;
    private Color primaryColor = Color.BLUE;

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

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, primaryColor, 0, 10, primaryColor.brighter());

                for (int i = snake.size() - 1; i >= 0; i--) {
                    Vector<Integer> v = snake.get(i);
                    if (i == 0) {
                        g.setColor(primaryColor.darker());
                    } else {
                        g2d.setPaint(gp);
                    }
                    

                    g.fillArc(v.get(0), v.get(1), 10, 10, 0, 360);
                    if (i == 0) {
                        g.setColor(Color.WHITE);
                        g.drawArc(v.get(0) + 5 + 3 * dirX, v.get(1) + 5 + 3 * dirY, 2, 2, 0, 360);
                    }
                }
                for (Integer v : snakeBulges) {
                    g.setColor(primaryColor);
                    g.fillArc(snake.get(v).get(0) - 3, snake.get(v).get(1) - 3, 16, 16, 0, 360);
                }

                for (Vector<Integer> v : apples) {
                    g.setColor(Color.RED);
                    Image image = new ImageIcon("src/main/java/proyecto/image.png").getImage();
                    g.drawImage(image, v.get(0) - 1, v.get(1) - 1, 12, 12, null);
                }

                // g.drawImage(img, 0, 0, 240 * 2, 240 * 2, null);
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
                int oldDirX = dirX;
                int oldDirY = dirY;
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
                if (dirX == -oldDirX && dirY == -oldDirY) {
                    dirX = oldDirX;
                    dirY = oldDirY;
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
        } else if (type.equals("peaceful")) {
            peaceful = value.equals("Peaceful");
        }
    }

    private void start() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        snakeLength = 10;
        snakeX = 40;
        snakeY = 40;
        alive = true;
        snake.clear();
        apples.clear();
        dirX = 1;
        dirY = 0;
        primaryColor = JColorChooser.showDialog(panel, "Choose snake color", Color.BLUE);
        if (primaryColor == null) {
            primaryColor = Color.BLUE;
        }
        thread = new Thread(this::run);
        thread.start();
        panel.setVisible(true);

    }
    private boolean peaceful = true;
    public void run() {
        int frame = 0;
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!alive) {
                continue;
            }
            snakeX += dirX;
            snakeY += dirY;
            Vector<Integer> currPos = new Vector<Integer>() {
                {
                    add(snakeX);
                    add(snakeY);
                }
            };
            
            if (snakeX < 0 || snakeX >= 240 || snakeY < 0 || snakeY >= 240) {
                if (!peaceful) {
                    break;
                } else {
                    snakeX = (snakeX + 240) % 240;
                    snakeY = (snakeY + 240) % 240;
                }

            }
            for (Vector<Integer> apple : apples) {
                int x = apple.get(0);
                int y = apple.get(1);
                if (x + 10 > snakeX && x < snakeX + 10 && y + 10 > snakeY && y < snakeY + 10) {
                    snakeLength += 5;
                    apples.remove(apple);
                    snakeBulges.add(0, 0);
                    break;
                }
            }
                for (int i = snakeBulges.size() - 1; i >= 0; i--) {
                    if (snakeBulges.get(i) >= snake.size() - 2) {
                        snakeBulges.remove(i);
                    } else {
                        snakeBulges.set(i, snakeBulges.get(i) + 2);
                    }
                }
            
            if (snake.contains(currPos)) {
                break;
            }
            snake.add(0, currPos);
            if (snake.size() > snakeLength) {
                snake.remove(snake.size() - 1);
            }

            if (Math.random() < 0.0025) {
                int x = (int) (Math.random() * 240);
                int y = (int) (Math.random() * 240);
                Vector<Integer> apple = new Vector<Integer>() {
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
