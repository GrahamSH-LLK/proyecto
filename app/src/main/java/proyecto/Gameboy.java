package proyecto;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.net.URI;

import com.jsoniter.JsonIterator;

public class Gameboy implements Notifiable {
    private WebSocketClient client = null;
    private int[] arr = new int[92160];
    private JPanel panel;

    public void setRom(String rom) {
        client.send("{\"event\": \"loadRom\", \"rom\": \"" + rom + "\"}");
    }

    public JPanel getPanel() {
        return panel;
    }

    public Gameboy() {
        Notifier.register(this);
        panel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(160 * 2, 140 * 2);
            }

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                // width = 160, height = 144
                BufferedImage img = new BufferedImage(160, 144, BufferedImage.TYPE_INT_RGB);
                Color[] pixels = new Color[160 * 144];
                for (int i = 0; i < arr.length; i += 4) {
                    pixels[i / 4] = new Color(arr[i], arr[i + 1], arr[i + 2]);
                }

                for (int i = 0; i < 160 * 144; i++) {
                    img.setRGB(i % (160), i / (160), pixels[i].getRGB());
                }
                g.drawImage(img, 0, 0, 160 * 2, 144 * 2, null);

            }
        };
        // listen to keyboard events and send them to the server
        panel.setFocusable(true);
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher() {

                    @Override
                    public boolean dispatchKeyEvent(KeyEvent e) {
                        if (e.getID() == KeyEvent.KEY_RELEASED) {
                            client.send("{\"event\": \"keyrelease\", \"key\": " + e.getKeyCode() + "}");
                        } else if (e.getID() == KeyEvent.KEY_PRESSED) {
                            client.send("{\"event\": \"keypress\", \"key\": " + e.getKeyCode() + "}");
                        }
                        return false;
                    }
                });

        panel.setVisible(true);

        try {
            client = new WebSocketClient(new URI("ws://localhost:9002")) {
                public void onMessage(String message) {
                    arr = JsonIterator.deserialize(message, int[].class);
                    panel.repaint();
                }

                public void onOpen(ServerHandshake handshake) {
                    System.out.println("opened connection");
                }

                public void onClose(int code, String reason, boolean remote) {
                     System.out.println("closed connection" + reason);
                }

                public void onError(Exception ex) {
                    ex.printStackTrace();
                }

            };
            client.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void acceptEvent(String type, String value) {
        if (type.equals("loadRom")) {
            setRom(value);
        } else if (type.equals("start")) {
            System.out.println("hi" + value);
            if (value.equals("gameboy")) {
                panel.setVisible(true);

            } else {
                panel.setVisible(false);
            }
        }
    }

}
