import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Player {
    private Socket socket;
    private String username;
    private boolean disconnected;
    private UUID userToken;
    private double rank;
    private double joinedQueue;
    private Lock lock = new ReentrantLock();

    //podemos usar o synchronized 

    public Player(Socket socket, String username, UUID userToken) {
        this.socket = socket;
        this.username = username;
        this.userToken = userToken;
        this.disconnected = false;
        this.rank = 0;
        this.joinedQueue = System.currentTimeMillis();
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

    public boolean isDisconnected() {
        lock.lock();
        boolean state = disconnected;
        lock.unlock();
        return state;
    }

    public void setDisconnected(boolean disconnected) {
        lock.lock();
        this.disconnected = disconnected;
        lock.unlock();
    }

    public UUID getUserToken() {
        return userToken;
    }
    
    public void setUserToken(UUID userToken) {
        this.userToken = userToken;
    }

    public  double getRank(){
        lock.lock();
        double state = rank;
        lock.unlock();
        return state;
    }

    public void setRank(double rank){
        lock.lock();
        this.rank = rank;
        lock.unlock();
    }

    public double getJoinedQueue(){
        return this.joinedQueue;
    }
}