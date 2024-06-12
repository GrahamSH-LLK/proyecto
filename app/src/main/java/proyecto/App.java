
package proyecto;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class App {
    private static Gameboy gameboy = new Gameboy();

    private static Pong pong = new Pong();

    private static Snake snake = new Snake();
    private static String currGame = "gameboy";

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
            JPanel panel = new JPanel();
            panel.add(new JLabel("Modal Content"), BorderLayout.CENTER);
            JComboBox<String> modes = new JComboBox<String>();
            modes.addItem("Gameboy");
            modes.addItem("Pong");
            modes.addItem("Snake");
            panel.add(modes, BorderLayout.CENTER);
            JComboBox<String> comboBox = new JComboBox<String>();
            
            modes.addActionListener(modesEvent -> {
                if (modes.getSelectedItem().toString().equals("Gameboy")) {
                    comboBox.setVisible(true);
                    comboBox.removeAllItems();
                    comboBox.addItem("tetris");
                    comboBox.addItem("mario");
                    comboBox.addItem("pocketlove");

                    comboBox.addItem("zelda");
                    comboBox.addItem("2048");
                }  else if (modes.getSelectedItem().toString().equals("Snake")) {
                    comboBox.setVisible(true);
                    comboBox.removeAllItems();
                    comboBox.addItem("Peaceful");
                    comboBox.addItem("Normal");
                } else {
                    comboBox.setVisible(false);
                    comboBox.removeAllItems();
                }
            });

            panel.add(comboBox, BorderLayout.CENTER);
            JButton submit = new JButton("Submit");
            submit.addActionListener(e2 -> {
                Notifier.emit("start", modes.getSelectedItem().toString().toLowerCase());
                if (modes.getSelectedItem().toString().equals("Snake")) {
                    Notifier.emit("peaceful", comboBox.getSelectedItem().toString());
                } else if (modes.getSelectedItem().toString().equals("Gameboy")) {
                    Notifier.emit("loadRom", comboBox.getSelectedItem().toString());
                }

                Modal.getModal(frame).close();
                currGame = modes.getSelectedItem().toString();
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
        toolbar.add(button2, BorderLayout.EAST);
        button2.addActionListener(e2-> {
            Notifier.emit("start",currGame.toLowerCase());
        });
        frame.add(toolbar, BorderLayout.PAGE_START);
        final JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        center.add(gameboy.getPanel());
        center.add(pong.getPanel());
        center.add(snake.getPanel());


        frame.add(center, BorderLayout.CENTER);

        pong.getPanel().setVisible(false);
        snake.getPanel().setVisible(false);
        // add cards
        /*var colorchooser = new JColorChooser();
        colorchooser.setPreviewPanel(new JPanel());
        JColorChooser.createDialog(frame, "Choose Color", true, colorchooser, null, null).setVisible(true);
        //var tcc =  JColorChooser.showDialog(center, currGame, Color.WHITE);*/
      
        
        frame.setVisible(true);
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
        public Modal close() {
            setVisible(false);
            return inst;
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