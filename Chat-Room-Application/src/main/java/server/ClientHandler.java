package server;

import util.MessageProtocol;
import util.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

/**
 * Handles communication with a single client in a separate thread.
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ChatServer server;
    private DataInputStream input;
    private DataOutputStream output;
    private String username;
    private final InsultGenerator insultGenerator;

    /**
     * Constructs a ClientHandler with the specified client socket and server reference.
     *
     * @param clientSocket The client's socket connection.
     * @param server       The ChatServer instance.
     */
    public ClientHandler(Socket clientSocket, ChatServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.insultGenerator = new InsultGenerator();

        try {
            input = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());
        }

        catch (IOException e) {
            System.err.println("Error initializing client handler.");
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                int messageType = input.readInt();
                processMessage(messageType);
            }
        }

        catch (IOException e) {
            System.out.println("Client disconnected: " + username);

            if (username != null) {
                server.removeClient(new ConnectedClient(username, clientSocket, output));
            }
        }
    }

    /**
     * Processes a message from the client based on the message type.
     *
     * @param messageType The type of the message.
     * @throws IOException If an I/O error occurs.
     */
    private void processMessage(int messageType) throws IOException {
        if (messageType == MessageType.CONNECT_MESSAGE.getValue()) {
            handleConnectMessage();
        }

        else if (messageType == MessageType.DISCONNECT_MESSAGE.getValue()) {
            handleDisconnectMessage();
        }

        else if (messageType == MessageType.QUERY_CONNECTED_USERS.getValue()) {
            handleQueryUsers();
        }

        else if (messageType == MessageType.BROADCAST_MESSAGE.getValue()) {
            handleBroadcastMessage();
        }

        else if (messageType == MessageType.DIRECT_MESSAGE.getValue()) {
            handleDirectMessage();
        }

        else if (messageType == MessageType.SEND_INSULT.getValue()) {
            handleSendInsult();
        }

        else {
            sendFailedMessage("Unknown message type: " + messageType);
        }
    }

    /**
     * Handles a connect message from the client.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void handleConnectMessage() throws IOException {
        String requestedUsername = MessageProtocol.readString(input);
        boolean success = true;
        String responseMessage;

        if (server.getClientByUsername(requestedUsername) != null) {
            success = false;
            responseMessage = "Username already taken.";
        }

        else {
            username = requestedUsername;
            server.addClient(new ConnectedClient(username, clientSocket, output));
            int numClients = server.getConnectedUsernames(username).size();
            responseMessage = "There are " + numClients + " other connected clients.";
        }

        sendConnectResponse(success, responseMessage);
    }

    /**
     * Sends a connect response to the client.
     *
     * @param success         Whether the connection was successful.
     * @param responseMessage The response message.
     * @throws IOException If an I/O error occurs.
     */
    private void sendConnectResponse(boolean success, String responseMessage) throws IOException {
        output.writeInt(MessageType.CONNECT_RESPONSE.getValue());
        output.writeBoolean(success);
        MessageProtocol.writeString(output, responseMessage);
    }

    /**
     * Handles a disconnect message from the client.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void handleDisconnectMessage() throws IOException {
        String disconnectingUsername = MessageProtocol.readString(input);

        if (username.equals(disconnectingUsername)) {
            server.removeClient(new ConnectedClient(username, clientSocket, output));
            sendConnectResponse(true, "You are no longer connected.");
            clientSocket.close();
        }

        else {
            sendConnectResponse(false, "Invalid username for disconnect.");
        }
    }

    /**
     * Handles a query connected users message from the client.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void handleQueryUsers() throws IOException {
        String requestingUsername = MessageProtocol.readString(input);

        if (!username.equals(requestingUsername)) {
            sendFailedMessage("Invalid username for query.");
            return;
        }

        output.writeInt(MessageType.QUERY_USER_RESPONSE.getValue());
        var otherUsers = server.getConnectedUsernames(username);
        output.writeInt(otherUsers.size());

        for (String user : otherUsers) {
            MessageProtocol.writeString(output, user);
        }
    }

    /**
     * Handles a broadcast message from the client.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void handleBroadcastMessage() throws IOException {
        String senderUsername = MessageProtocol.readString(input);
        String message = MessageProtocol.readString(input);

        if (!username.equals(senderUsername)) {
            sendFailedMessage("Invalid sender username.");
            return;
        }

        server.broadcastMessage(message, senderUsername);
    }

    /**
     * Handles a direct message from the client.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void handleDirectMessage() throws IOException {
        String senderUsername = MessageProtocol.readString(input);
        String recipientUsername = MessageProtocol.readString(input);
        String message = MessageProtocol.readString(input);

        if (!username.equals(senderUsername)) {
            sendFailedMessage("Invalid sender username.");
            return;
        }

        server.directMessage(message, senderUsername, recipientUsername);
    }

    /**
     * Handles a send insult message from the client.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void handleSendInsult() throws IOException {
        String senderUsername = MessageProtocol.readString(input);
        String recipientUsername = MessageProtocol.readString(input);

        if (!username.equals(senderUsername)) {
            sendFailedMessage("Invalid sender username.");
            return;
        }

        String insult = insultGenerator.generateInsult();
        String message = senderUsername + " -> " + recipientUsername + ": " + insult;
        server.broadcastMessage(message, senderUsername);
    }

    /**
     * Sends a failed message to the client with the specified error message.
     *
     * @param errorMessage The error message.
     * @throws IOException If an I/O error occurs.
     */
    private void sendFailedMessage(String errorMessage) throws IOException {
        output.writeInt(MessageType.FAILED_MESSAGE.getValue());
        MessageProtocol.writeString(output, errorMessage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientHandler that = (ClientHandler) o;

        return Objects.equals(clientSocket, that.clientSocket);
    }

    @Override
    public int hashCode() {
        return clientSocket != null ? clientSocket.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ClientHandler{" +
                "username='" + username + '\'' +
                ", clientSocket=" + clientSocket +
                '}';
    }
}