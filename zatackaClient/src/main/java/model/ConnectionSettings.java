package model;

/**
 * Created by rafal on 24.01.15.
 */
public class ConnectionSettings {

    private String host;
    private int port;
    private String nick;

    public ConnectionSettings(String host, int port, String nick) {
        this.host = host;
        this.port = port;
        this.nick = nick;
    }

    public ConnectionSettings() {
        this("", 8080, "");
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
}
