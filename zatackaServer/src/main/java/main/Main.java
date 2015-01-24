package main;

import java.awt.EventQueue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import view.MainWindow;

/**
 * @author Lukasz
 */
public class Main implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private Main() {
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Main());
    }

    @Override
    public void run() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            LOGGER.error(e, e);
        }
    }
}
