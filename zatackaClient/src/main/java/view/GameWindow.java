package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import net.SocketManager;
import org.apache.log4j.Logger;

/**
 * Created by rafal on 14.01.15.
 */
public class GameWindow extends JDialog implements ActionListener {

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
    private JPanel panel;
    private final KeyStroke leftStroke;
    private final KeyStroke rightStroke;

    public GameWindow(Window parent) {
        super(parent, "Zatacka");

        setSize(new Dimension(width + 50, height + 100));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);

        initGui();

        closeButton.setActionCommand(Commands.CLOSE_COMMAND);
        closeButton.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                movingThreadRunning = false;
            }
        });

        int condition = JPanel.WHEN_IN_FOCUSED_WINDOW;
        InputMap inputMap = panel.getInputMap(condition);

        leftStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, 0);
        rightStroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, 0);
        inputMap.put(leftStroke, leftStroke.toString());
        inputMap.put(rightStroke, rightStroke.toString());
    }

    private void initGui() {
        panel = new JPanel();
        getContentPane().add(panel);

        LayoutManager boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(boxLayout);

        board = new JLabel();
        createBoardImage(width, height);
        board.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(board);

        closeButton = new JButton("Zamknij");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(closeButton);
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

    public void startMoving(SocketManager socketManager) throws IOException {
        ActionMap actionMap = panel.getActionMap();
        actionMap.put(leftStroke.toString(), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                socketManager.getOut().println("l");
            }
        });
        actionMap.put(rightStroke.toString(), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                socketManager.getOut().println("r");
            }
        });

        Thread readingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("start watku " + movingThreadRunning);
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
            public boolean refreshBoard = false;
            public int oldRow = -317;
            public int oldColumn = -317;

            @Override
            public void run() {
                while (movingThreadRunning) {
                    try {
                        String command = socketManager.getIn().readLine();
                        if (command.equals("kasztan")) {
                            refreshBoard = true;
                        } else {
                            String[] rowInts = command.split("/");

                            int row = Integer.parseInt(rowInts[0]);
                            int column = Integer.parseInt(rowInts[1]);
                            int color = Integer.parseInt(rowInts[2]);
                            int number = Integer.parseInt(rowInts[3]);

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Pakiet nr " + number + " odebrano. Narysuj " + row + "," + column);

                            }

                            double[] vector = new double[] {oldRow - row, oldColumn - column};
                            double magnitude = vector[0] * vector[0] + vector[1] * vector[1];
                            vector[0] /= magnitude;
                            vector[1] /= magnitude;
                            for (double x = row, y = column;
                                 x <= oldRow && y <= oldColumn;
                                 x += vector[0], y += vector[1]) {
                                drawPoint((int) x, (int) y, color);
                            }//*/
                            drawPoint(row, column, color);

                            oldRow = row;
                            oldColumn = column;
                        }

                        if (refreshBoard) {
                            refreshBoardImage();
                        }
                    } catch (IOException | NumberFormatException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }

            private void drawPoint(int row, int column, int color) {
                int radius = 1;
                for (int i = row - radius; i <= row + radius; i++) {
                    for (int j = column - radius; j <= column + radius; j++) {
                        image.setRGB(i, j, color);
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
//        readingThread.start();
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
