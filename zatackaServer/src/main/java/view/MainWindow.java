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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import model.Player;
import model.Point;
import org.apache.log4j.Logger;

public class MainWindow extends JFrame {

    private static final long serialVersionUID = -2070331732816354501L;

    private static final Logger LOGGER = Logger.getLogger(MainWindow.class);

    public static final int WIDTH = 480;
    public static final int HEIGHT = 480;
    
    public static final int[] COLORS = {
        0x0000FF00,
        0x00FF00FF,
        0x00AAAAAA,
        0x00AAAA00
    };

    //ServerSocket, Socket, Input and Output Streams
    private ServerSocket serverSocket = null;
    private final Collection<ConnectionThread> connections = new ArrayList<>();
    private Game game;
    private CountDownLatch barrier;

    // Window info
    private Dimension screenSize;                                    // screen size
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
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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

                    connections.add(new ConnectionThread(connections.size(), serverSocket.accept()));

                    textArea.append("Polaczono z klientem.\n");
                    scrollToBottom();
                }
            } catch (NumberFormatException | IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    private class ConnectionThread implements Runnable {

        private final int id;
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        private boolean running = true;

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
                        int x = (int) positions.get(j).getX();
                        int y = (int) positions.get(j).getY();
                        int c = COLORS[id];
                        out.println(x + "/" + y + "/" + c);
                    }
                }
            }
            
            out.println("kasztan");

            while (running) {
                try {
                    for (int i = 0; i < players.size(); i++) {
                        Player player = players.get(i);
                        Point lastPlayerPosition = player.getLastPosition();
                        if (lastPlayerPosition == null) {
                            LOGGER.error("gracz: " + player);
                        } else {
                            int x = (int) lastPlayerPosition.getX();
                            int y = (int) lastPlayerPosition.getY();
                            int c = 255;
                            out.println(x + "/" + y + "/" + c);
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
                    barrier.await(50, TimeUnit.MILLISECONDS);
                } catch (IOException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        @Override
        protected void finalize() throws Throwable {
            running = false;
            super.finalize();
        }

    }
}
