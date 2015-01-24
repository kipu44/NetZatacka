package main;

import java.awt.EventQueue;

/**
 *
 * @author Lukasz
 */
public class Main implements Runnable {

    public static void main(String[] args) {
        EventQueue.invokeLater(new Main());
    }

    @Override
    public void run() {
        MainWindow mainWindow = new MainWindow();
        mainWindow.setVisible(true);
    }
}
