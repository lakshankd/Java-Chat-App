import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static Map<String, PrintWriter> connectedClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = 12346; // Change to your desired port number

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running and listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.println("Please register with a unique username (e.g., REGISTER John)");

                String registrationMessage = in.readLine();
                if (registrationMessage.startsWith("REGISTER ")) {
                    username = registrationMessage.substring(9).trim();
                    if (connectedClients.containsKey(username)) {
                        out.println("Username already taken. Please choose a different username.");
                        return;
                    } else {
                        connectedClients.put(username, out);
                        out.println("WELCOME " + username);
                    }
                } else {
                    out.println("Invalid registration. Please try again.");
                    return;
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("LIST")) {
                        out.println("Connected Clients: " + connectedClients.keySet());
                    } else if (message.equals("LISTENING")) {
                        if (connectedClients.size() == 1) {
                            out.println("No other clients are present. Waiting for input.");
                            while (true) {
                                String inputFromClient = in.readLine();
                                if (inputFromClient != null) {
                                    broadcastMessage(username + ": " + inputFromClient);
                                }
                            }
                        } else {
                            out.println("Other clients are present. Use LIST to see the list.");
                        }
                    } else if (message.startsWith("SEND ")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length == 3) {
                            String recipient = parts[1];
                            String text = parts[2];
                            PrintWriter recipientWriter = connectedClients.get(recipient);
                            if (recipientWriter != null) {
                                recipientWriter.println(username + " (private): " + text);
                            } else {
                                out.println("User " + recipient + " is not online.");
                            }
                        }
                    } else {
                        // Handle other commands or messages
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    connectedClients.remove(username);
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcastMessage(String message) {
            for (PrintWriter clientOut : connectedClients.values()) {
                clientOut.println(message);
            }
        }
    }
}