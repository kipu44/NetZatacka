package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import model.ConnectionSettings;
import org.apache.log4j.Logger;

/**
 *
 * @author Lukasz
 */
public class SocketManager {
    
    public static final Logger LOGGER = Logger.getLogger(SocketManager.class);
    
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public SocketManager() {
    }
    
    public void createConnection(ConnectionSettings settings) throws IOException {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Tworzenie polaczenia: " + settings.getHost() + "::" + settings.getPort());
            }
            
            socket = new Socket(settings.getHost(), settings.getPort());
            out =  new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
     }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }
}
