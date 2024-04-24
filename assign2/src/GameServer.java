import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.stream.Stream;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
public class GameServer implements Runnable{

    private static List<Socket> userSockets = new ArrayList<>();
    private int secretNumber;
    private boolean gameRunning;
    private static Lock lock = new ReentrantLock();
    private static Condition enoughPlayers = lock.newCondition();
    private static List<Socket> gameSockets = new ArrayList<>();
    private boolean number_guessed = false;

    public GameServer(List<Socket> gameSockets) {
        this.gameSockets = gameSockets;
    }

    @Override
    public void run() {
        System.out.println("Starting game with " + gameSockets.size() + " players");
        gameRunning = true;
        secretNumber = generateSecretNumber();

        try {
            while (gameRunning) {
                for (Socket socket : gameSockets) {
                    handlePlayerTurn(socket);
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

        if(number_guessed==true){
            writer.println("The other player guessed the number :)");
            gameRunning = false;
            return;
        }

        writer.println("Guess the secret number (between 1 and 100):");
        
        String guess = reader.readLine();
        int guessedNumber = Integer.parseInt(guess);

        int distance = Math.abs(guessedNumber - secretNumber);

        if (guessedNumber == secretNumber) {
            writer.println("Congratulations! You guessed the secret number.");
            number_guessed = true;
            //gameRunning = false;
            return;
        } else if(distance <= 5){
            writer.println("Almost there! Player " + socket + " is very close.");
        } else if(distance <= 15){
            writer.println("Close! Player " + socket + " is getting closer.");
        } else {
            writer.println("Far! " + socket + " is far from the secret number.");
        }
    }

    private static boolean isValidLogin(String username, String password) {
        Path path = Paths.get("../doc/users.txt");
        try (Stream<String> lines = Files.lines(path)) {
            return lines.anyMatch(line -> {
                String[] parts = line.split(":");
                return parts[0].equals(username) && parts[1].equals(password);
            });
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void handleLogin(Socket socket) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.println("Enter username:");
            String username = reader.readLine();
            writer.println("Enter password:");
            String password = reader.readLine();

            if (isValidLogin(username, password)) {
                System.out.println("User " + username + " logged in successfully.");
                writer.println("Connected");
                lock.lock();
                try {
                    userSockets.add(socket);

                    if (userSockets.size() == 2) {
                        enoughPlayers.signal();
                        startGame();
                    }
                } finally {
                    lock.unlock();
                }

            } else {
                writer.println("Invalid login. Try again.");
                socket.close(); // Close the connection if the login is invalid
            }

        } catch (IOException e) {
            System.out.println("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void startGame(){
        List<Socket> gameUserSockets = new ArrayList<>(userSockets);
        GameServer gameServer = new GameServer(gameUserSockets);
        Thread gameThread = Thread.ofVirtual().start(gameServer);
        
        // Remover jogadores da lista após iniciar o jogo
        lock.lock();
        try {
            userSockets.clear();
            System.out.println("Tamanho do userSockets apos clear = " + userSockets.size());
        } finally {
            lock.unlock();
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

            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                // Criar e iniciar uma thread virtual para lidar com o login do usuário
                Thread.ofVirtual().start(() -> {
                    handleLogin(socket);
                });
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
