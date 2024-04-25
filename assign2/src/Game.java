import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread{
    private List<Player> players;
    private UUID gameSessionToken;
    private int secretNumber;
    private boolean gameRunning; 
    private boolean number_guessed;
    private static Lock lock = new ReentrantLock();


    public Game(List<Player> players) {
        this.players = players;
        this.secretNumber = generateSecretNumber();
        this.gameSessionToken = generateSessionToken();
        this.gameRunning = true; 
        this.number_guessed = false;
        this.start();
    }

    private int generateSecretNumber() {
        int number = ThreadLocalRandom.current().nextInt(1, 101); // Generates a random number between 1 and 100
        System.out.println("The secret number is: " + number);
        return number;
    }

    private static UUID generateSessionToken() {
        return UUID.randomUUID();
    }

    private void handlePlayerTurn(Socket socket) throws IOException {
        socket.setSoTimeout(30000);
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);

        if (number_guessed) {
            lock.lock();
            try {
                    writer.println("The other player guessed the number :)");
                    gameRunning = false;
                    return;
                
            } finally {
                lock.unlock();
            }
        }

        writer.println("Guess the secret number (between 1 and 100):");
        int distance = 0;

        try{
            String guess = reader.readLine();
            int guessedNumber = Integer.parseInt(guess);
            socket.setSoTimeout(0);
            distance = Math.abs(guessedNumber - secretNumber);
        } catch(SocketTimeoutException e) {
            writer.println("You took to long to play!!! Passing to the other Player!");
            return;
        }

        
        lock.lock();
        try {
            if (distance == 0) {
                writer.println("Congratulations! You guessed the secret number.");
                number_guessed = true;
                //gameRunning = false;
                return;
            } else if (distance <= 5) {
                writer.println("Almost there! Player " + socket + " is very close.");
            } else if (distance <= 15) {
                writer.println("Close! Player " + socket + " is getting closer.");
            } else {
                writer.println("Far! " + socket + " is far from the secret number.");
            }
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void run() {
        System.out.println("Starting game with " + players.size() + " players");
        System.out.println("Game session tokens : " + gameSessionToken);

        try {
            while (gameRunning) {
                for (Player player : players) {
                    handlePlayerTurn(player.getSocket());
                }
            }
        } catch (IOException e) {
            System.out.println("Error during game: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
