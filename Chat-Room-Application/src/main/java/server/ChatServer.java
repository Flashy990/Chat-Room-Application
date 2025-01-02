package server;

import util.MessageProtocol;
import util.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The main server class that listens for incoming client connections and manages connected clients.
 */
public class ChatServer {
    private ServerSocket serverSocket;
    private final List<ConnectedClient> connectedClients;
    private final int port;
    private final boolean isRunning;

    /**
     * Constructs a ChatServer that listens on the specified port.
     *
     * @param port The port number the server will listen on.
     */
    public ChatServer(int port) {
        this.port = port;
        connectedClients = new CopyOnWriteArrayList<>();
        isRunning = true;
    }

    /**
     * Starts the server and begins accepting client connections.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Chat server started on port " + port);
            acceptClients();
        }

        catch (IOException e) {
            System.err.println("Unable to start server on port " + port);
        }
    }

    /**
     * Accepts incoming client connections.
     */
    private void acceptClients() {
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                int MAX_CLIENTS = 10;

                if (connectedClients.size() < MAX_CLIENTS) {
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    new Thread(clientHandler).start();
                }

                else {
                    System.out.println("Maximum clients connected. Connection refused.");
                    clientSocket.close();
                }
            }

            catch (IOException e) {
                System.err.println("Error accepting client connection.");
            }
        }
    }

    /**
     * Adds a new client to the list of connected clients.
     *
     * @param client The client to add.
     */
    public synchronized void addClient(ConnectedClient client) {
        connectedClients.add(client);
        System.out.println("Client connected: " + client.getUsername());
    }

    /**
     * Removes a client from the list of connected clients.
     *
     * @param client The client to remove.
     */
    public synchronized void removeClient(ConnectedClient client) {
        connectedClients.remove(client);
        client.close();
        System.out.println("Client disconnected: " + client.getUsername());
    }

    /**
     * Retrieves a client by their username.
     *
     * @param username The username of the client.
     * @return The ConnectedClient object, or null if not found.
     */
    public synchronized ConnectedClient getClientByUsername(String username) {
        for (ConnectedClient client : connectedClients) {
            if (client.getUsername().equals(username)) {
                return client;
            }
        }

        return null;
    }

    /**
     * Gets a list of usernames of connected clients, excluding a specified username.
     *
     * @param excludeUsername The username to exclude.
     * @return A list of usernames.
     */
    public synchronized List<String> getConnectedUsernames(String excludeUsername) {
        List<String> usernames = new CopyOnWriteArrayList<>();

        for (ConnectedClient client : connectedClients) {
            if (!client.getUsername().equals(excludeUsername)) {
                usernames.add(client.getUsername());
            }
        }

        return usernames;
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message The message to send.
     * @param sender  The username of the sender.
     */
    public void broadcastMessage(String message, String sender) {
        for (ConnectedClient client : connectedClients) {
            try {
                byte[] messageData = createBroadcastMessageData(sender, message);
                client.sendMessage(MessageType.BROADCAST_MESSAGE.getValue(), messageData);
            }

            catch (IOException e) {
                System.err.println("Error broadcasting message to " + client.getUsername());
            }
        }
    }

    /**
     * Sends a direct message to a specific client.
     *
     * @param message   The message to send.
     * @param sender    The username of the sender.
     * @param recipient The username of the recipient.
     */
    public void directMessage(String message, String sender, String recipient) {
        ConnectedClient client = getClientByUsername(recipient);

        if (client != null) {
            try {
                byte[] messageData = createDirectMessageData(sender, recipient, message);
                client.sendMessage(MessageType.DIRECT_MESSAGE.getValue(), messageData);
            }

            catch (IOException e) {
                System.err.println("Error sending direct message to " + recipient);
            }
        }

        else {
            System.err.println("User not found: " + recipient);

            // Optionally we can send a failed message back to the sender
            ConnectedClient senderClient = getClientByUsername(sender);

            if (senderClient != null) {
                try {
                    byte[] errorData = createFailedMessageData("User not found: " + recipient);
                    senderClient.sendMessage(MessageType.FAILED_MESSAGE.getValue(), errorData);
                }

                catch (IOException e) {
                    System.err.println("Error sending failed message to " + sender);
                }
            }
        }
    }

    /**
     * Creates the message data for a broadcast message.
     *
     * @param sender  The sender's username.
     * @param message The message content.
     * @return The byte array representing the message data.
     * @throws IOException If an I/O error occurs.
     */
    private byte[] createBroadcastMessageData(String sender, String message) throws IOException {
        // Message format:
        // int sender username size, byte[] sender username
        // int message size, byte[] message

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dataOutput = new DataOutputStream(baos)) {

            MessageProtocol.writeString(dataOutput, sender);
            MessageProtocol.writeString(dataOutput, message);

            return baos.toByteArray();
        }
    }

    /**
     * Creates the message data for a direct message.
     *
     * @param sender    The sender's username.
     * @param recipient The recipient's username.
     * @param message   The message content.
     * @return The byte array representing the message data.
     * @throws IOException If an I/O error occurs.
     */
    private byte[] createDirectMessageData(String sender, String recipient, String message) throws IOException {
        // Message format:
        // int sender username size, byte[] sender username
        // int recipient username size, byte[] recipient username
        // int message size, byte[] message

        // Use a ByteArrayOutputStream to build the message data
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dataOutput = new DataOutputStream(baos)) {

            MessageProtocol.writeString(dataOutput, sender);
            MessageProtocol.writeString(dataOutput, recipient);
            MessageProtocol.writeString(dataOutput, message);

            return baos.toByteArray();
        }
    }

    /**
     * Creates the message data for a failed message.
     *
     * @param errorMessage The error message.
     * @return The byte array representing the failed message data.
     * @throws IOException If an I/O error occurs.
     */
    private byte[] createFailedMessageData(String errorMessage) throws IOException {
        // Message format:
        // int message size, byte[] message

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dataOutput = new DataOutputStream(baos)) {

            MessageProtocol.writeString(dataOutput, errorMessage);

            return baos.toByteArray();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatServer that = (ChatServer) o;

        return port == that.port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(port);
    }

    @Override
    public String toString() {
        return "ChatServer{" +
                "port=" + port +
                ", connectedClients=" + connectedClients.size() +
                '}';
    }
}