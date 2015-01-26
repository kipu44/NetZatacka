package view;

import controller.Game;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import model.Player;
import model.Point;
import org.apache.log4j.Logger;

public class MainWindow extends JFrame {

    private static final long serialVersionUID = -2070331732816354501L;

    private static final Logger LOGGER = Logger.getLogger(MainWindow.class);

    public static final int WIDTH = 480;
    public static final int HEIGHT = 480;

    private static final int[] COLORS = {
            0xFF00FFFF,
            0xFFFFFF00,
            0xFF000000,
            0xFFFFFFFF
    };

    //ServerSocket, Socket, Input and Output Streams
    private ServerSocket serverSocket = null;
    private final Collection<ConnectionThread> connections = new ArrayList<>();
    private Game game;
    private CountDownLatch barrier;

    // Window info
    private Dimension screenSize;
    private int width;
    private int height;

    // JComponents
    private JTextArea textArea;
    private JScrollPane sp;
    private JLabel portLabel;
    private JTextField portField;
    private JButton createButton;

    // --- Constructor ---
    public MainWindow() {
        initUI();
        initListener();
    }

    // --- User Interface ---
    private void initUI() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = (int) screenSize.getWidth();
        height = (int) screenSize.getHeight();
        setSize(width / 3, height / 3);
        setResizable(true);
        setTitle("Server Interface");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel consolePane = new JPanel();
        consolePane.setLayout(new BorderLayout());
        consolePane.setPreferredSize(new Dimension(width, height));
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.append("Uruchomiono aplikacje\n");
        sp = new JScrollPane(textArea);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        consolePane.add(sp, BorderLayout.CENTER);
        add(consolePane, BorderLayout.WEST);

        portLabel = new JLabel("Port number:");
        portField = new JTextField("9876");
        portField.setPreferredSize(new Dimension(100, 25));
        createButton = new JButton("Create");
        createButton.setToolTipText("Create game");

        JPanel pNorth = new JPanel();
        pNorth.add(portLabel);
        pNorth.add(portField);
        pNorth.add(createButton);
        add(pNorth, BorderLayout.NORTH);
    }

    public void scrollToBottom() {
        textArea.setCaretPosition(textArea.getText().length());
    }

    private void initListener() {
        createButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new CreateServerThread("Server");
            }
        });
    }

    private class CreateServerThread implements Runnable {

        private CreateServerThread(String name) {
            new Thread(this, name).start();
        }

        @Override
        public void run() {
            try {
                createButton.setEnabled(false);
                serverSocket = new ServerSocket(Integer.parseInt(portField.getText()));

                while (connections.size() < 9) {
                    textArea.append("Czekanie na klienta...\n");
                    scrollToBottom();

                    if (game == null) {
                        game = new Game(WIDTH, HEIGHT);
                        new Thread(game, "Game").start();
                    }

                    barrier = new CountDownLatch(connections.size() + 1);
                    Socket socket = serverSocket.accept();
                    connections.add(new ConnectionThread(connections.size(), socket));

                    textArea.append("Polaczono z klientem.\n");
                    scrollToBottom();
                }
            } catch (NumberFormatException | IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    private class ConnectionThread implements Runnable {

        private static final String D_D_D_D = "%d/%d/%d/%d";
        private final int id;
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private int lastSentX = -120;
        private int lastSentY = -120;

        private boolean running = true;
        private int number = 1;

        private ConnectionThread(int id, Socket socket) throws IOException {
            this.id = id;
            this.socket = socket;

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            game.addPlayer();
            new Thread(this, Integer.toString(id)).start();
        }

        @Override
        public void run() {
            out.println(Integer.toString(WIDTH) + "/" + Integer.toString(HEIGHT));
            List<Player> players = game.getPlayers();

            for (int i = 0; i < players.size(); i++) {
                if (i != id) {
                    List<Point> positions = players.get(i).getPositions();

                    for (int j = 0; j < positions.size(); j++) {
                        sendPosition(positions.get(j), i);
                    }
                }
            }

            out.println("kasztan");

            while (running) {
                try {
                    for (int i = 0; i < players.size(); i++) {
                        Player player = players.get(i);
                        if (player.isAlive()) {
                            Point lastPlayerPosition = player.getLastPosition();
                            if (lastPlayerPosition == null) {
                                // LOGGER.error("gracz: " + player);
                            } else {
                                sendPosition(lastPlayerPosition, i);
                            }
                        }
                    }

                    if (in.ready()) {
                        String inLine = in.readLine();
                        textArea.append(inLine);

                        if ("l".equals(inLine)) {
                            game.rotateLeft(id);
                        } else if ("r".equals(inLine)) {
                            game.rotateRight(id);
                        }
                    }

                    barrier.countDown();
                    barrier.await(20, TimeUnit.MILLISECONDS);
                } catch (IOException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        private void sendPosition(Point point, int id) {
            int x = (int) point.getX();
            int y = (int) point.getY();
            int c = COLORS[id];

            if (x != lastSentX || y != lastSentY) {
                number++;
                out.println(String.format(Locale.ENGLISH, D_D_D_D, x, y, c, number));
                if (Math.abs(x - lastSentX) > 3 || Math.abs(y - lastSentY) > 3) {
                    LOGGER.error("Error");
                } else {
                    interpolate(x, y);
                }
            }
        }

        private void interpolate(int row, int column) {
            if (lastSentX == -120 || lastSentY == -120) {
                double[] vector = {lastSentX - row, lastSentY - column};
                double magnitude = vector[0] * vector[0] + vector[1] * vector[1];
                magnitude = StrictMath.sqrt(magnitude);
//            magnitude *= 3.0;
                vector[0] /= magnitude;
                vector[1] /= magnitude;
                for (double x = row, y = column;
                     x <= lastSentX && y <= lastSentY;
                     x += vector[0], y += vector[1]) {
                    int ix = (int) x;
                    int iy = (int) y;
                    game.drawPoint(ix, iy);
                }

                lastSentX = row;
                lastSentY = column;
            } else {
                game.drawPoint(row, column);
            }
        }

        @Override
        protected void finalize() throws Throwable {
            running = false;
            super.finalize();
        }

    }
}
