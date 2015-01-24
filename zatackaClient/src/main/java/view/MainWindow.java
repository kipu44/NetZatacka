package view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import model.ConnectionSettings;
import net.SocketManager;
import org.apache.log4j.Logger;

/**
 * Created by rafal on 14.01.15.
 */
public class MainWindow extends JFrame implements ActionListener {

    public static final Logger LOGGER = Logger.getLogger(MainWindow.class);

    private JButton playButton;
    private JButton closeButton;

    private JButton settingsButton;
    private GameWindow gameWindow;

    private SettingsWindow settingsWindow;
    private ConnectionSettings settings;

    public MainWindow() {
        super("Zatacka");

        setSize(new Dimension(200, 300));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initGui();

        settingsButton.setActionCommand(Commands.SETTINGS_COMMAND);
        settingsButton.addActionListener(this);

        playButton.setActionCommand(Commands.PLAY_COMMMAND);
        playButton.addActionListener(this);

        closeButton.setActionCommand(Commands.CLOSE_COMMAND);
        closeButton.addActionListener(this);

        settingsWindow = new SettingsWindow(this);
        settingsWindow.setOkListener(this);

        gameWindow = new GameWindow(this);

        settings=new ConnectionSettings();
    }

    private void initGui() {
        Container pane = getContentPane();
        LayoutManager boxLayout = new BoxLayout(pane, BoxLayout.Y_AXIS);
        pane.setLayout(boxLayout);

        playButton = new JButton("Graj");
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(playButton);

        settingsButton = new JButton("Ustawienia");
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(settingsButton);

        closeButton = new JButton("Zamknij");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(closeButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            String actionCommand = e.getActionCommand();
            switch (actionCommand) {
                case Commands.PLAY_COMMMAND:
                    gameWindow.startMoving(settings);
                    gameWindow.setVisible(true);
                    break;
                case Commands.SETTINGS_COMMAND:
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("ustawnienia");
                    }
                    settingsWindow.setVisible(true);
                    break;
                case Commands.OK_COMMAND:
                    settings = settingsWindow.getSettings();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("ustawnienia ok");
                    }
                    break;
                case Commands.CLOSE_COMMAND:
                    dispose();
                    break;
                default:
                    LOGGER.warn("nieznana komenda: " + actionCommand);
            }
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(this, e1.getMessage(), "Blad", JOptionPane.ERROR_MESSAGE);
        }
    }
}
