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

            // Leitura da resposta do servidor sobre o sucesso do login
            fromServer = reader.readLine();
            System.out.println(fromServer);
            if (fromServer.equals("Invalid login. Try again.")) {
                System.out.println("Login failed. Please try again.");
                return; // Se o login falhar, sai do programa
            } else {
                System.out.println("Login successful. Starting game...");
            }

            String response;
            boolean gameRunning = true;

            while (gameRunning) {
                response = reader.readLine();
                System.out.println(response);

                if (response.contains("Guess the secret number")) {
                    String guess = userInputReader.readLine();
                    writer.println(guess);
                }else if(response.contains("The other player guessed the number :)")){
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
