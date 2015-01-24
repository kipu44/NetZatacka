package net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Lukasz
 */
public class SocketManager {
    
    public static final Logger LOGGER = Logger.getLogger(SocketManager.class);
    
    private ServerSocket serverSocket;
    private List<Socket> clients;
    private ObjectInputStream inputStream = null;
    private ObjectOutputStream outputStream = null;

    public SocketManager() {
    }
    
    public void createServer(int port) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Utworzono serwer na porice: " + port);
            }
            
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
     }
    
    public void waitForClient() {
        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Serwer czeka na klienta");
            }
            
            clients.add(serverSocket.accept());
            
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Polaczono z klientem");
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
