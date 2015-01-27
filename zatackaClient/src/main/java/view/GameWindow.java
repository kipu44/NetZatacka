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
import net.Point;
import net.PredictionCalculator;
import net.SocketManager;
import org.apache.log4j.Logger;

/**
 * Created by rafal on 14.01.15.
 */
public class GameWindow extends JDialog implements ActionListener {

    private static final long serialVersionUID = -8526687153001631775L;

            private static final int[] COLORS = {
            0xFF00FFFF,
            0xFFFFFF00,
            0xFF000000,
            0xFFFFFFFF
    };
    
    private static final Logger LOGGER = Logger.getLogger(GameWindow.class);

    private int width = 640;
    private int height = 480;

    private JButton closeButton;
    private BufferedImage image;
    private JLabel board;

    private boolean movingThreadRunning;
    private JPanel panel;
    private final KeyStroke leftStroke;
    private final KeyStroke rightStroke;
    private boolean[][] painted;
    private PredictionCalculator prediction;

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

    public void startMoving(SocketManager socketManager, Point pos, Point dir, int id) throws IOException {
        ActionMap actionMap = panel.getActionMap();
        actionMap.put(leftStroke.toString(), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (prediction != null) prediction.rotateLeft();
                socketManager.getOut().println("l");
            }
        });
        actionMap.put(rightStroke.toString(), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (prediction != null) prediction.rotateRight();
                socketManager.getOut().println("r");
            }
        });

        Thread writingThread = new Thread(new Runnable() {
            public boolean refreshBoard = false;

            public int[] oldRow = {-317, -317, -317, -317};
            public int[] oldColumn = {-317, -317, -317, -317};

            @Override
            public void run() {
                while (movingThreadRunning) {
                    try {
                       // if (prediction == null /*|| socketManager.getIn().ready()*/) {
                            String command = socketManager.getIn().readLine();
                            if (command.equals("kasztan")) {
                                refreshBoard = true;
                                oldRow[id] = (int)pos.getX();
                                oldColumn[id] = (int)pos.getY();
                                prediction = new PredictionCalculator(pos, dir);
                                socketManager.getOut().println("start");
                                prediction.initTime();
                            } else {
                                String[] rowInts = command.split("/");

                                int row = Integer.parseInt(rowInts[0]);
                                int column = Integer.parseInt(rowInts[1]);
                                int color = Integer.parseInt(rowInts[2]);
                                int number = Integer.parseInt(rowInts[3]);

                                //prediction.synchronizePosition(new Point(row, column));
                                
                                if (!painted[row][column]) {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("Pakiet watku " + number + " odebrano. Narysuj " + row + "," + column);
                                    }
                                    interpolate(row, column, number, color);
                                }
                            }
                       // } //else {
//                            prediction.Update();
//                            
//                            Point point = prediction.getPosition();
//                            
//                            if (LOGGER.isDebugEnabled()) {
//                                        LOGGER.debug("Predicition:" + point.getX() + ", " + point.getY());
//                                    }
//                            
//                            if (oldRow[id] != (int) point.getX() || oldColumn[id] != (int) point.getY()) {
//                                //interpolate((int) point.getX(), (int) point.getY(), id, COLORS[id]);
//                            }
                        //} 

                        if (refreshBoard) {
                            refreshBoardImage();
                        }

                    } catch (IOException | NumberFormatException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }

            private void interpolate(int row, int column, int number, int color) {
                if (oldRow[number] != -317 && oldColumn[number] != -317) {
                    int dx = Math.abs(row - oldRow[number]), sx = oldRow[number] < row ? 1 : -1;
                    int dy = Math.abs(column - oldColumn[number]), sy = oldColumn[number] < column ? 1 : -1;
                    int err = (dx > dy ? dx : -dy) / 2, e2;

                    int x0 = oldRow[number];
                    int y0 = oldColumn[number];

                    for (;;) {
                        drawPoint(x0, y0, color);
                        if (x0 == row && y0 == column) {
                            break;
                        }
                        e2 = err;
                        if (e2 > -dx) {
                            err -= dy;
                            x0 += sx;
                        }
                        if (e2 < dy) {
                            err += dx;
                            y0 += sy;
                        }
                    }
                } else {
                    drawPoint(row, column, color);
                }

                oldRow[number] = row;
                oldColumn[number] = column;
            }

            private void drawPoint(int row, int column, int color) {
                if (!painted[row][column]) {
                    painted[row][column] = true;
                    int radius = 1;
                    for (int i = row - radius; i <= row + radius; i++) {
                        for (int j = column - radius; j <= column + radius; j++) {
                            image.setRGB(i, j, color);
                        }
                    }
                }
            }
        });

        movingThreadRunning = true;
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        writingThread.start();
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
        painted = new boolean[newWidth][newHeight];
        for (int i = 0; i < newWidth; i++) {
            painted[i][newHeight - 1] = true;
            painted[i][0] = true;
        }
        for (int i = 0; i < newHeight; i++) {
            painted[newWidth - 1][i] = true;
            painted[0][i] = true;
        }
        refreshBoardImage();
    }

    private void refreshBoardImage() {
        Icon icon = new ImageIcon(image);
        board.setIcon(icon);
    }
}
