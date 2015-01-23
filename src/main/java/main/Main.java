package main;

import java.awt.EventQueue;
import view.MainWindow;

/**
 * Created by rafal on 14.01.15.
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
