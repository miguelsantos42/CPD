import java.net.Socket;
import java.util.UUID;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;


public class Player {
    private Socket socket;
    private String username;
    private boolean disconnected;
    private UUID userToken;
    private PrintWriter writer;


    public Player(Socket socket, String username, UUID userToken) throws IOException{
        this.socket = socket;
        this.username = username;
        this.userToken = userToken;
        this.disconnected = false;
        this.writer = new PrintWriter(socket.getOutputStream(), true);

    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) throws IOException{
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream(), true);

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

    public PrintWriter getWriter() {
        return writer;
    }


}