
package proyecto;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.RandomAccessFile;
import java.net.URI;

import javax.swing.*;
import javax.swing.border.Border;
import java.nio.channels.SocketChannel;
// flatlaf
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.json.Json;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.google.common.eventbus.Subscribe;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.jsoniter.JsonIterator;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public class App {
    private static int[] arr = new int[92160];
    private static double[] audio = new double[352];
    private static WebSocketClient client = null;
    private static AudioPlayer player = new AudioPlayer();

    public static void main(String[] args) {

        // create jframe
        JFrame frame = new JFrame("game maybe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        frame.setBackground(Color.decode("#fff"));
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        JToolBar toolbar = new JToolBar();
        toolbar.setBackground(Color.decode("#F6F6F8"));

        // toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setFloatable(false);
        toolbar.setVisible(true);
        JLabel label = new JLabel("GameApp");
        toolbar.add(label);
        // toolbar.addSeparator();
        toolbar.add(Box.createHorizontalGlue());

        JButton button = new JButton("􀁌  ");

        button.addActionListener(e -> {
            System.out.println("click");
            JPanel panel = new JPanel();
            panel.add(new JLabel("Modal Content"), BorderLayout.CENTER);
            JComboBox<String> comboBox = new JComboBox<String>();
            comboBox.addItem("zelda");
            comboBox.addItem("tetris");
            comboBox.addItem("mario");

            panel.add(comboBox, BorderLayout.CENTER);
            JButton submit = new JButton("Submit");
            submit.addActionListener(e2 -> {
                System.out.println("submit");
                System.out.println(comboBox.getSelectedItem());
                client.send("{\"event\": \"loadRom\", \"rom\": \"" + comboBox.getSelectedItem() + "\"}");
            });
            panel.add(submit, BorderLayout.CENTER);

            Modal.getModal(frame)
                    .setContent(panel)
                    .setTitleTo("Modal Title")
                    .bake();

        }

        );
        toolbar.add(button, BorderLayout.EAST);
        JButton button2 = new JButton("􀊊  ");
        button2.addActionListener(e -> {
            System.out.println("click");
            JPanel panel = new JPanel();
            panel.add(new JLabel("Modal Content 2"), BorderLayout.CENTER);

            Modal.getModal(frame)
                    .setContent(panel)
                    .setTitleTo("Modal Title 2")
                    .bake();

        }

        );
        toolbar.add(button2, BorderLayout.EAST);
        frame.add(toolbar, BorderLayout.PAGE_START);

        // card layout
        JPanel cards = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                // width = 160, height = 144
                BufferedImage img = new BufferedImage(160 * 4, 144, BufferedImage.TYPE_INT_RGB);

                for (int i = 0; i < arr.length; i++) {
                    Color rgb = new Color(arr[i], arr[i], arr[i]);
                    img.setRGB(i % (160 * 4), i / (160 * 4), rgb.getRGB());
                }
                g.drawImage(img, 0, 0, 160 * 2, 144 * 2, null);

            }
        };
        // listen to keyboard events
        cards.setFocusable(true);
        boolean[] keys = new boolean[256];
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

        cards.setVisible(true);
        frame.add(cards, BorderLayout.CENTER);
        // add cards
        frame.setVisible(true);

        // connect to node socket on /tmp/gameboy.sock
        try {
            client = new WebSocketClient(new URI("ws://localhost:9002")) {
                public void onMessage(String message) {
                    //String[] parts = message.split("\n");
                    arr = JsonIterator.deserialize(message, int[].class);
                    //audio = JsonIterator.deserialize(parts[1], double[].class);
                    // play audio

                    cards.repaint();
                    //player.handleIncomingAudioData(audio);

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

    static class Modal extends JDialog {
        private static Modal inst = null;

        private Modal(JFrame frame, String title) {
            super(frame, title, true);
            setLayout(new BorderLayout());
            setSize(400, 300);
            setLocationRelativeTo(frame);
            setResizable(false);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }

        public Modal bake() {
            setVisible(true);
            return inst;
        }

        public Modal setTitleTo(String title) {
            super.setTitle(title);
            return inst;
        }

        public Modal setContent(JPanel panel) {
            // clear content
            getContentPane().removeAll();
            getContentPane().add(panel, BorderLayout.CENTER);
            return inst;
        }

        public static Modal getModal(JFrame frame) {
            return inst = (inst != null ? inst : new Modal(frame, "Default Title"));
        }

    }


}