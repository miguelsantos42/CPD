import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameServer {

    private List<Socket> userSockets;
    private int secretNumber;
    private boolean gameRunning;

    public GameServer(List<Socket> userSockets) {
        this.userSockets = userSockets;
    }

    public void start() {
        System.out.println("Starting game with " + userSockets.size() + " players");
        gameRunning = true;
        secretNumber = generateSecretNumber();

        try {
            while (true) {
                gameRunning = true;
                for (Socket socket : userSockets) {
                    handlePlayerTurn(socket);
                }
                if (!gameRunning) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error during game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int generateSecretNumber() {
        int number = ThreadLocalRandom.current().nextInt(1, 101); // Generates a random number between 1 and 100
        System.out.println("The secret number is: " + number);
        return number;
    }

    private void handlePlayerTurn(Socket socket) throws IOException {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);

        writer.println("Guess the secret number (between 1 and 100):");
        
        String guess = reader.readLine();
        int guessedNumber = Integer.parseInt(guess);

        int distance = Math.abs(guessedNumber - secretNumber);

        if (guessedNumber == secretNumber) {
            writer.println("Congratulations! You guessed the secret number.");
            gameRunning = false;
            return;
        } else if(distance <= 5){
            writer.println("Almost there! Player " + socket + " is very close.");
        } else if(distance <= 15){
            writer.println("Close! Player " + socket + " is getting closer.");
        } else {
            writer.println("Far! " + socket + " is far from the secret number.");
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GameServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            List<Socket> userSockets = new ArrayList<>();

            while (userSockets.size() < 2) { // Waiting for at least 2 players to join
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);
                userSockets.add(socket);
            }

            GameServer gameServer = new GameServer(userSockets);
            gameServer.start();

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
