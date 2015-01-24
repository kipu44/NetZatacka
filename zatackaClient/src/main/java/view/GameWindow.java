package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import model.ConnectionSettings;
import net.SocketManager;
import org.apache.log4j.Logger;

/**
 * Created by rafal on 14.01.15.
 */
public class GameWindow extends JDialog implements ActionListener, KeyListener {

    public static final Logger LOGGER = Logger.getLogger(GameWindow.class);

    private int width = 250;
    private int height = 200;
    private int y = width / 2;
    private int x = height / 2;

    private boolean leftKey;
    private boolean rightKey;
    private boolean downKey;
    private boolean upKey;

    private JButton closeButton;
    private BufferedImage image;
    private JLabel board;

    private boolean movingThreadRunning;

    private SocketManager socketManager;

    public GameWindow(Window parent) {
        super(parent, "Zatacka");

        setSize(new Dimension(250, 250));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);

        initGui();

        closeButton.setActionCommand(Commands.CLOSE_COMMAND);
        closeButton.addActionListener(this);

        addKeyListener(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                movingThreadRunning = false;
            }
        });

        socketManager = new SocketManager();
    }

    private void initGui() {
        Container pane = getContentPane();
        LayoutManager boxLayout = new BoxLayout(pane, BoxLayout.Y_AXIS);
        pane.setLayout(boxLayout);

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, 0xFF000000 | 250 * x / width << 16 | 250 * y / height << 8 | 0xFF / 2);
            }
        }
        ImageIcon icon = new ImageIcon(image);
        board = new JLabel(icon);
        board.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(board);

        closeButton = new JButton("Zamknij");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(closeButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        switch (actionCommand) {
            case Commands.CLOSE_COMMAND:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("zamykanie");
                }
                movingThreadRunning = false;
                setVisible(false);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
//        showKey(e, "typed");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("pressed");
        }
        boolean show = true;
        if (e.getKeyChar() == 'f' && !leftKey) {
            leftKey = true;
        } else if (e.getKeyChar() == 'g' && !rightKey) {
            rightKey = true;
        } else if (e.getKeyChar() == 't' && !upKey) {
            upKey = true;
        } else if (e.getKeyChar() == 'v' && !downKey) {
            downKey = true;
        } else {
            show = false;
        }
        if (show) {
            showKey(e, "pressed");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("released");
        }
        boolean show = true;
        if (e.getKeyChar() == 'f' && leftKey) {
            leftKey = false;
        } else if (e.getKeyChar() == 'g' && rightKey) {
            rightKey = false;
        } else if (e.getKeyChar() == 't' && upKey) {
            upKey = false;
        } else if (e.getKeyChar() == 'v' && downKey) {
            downKey = false;
        } else {
            show = false;
        }
        if (show) {
            showKey(e, "released");
        }
    }

    private static void showKey(KeyEvent e, String pressed) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(pressed + " ext: " + e.getExtendedKeyCode());
            LOGGER.debug(pressed + " char: " + e.getKeyChar());
            LOGGER.debug(pressed + " int: " + e.getKeyCode());
            LOGGER.debug(pressed + " loc: " + e.getKeyLocation());
        }
    }

    public void startMoving(ConnectionSettings settings) {
        socketManager.createConnection(settings);

        Thread movingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("start watku " + movingThreadRunning);
                }
                while (movingThreadRunning) {
                    boolean refresh = false;
                    if (leftKey) {
                        x = (x - 1 + width) % width;
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("lewo");
                        }
                        refresh = true;
                    }
                    if (rightKey) {
                        x = (x + 1) % width;
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("prawo");
                        }
                        refresh = true;
                    }

                    if (upKey) {
                        y = (y - 1 + height) % height;
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("gora");
                        }
                        refresh = true;
                    }
                    if (downKey) {
                        y = (y + 1) % height;
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("dol");
                        }
                        refresh = true;
                    }

                    if (refresh) {
                        image.setRGB(x, y, Color.RED.getRGB());
                        Icon icon = new ImageIcon(image);
                        board.setIcon(icon);
                    }

                    try {
//                        if (LOGGER.isDebugEnabled()) {
//                            LOGGER.debug("sleep");
//                        }
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        LOGGER.error("sleep-error: " + e.getMessage(), e);
                    }
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("koniec watku");
                }
            }
        });

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("start1");
        }
        movingThreadRunning = true;
        leftKey = false;
        rightKey = false;
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("start2");
        }
        movingThread.start();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("start3");
        }
    }
}
