package client;

import java.io.DataInputStream;
import java.io.IOException;

import util.MessageProtocol;
import util.MessageType;

/**
 * Listens for incoming messages from the server in a separate thread.
 */
public class ServerListener implements Runnable {
    private final DataInputStream input;
    private final ChatClient client;

    /**
     * Constructs a ServerListener with the specified input stream and client reference.
     *
     * @param input  The DataInputStream to read messages from the server.
     * @param client The ChatClient instance.
     */
    public ServerListener(DataInputStream input, ChatClient client) {
        this.input = input;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            while (client.isConnected()) {
                int messageType = input.readInt();
                processMessage(messageType);
            }
        }

        catch (IOException e) {
            System.out.println("Server connection closed.");
            client.disconnect();
        }
    }

    /**
     * Processes a message from the server based on the message type.
     *
     * @param messageType The type of the message.
     * @throws IOException If an I/O error occurs.
     */
    private void processMessage(int messageType) throws IOException {
        if (messageType == MessageType.CONNECT_RESPONSE.getValue()) {
            handleConnectResponse();
        }

        else if (messageType == MessageType.QUERY_USER_RESPONSE.getValue()) {
            handleQueryUserResponse();
        }

        else if (messageType == MessageType.BROADCAST_MESSAGE.getValue()) {
            handleBroadcastMessage();
        }

        else if (messageType == MessageType.DIRECT_MESSAGE.getValue()) {
            handleDirectMessage();
        }

        else if (messageType == MessageType.FAILED_MESSAGE.getValue()) {
            handleFailedMessage();
        }

        else {
            System.out.println("Unknown message type received: " + messageType);
        }
    }

    /**
     * Handles a connect response from the server.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void handleConnectResponse() throws IOException {
        boolean success = input.readBoolean();
        String message = MessageProtocol.readString(input);
        System.out.println(message);

        if (!success) {
            client.setConnected(false);
            client.disconnect();
        }
    }

    /**
     * Handles a query user response from the server.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void handleQueryUserResponse() throws IOException {
        int numUsers = input.readInt();

        if (numUsers == 0) {
            System.out.println("No other users are connected.");
        }

        else {
            System.out.println("Connected users:");

            for (int i = 0; i < numUsers; i++) {
                String username = MessageProtocol.readString(input);
                System.out.println("- " + username);
            }
        }
    }

    /**
     * Handles a broadcast message from the server.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void handleBroadcastMessage() throws IOException {
        String sender = MessageProtocol.readString(input);
        String message = MessageProtocol.readString(input);
        System.out.println(sender + " (broadcast): " + message);
    }

    /**
     * Handles a direct message from the server.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void handleDirectMessage() throws IOException {
        String sender = MessageProtocol.readString(input);
        String recipient = MessageProtocol.readString(input);
        String message = MessageProtocol.readString(input);

        if (recipient.equals(client.getUsername())) {
            System.out.println(sender + " (private): " + message);
        }
    }

    /**
     * Handles a failed message from the server.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void handleFailedMessage() throws IOException {
        String errorMessage = MessageProtocol.readString(input);
        System.out.println("Error: " + errorMessage);
    }
}