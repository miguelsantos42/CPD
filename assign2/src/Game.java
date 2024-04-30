import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Game extends Thread{
    private List<Player> players;
    private int secretNumber;
    private boolean gameRunning; 
    private boolean number_guessed;
    private CountDownLatch lockObject = new CountDownLatch(1);


    public Game(List<Player> players) {
        this.players = players;
        this.secretNumber = generateSecretNumber();
        this.gameRunning = true; 
        this.number_guessed = false;
        this.start();
    }

    private int generateSecretNumber() {
        int number = ThreadLocalRandom.current().nextInt(1, 101); // Generates a random number between 1 and 100
        System.out.println("The secret number is: " + number);
        return number;
    }

    private synchronized void handlePlayerTurn(Player player) throws IOException {
        InputStream input = player.getSocket().getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        OutputStream output = player.getSocket().getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);

        if (number_guessed) {
            for (Player p : players) {
                if (p.isDisconnected()){
                    writer.println("The other player was disconnected you won :)");
                    gameRunning = false;
                    return;
                }
            }
            writer.println("The other player guessed the number :)");
            gameRunning = false;
            return;
        }

        writer.println("Guess the secret number (between 1 and 100):");
        int distance = 0;

        try{
            String guess = reader.readLine();
            int guessedNumber = Integer.parseInt(guess);
            distance = Math.abs(guessedNumber - secretNumber);
        } catch(Exception e) {
            System.out.println("Player disconnected waiting for reconnection");
            player.setDisconnected(true);
            try{
                if(lockObject.await(100, TimeUnit.SECONDS)){
                    System.out.println("Player reconnected");
                    lockObject = new CountDownLatch(1);
                    handlePlayerTurn(player);
                    return;
                } else {
                    System.out.println("Player failed to reconnect on time, other player wins");
                    number_guessed = true;
                    return;
                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        
        if (distance == 0) {
            writer.println("Congratulations! You guessed the secret number.");
            number_guessed = true;
            return;
        } else if (distance <= 5) {
            writer.println("Almost there! Player " + player.getSocket() + " is very close.");
        } else if (distance <= 15) {
            writer.println("Close! Player " + player.getSocket() + " is getting closer.");
        } else {
            writer.println("Far! " + player.getSocket() + " is far from the secret number.");
        }

    }

    List<Player> getPlayers() {
        return players;
    }

    void signalReconnect() {
        lockObject.countDown();
    }

    boolean isGameRunning(){
        return gameRunning;
    }

    @Override
    public void run() {
        System.out.println("Starting game with " + players.size() + " players");

        try {
            while (true) {
                for (Player player : players) {
                    handlePlayerTurn(player);
                    if(!gameRunning) {
                        players.clear();
                        return;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error during game: " + e.getMessage());
            e.printStackTrace();
        }
    }
}