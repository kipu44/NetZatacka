package view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.apache.log4j.Logger;

/**
 * Created by rafal on 14.01.15.
 */
public class GameWindow extends JDialog implements ActionListener, KeyListener {

    public static final Logger LOGGER = Logger.getLogger(GameWindow.class);

    private static final String CLOSE_COMMMAND = "zamknij";

    private JButton closeButton;
    private JLabel board;

    public GameWindow(Window parent) {
        super(parent, "Zatacka");

        setSize(new Dimension(250, 250));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);

        initGui();

        closeButton.setActionCommand(CLOSE_COMMMAND);
        closeButton.addActionListener(this);

        addKeyListener(this);
    }

    private void initGui() {
        setLayout(new FlowLayout(FlowLayout.CENTER));

        JPanel panel = new JPanel();
        panel.addKeyListener(this);
        add(panel);

        int width = 150;
        int height = 200;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, 0xFF000000 | 250 * x / width << 16 | 250 * y / height << 8 | 0xFF / 2);
            }
        }
        ImageIcon icon = new ImageIcon(image);
        board = new JLabel(icon);
        panel.add(board);

        closeButton = new JButton("Zamknij");
        panel.add(closeButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        switch (actionCommand) {
            case CLOSE_COMMMAND:
                setVisible(false);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        showKey(e, "typed");
        throw new RuntimeException("typed");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        showKey(e, "pressed");
        throw new RuntimeException("pressed");
    }

    @Override
    public void keyReleased(KeyEvent e) {
        showKey(e, "released");
        throw new RuntimeException("released");
    }

    private static void showKey(KeyEvent e, String pressed) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(pressed + " ext: " + e.getExtendedKeyCode());
            LOGGER.debug(pressed + " char: " + e.getKeyChar());
            LOGGER.debug(pressed + " int: " + e.getKeyCode());
            LOGGER.debug(pressed + " loc: " + e.getKeyLocation());
        }
    }
}
