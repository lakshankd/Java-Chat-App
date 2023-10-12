import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1"; // Change to the server's IP address or hostname
        int serverPort = 12346; // Change to the server's port number

        Socket socket = null; // Declare the socket outside the try-catch block

        try {
            socket = new Socket(serverAddress, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            // Registration
            String registrationMessage = in.readLine();
            System.out.println(registrationMessage);

            String userInput;
            while (true) {
                userInput = consoleInput.readLine();
                if (userInput.startsWith("REGISTER ")) {
                    out.println(userInput);
                    break;
                } else {
                    System.out.println("Invalid input. Please start with 'REGISTER your_username'");
                }
            }

            // Wait for the server's welcome message
            String welcomeMessage = in.readLine();
            System.out.println(welcomeMessage);

            // Start a thread to listen for messages from the server
            Thread serverListener = new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = in.readLine()) != null) {
                        System.out.println(serverResponse);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverListener.start();

            // Handle user input
            while (true) {
                userInput = consoleInput.readLine();
                if (userInput != null) {
                    out.println(userInput);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close(); // Close the socket in the finally block
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
