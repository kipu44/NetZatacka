package view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import model.ConnectionSettings;
import org.apache.log4j.Logger;

/**
 * Created by rafal on 24.01.15.
 */
public class SettingsWindow extends JDialog implements ActionListener {

    private static final long serialVersionUID = -2054887271562037871L;

    public static final Logger LOGGER = Logger.getLogger(SettingsWindow.class);

    private JTextField hostTextField;
    private JSpinner portSpinner;
    private JTextField nickTextField;
    private JButton okButton;

    public SettingsWindow(Window owner) {
        super(owner, "Ustawienia");

        setSize(new Dimension(200, 250));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);

        initGui();

        okButton.setActionCommand(Commands.OK_COMMAND);
        okButton.addActionListener(this);
    }

    private void initGui() {
        Container pane = getContentPane();
        LayoutManager boxLayout = new BoxLayout(pane, BoxLayout.Y_AXIS);
        pane.setLayout(boxLayout);

        JLabel hostLabel = new JLabel("host:");
        hostLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(hostLabel);

        hostTextField = new JTextField();
        hostTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(hostTextField);

        JLabel portLabel = new JLabel("port:");
        portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(portLabel);

        portSpinner = new JSpinner(new SpinnerNumberModel(8080, 0, 10000, 1));
        portSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(portSpinner);

        JLabel nickLabel = new JLabel("nick:");
        nickLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(nickLabel);

        nickTextField = new JTextField();
        nickTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(nickTextField);

        okButton = new JButton("Ok");
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(okButton);
    }

    public ConnectionSettings getSettings() {
        String host = hostTextField.getText();
        int port = (Integer) portSpinner.getValue();
        String nick = nickTextField.getText();
        return new ConnectionSettings(host, port, nick);
    }

    public void setOkListener(ActionListener listener) {
        okButton.addActionListener(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        switch (actionCommand) {
            case Commands.OK_COMMAND:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("ok");
                }
                setVisible(false);
                break;
            default:
                LOGGER.warn("nieznana komenda: " + actionCommand);
        }
    }
}
