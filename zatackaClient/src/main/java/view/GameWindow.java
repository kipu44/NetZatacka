package view;

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
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import net.SocketManager;
import org.apache.log4j.Logger;

/**
 * Created by rafal on 14.01.15.
 */
public class GameWindow extends JDialog implements ActionListener, KeyListener {

    private static final long serialVersionUID = -8526687153001631775L;

    private static final Logger LOGGER = Logger.getLogger(GameWindow.class);

    private int width = 640;
    private int height = 480;

    private boolean leftKey;
    private boolean rightKey;

    private JButton closeButton;
    private BufferedImage image;
    private JLabel board;

    private boolean movingThreadRunning;

    public GameWindow(Window parent) {
        super(parent, "Zatacka");

        setSize(new Dimension(width + 50, height + 100));
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
    }

    private void initGui() {
        Container pane = getContentPane();
        LayoutManager boxLayout = new BoxLayout(pane, BoxLayout.Y_AXIS);
        pane.setLayout(boxLayout);

        board = new JLabel();
        createBoardImage(width, height);
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
        char keyChar = e.getKeyChar();
        if (keyChar == 'f' && !leftKey) {
            leftKey = true;
        } else if (keyChar == 'g' && !rightKey) {
            rightKey = true;
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
        char keyChar = e.getKeyChar();
        if (keyChar == 'f' && leftKey) {
            leftKey = false;
        } else if (keyChar == 'g' && rightKey) {
            rightKey = false;
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

    public void startMoving(SocketManager socketManager) throws IOException {
        removeKeyListener(this);
        addKeyListener(this);

        Thread readingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("start watku " + movingThreadRunning);
                    LOGGER.debug(getKeyListeners());
                    LOGGER.debug(board.getKeyListeners());
                }
                while (movingThreadRunning) {
                    if (leftKey) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("lewo");
                        }
                        socketManager.getOut().println("l");
                    }
                    if (rightKey) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("prawo");
                        }
                        socketManager.getOut().println("r");
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

        Thread writingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (movingThreadRunning) {
                    try {
                        String command = socketManager.getIn().readLine();
                        String[] rowInts = command.split("/");

                        int row = Integer.parseInt(rowInts[0]);
                        int column = Integer.parseInt(rowInts[1]);
                        int color = Integer.parseInt(rowInts[2]);

//                        if (LOGGER.isDebugEnabled()) {
//                            LOGGER.debug("x = " + row + ", y = " + column + ", color = " + color);
//                        }

                        image.setRGB(row, column, color);
                        refreshBoardImage();
                    } catch (IOException | NumberFormatException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
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
        readingThread.start();
        writingThread.start();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("start3");
        }
    }

    public void setBoardSize(int width, int height) {
        this.width = width;
        this.height = height;
        setSize(new Dimension(width + 50, height + 100));

        createBoardImage(width, height);
    }

    private void createBoardImage(int newWidth, int newHeight) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("w = " + newWidth + ", h = " + newHeight);
        }

        image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < newWidth; x++) {
            for (int y = 0; y < newHeight; y++) {
                image.setRGB(x, y, 0xFF000000 | 250 * x / newWidth << 16 | 250 * y / newHeight << 8 | 0xFF / 2);
            }
        }
        refreshBoardImage();
    }

    private void refreshBoardImage() {
        Icon icon = new ImageIcon(image);
        board.setIcon(icon);
    }
}
