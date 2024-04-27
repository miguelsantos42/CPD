import java.net.Socket;
import java.util.UUID;

public class Player {
    private Socket socket;
    private String username;
    private boolean disconnected;
    private UUID userToken;

    public Player(Socket socket, String username, UUID userToken) {
        this.socket = socket;
        this.username = username;
        this.userToken = userToken;
        this.disconnected = false;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public synchronized boolean isDisconnected() {
        return disconnected;
    }

    public synchronized void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    public UUID getUserToken() {
        return userToken;
    }
    
    public void setUserToken(UUID userToken) {
        this.userToken = userToken;
    }

}