package view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Created by rafal on 14.01.15.
 */
public class MainWindow extends JFrame implements ActionListener {

    private static final String PLAY_COMMMAND = "graj";
    private static final String CLOSE_COMMMAND = "zamknij";

    private JButton playButton;
    private JButton closeButton;

    private GameWindow gameWindow;

    public MainWindow() {
        super("Zatacka");

        setSize(new Dimension(200, 300));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initGui();

        playButton.setActionCommand(PLAY_COMMMAND);
        playButton.addActionListener(this);
        closeButton.setActionCommand(CLOSE_COMMMAND);
        closeButton.addActionListener(this);

        gameWindow = new GameWindow(this);
    }

    private void initGui() {
        setLayout(new FlowLayout(FlowLayout.CENTER));

        JPanel panel = new JPanel();
        add(panel);

        playButton = new JButton("Graj");
        panel.add(playButton);

        closeButton = new JButton("Zamknij");
        panel.add(closeButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        switch (actionCommand) {
            case PLAY_COMMMAND:
                gameWindow.setVisible(true);
                break;
            case CLOSE_COMMMAND:
                dispose();
                break;
        }
    }
}
