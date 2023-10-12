import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static final int PORT = 12345;
    public static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Chat server is running on Port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client is connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        super();
        this.socket = socket;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Please Register: REGISTER <unique_user_name>");
            String registrationMessage = in.readLine();

            if (registrationMessage != null && registrationMessage.startsWith("REGISTER ")) {
                username = registrationMessage.substring((9));
                out.println("WELCOME " + username);

                while (true) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    }

                    if (message.equals("LIST")) {
                        List<String> connectedClients = ChatServer.clients.stream()
                                .filter(client -> client != this)
                                .map(client -> client.username)
                                .toList();
                        out.println("Connected Clients : " + String.join(", ", connectedClients));
                    } else if (message.equals("LISTENING")) {
                        // Client is waiting for message from the server
                        while (true) {
                            String receivedMessage = in.readLine();
                            if (receivedMessage == null) {
                                break;
                            }
                            System.out.println(username + " received: " + receivedMessage);
                        }
                    } else if (message.startsWith("SEND ")) {
                        String[] parts = message.split(" ", 3);
                        String toClientName = parts[1];
                        String sendMessage = parts[2];
                        ChatServer.broadcastMessage(username + " say to " + toClientName + ": " + sendMessage, this);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
                socket.close();
                ChatServer.removeClient(this);
                System.out.println(username + " has left.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void sendMessage(String message) {
        out.println(message);
    }
}
