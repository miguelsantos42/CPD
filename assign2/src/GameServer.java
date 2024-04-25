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
public class GameServer{

    private static List<Player> usersList = new ArrayList<>();
    private static Lock lock = new ReentrantLock();
    private static Condition enoughPlayers = lock.newCondition();


    private static UUID generateSessionToken() {
        return UUID.randomUUID();
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
                UUID sessionToken = generateSessionToken();
                System.out.println("User " + username + " logged in successfully.");
                writer.println("Connected " + sessionToken);
                lock.lock();
                try {
                    usersList.add(new Player(socket, username, sessionToken));
                    if (usersList.size() == 2) {
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
        List<Player> usersListTemp = new ArrayList<>(usersList); 
        Game game = new Game(usersListTemp);
        
        // Remover jogadores da lista após iniciar o jogo
        lock.lock();
        try {
            usersList.clear();
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
