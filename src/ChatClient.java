import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int serverPort = 12345;

        try {
            Socket socket = new Socket(serverAddress, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            //  Receive the registration request from the server
            String registrationRequest = in.readLine();
            System.out.println(registrationRequest);

            // Prompt the user to input "REGISTER <unique_user_name>"
            String userRegistration = in.readLine();
            out.println(userRegistration);

            //  Receive the welcome message from server
            String welcomeMessage = in.readLine();
            System.out.println(welcomeMessage);

            //  Register with a unique username
            System.out.println("Enter a unique username: ");
            String username = userInput.readLine();
            out.println("REGISTER " + username);



            //  Start a thread for listening to messages from the server
            Thread listeningThread = new Thread(() -> {
                try {
                    while (true) {
                        String serverMessage = in.readLine();
                        if (serverMessage != null) {
                            System.out.println(serverMessage);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            listeningThread.start();

            //  implementing a loop to read user input and send message to the server
            while (true) {
                String message = userInput.readLine();
                out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}