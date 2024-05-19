import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.stream.Stream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameServer {

    private static List<Player> casualQueue = new ArrayList<>();
    private static List<Player> rankedQueue = new ArrayList<>();
    private static List<Game> gamesList = new ArrayList<>();
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
                lock.lock();
                try {
                    for (int i = 0; i < gamesList.size(); i++) {
                        Game game = gamesList.get(i);
                        if (!game.isGameRunning()) {
                            gamesList.remove(i);
                            i--;
                        }
                    }
                    for (Game game : gamesList) {
                        for (Player player : game.getPlayers()) {
                            if (player.getUsername().equals(username)) {
                                if (player.isDisconnected()) {
                                    player.setSocket(socket);
                                    player.setUserToken(sessionToken);
                                    player.setDisconnected(false);
                                    game.signalReconnect();
                                    writer.println("Player reconnected successfully.");
                                    return;
                                }
                                writer.println("Player already connected.");
                                socket.close();
                                return;
                            }
                        }
                    }
                    for (Player player : casualQueue) {
                        if (player.getUsername().equals(username)) {
                            writer.println("Player already connected.");
                            socket.close();
                            return;
                        }
                    }
                    for (Player player : rankedQueue) {
                        if (player.getUsername().equals(username)) {
                            writer.println("Player already connected.");
                            socket.close();
                            return;
                        }
                    }

                    Thread.ofVirtual().start(() -> {
                        processGameModeSelection(socket, writer, reader, username, sessionToken);
                    });
                } finally {
                    lock.unlock();
                }

            } else {
                writer.println("Invalid login. Try again.");
                socket.close();
            }

        } catch (IOException e) {
            System.out.println("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void processGameModeSelection(Socket socket, PrintWriter writer, BufferedReader reader, String username, UUID sessionToken){
        try{
        writer.println("Login successful. Which game do you want to play? (1 - Casual, 2 - Ranked):");
        String gameMode = reader.readLine();
        lock.lock();
        try{
            if (gameMode.equals("1")) {
            writer.println("Joined the casual game queue.");
            casualQueue.add(new Player(socket, username, sessionToken));
                if (casualQueue.size() >= 2) {
                    Thread.ofVirtual().start(() -> {
                        startGame(casualQueue);
                    });
                }
            } else if (gameMode.equals("2")) {
                writer.println("Joined the ranked game queue.");
                rankedQueue.add(new Player(socket, username, sessionToken));
                if (rankedQueue.size() >= 2) {
                    Thread.ofVirtual().start(() -> {
                        startGame(rankedQueue);
                    });
                }
            } else {
                writer.println("Invalid game mode. Connection closing.");
                socket.close();
                return;
            }
        } finally{
            lock.unlock();
        }
        }catch (IOException e) {
            System.out.println("Error during login: " + e.getMessage());
            e.printStackTrace();
        }

        
    }

    private static void startGame(List<Player> queue) {
        List<Player> players = new ArrayList<>(queue.subList(0, 2));
        Game game = new Game(players);

        lock.lock();
        try {
            gamesList.add(game);
            queue.removeAll(players);
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

            while (true) {
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
