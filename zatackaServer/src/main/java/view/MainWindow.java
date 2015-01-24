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
import model.Point;
import org.apache.log4j.Logger;

public class MainWindow extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(MainWindow.class);

    public static final int WIDTH = 600;
    public static final int HEIGHT = 600;

    //ServerSocket, Socket, Input and Output Streams
    private ServerSocket serverSocket = null;
    private List<ConnectionThread> connections = new ArrayList<>();
    private Game game;
    CountDownLatch barrier;

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
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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

        public CreateServerThread(String name) {
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
                    } else {
                        game.addPlayer();
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

        private int id;
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ConnectionThread(int i, Socket socket) {

            this.id = i;
            this.socket = socket;

            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }

            new Thread(this, Integer.toString(i)).start();
        }

        @Override
        public void run() {
            out.println(Integer.toString(WIDTH) + "/" + Integer.toString(HEIGHT));

            while (true) {
                try {
                    for (int i = 0; i < game.getPlayers().size(); i++) {
                        Point lastPlayerPosition = game.getPlayers().get(i).getLastPosition();
                        String x = Integer.toString((int) lastPlayerPosition.getX());
                        String y = Integer.toString((int) lastPlayerPosition.getY());
                        String c = Integer.toString(255);
                        out.println(x + "/" + y + "/" + c);
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
    }
}
