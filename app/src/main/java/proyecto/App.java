
package proyecto;

import java.awt.*;

import javax.swing.*;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class App {
    private static Gameboy gameboy = new Gameboy();

    private static Pong pong = new Pong();

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
            panel.add(modes, BorderLayout.CENTER);
            JComboBox<String> comboBox = new JComboBox<String>();

            modes.addActionListener(modesEvent -> {
                if (modes.getSelectedItem().toString().equals("Gameboy")) {
                    comboBox.removeAllItems();
                    comboBox.addItem("tetris");
                    comboBox.addItem("mario");
                    comboBox.addItem("pocketlove");

                    comboBox.addItem("zelda");
                    comboBox.addItem("2048");
                } else {
                    comboBox.removeAllItems();
                    comboBox.addItem("other");
                    comboBox.addItem("other2");
                    comboBox.addItem("other3");
                }
            });

            panel.add(comboBox, BorderLayout.CENTER);
            JButton submit = new JButton("Submit");
            submit.addActionListener(e2 -> {
                Notifier.emit("start", modes.getSelectedItem().toString().toLowerCase());
                Notifier.emit("loadRom", comboBox.getSelectedItem().toString());
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
        frame.add(toolbar, BorderLayout.PAGE_START);
        final JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        center.add(gameboy.getPanel());
        center.add(pong.getPanel());

        frame.add(center, BorderLayout.CENTER);

        pong.getPanel().setVisible(false);
        // add cards
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