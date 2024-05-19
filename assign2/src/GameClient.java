import java.io.*;
import java.net.*;

public class GameClient {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java GameClient <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));

            // Processo de login
            String fromServer;
            String fromUser;
            
            //username
            fromServer = reader.readLine();
            System.out.println(fromServer);
            fromUser = userInputReader.readLine();
            writer.println(fromUser);
            //writer.println(fromUser);

            //password
            fromServer = reader.readLine();
            System.out.println(fromServer);
            fromUser = userInputReader.readLine();
            writer.println(fromUser);
            //todo add logic to reconect
            // Leitura da resposta do servidor sobre o sucesso do login
            fromServer = reader.readLine();
            System.out.println(fromServer);
            if (fromServer.equals("Invalid login. Try again.")) {
                System.out.println("Login failed. Please try again.");
                return; // Se o login falhar, sai do programa
            } else if(fromServer.equals("Player already connected.")) {
                return;
            } else if(fromServer.equals("Player reconnected to game.")){
            } 
            else {
                System.out.println("Login successful. Starting game...");
                System.out.println("Which game do you want to play? (1 - Casual, 2 - Ranked): ");
                while(true){
                    String gameMode = userInputReader.readLine();
                    if (gameMode.equals("1")) {
                        writer.println("1");
                        break;
                    } else if (gameMode.equals("2")) {
                        writer.println("2");
                        break;
                    } else {
                        System.out.println("Invalid input. Please enter 1 or 2.");
                    }
                }
                System.out.println(reader.readLine());
            }

            String response;
            boolean gameRunning = true;

            while (gameRunning) {
                response = reader.readLine();
                System.out.println(response);

                if (response.contains("Guess the secret number")) {
                    String guess;
                    int guessedNumber = -1;
                    while(true){
                        try {
                            guess = userInputReader.readLine();
                            guessedNumber = Integer.parseInt(guess);
                            if(guessedNumber < 1 || guessedNumber > 100){
                                System.out.println("Invalid input. Please enter a number between 1 and 100:");
                            }else{
                                break;
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid input. Please enter a number between 1 and 100:");
                        }
                    }
                    writer.println(String.valueOf(guessedNumber));
                }else if(response.contains("The other player guessed the number :)") || response.contains("The other player was disconnected you won :)")){
                    gameRunning = false;
                    break;
                }

                response = reader.readLine();
                System.out.println(response);

                if (response.contains("Congratulations")) {
                    gameRunning = false;
                }
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}