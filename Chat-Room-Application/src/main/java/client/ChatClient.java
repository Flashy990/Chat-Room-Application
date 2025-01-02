package client;

import util.MessageProtocol;
import util.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

/**
 * Client-side application that connects to the server and handles user interaction.
 */
public class ChatClient {
    private Socket serverSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private final String username;
    private final String serverAddress;
    private final int port;
    private volatile boolean isConnected;

    /**
     * Constructs a ChatClient with the specified server address, port, and username.
     *
     * @param serverAddress The server's IP address or hostname.
     * @param port          The server's port number.
     * @param username      The desired username for the client.
     */
    public ChatClient(String serverAddress, int port, String username) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.username = username;
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Starts the client and initiates communication with the server.
     */
    public void start() {
        try {
            serverSocket = new Socket(serverAddress, port);
            input = new DataInputStream(serverSocket.getInputStream());
            output = new DataOutputStream(serverSocket.getOutputStream());
            isConnected = true;

            sendConnectMessage();

            ServerListener serverListener = new ServerListener(input, this);
            new Thread(serverListener).start();

            handleUserInput();
        }

        catch (IOException e) {
            System.err.println("Unable to connect to server at " + serverAddress + ":" + port);
        }
    }

    /**
     * Sends a connect message to the server.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void sendConnectMessage() throws IOException {
        output.writeInt(MessageType.CONNECT_MESSAGE.getValue());
        MessageProtocol.writeString(output, username);
    }

    /**
     * Handles user input from the console.
     */
    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Type '?' for help.");

        while (isConnected) {
            if (!scanner.hasNextLine()) {
                break;
            }
            String userInput = scanner.nextLine();
            parseCommand(userInput);
        }
        scanner.close();
    }

    /**
     * Parses user commands and sends appropriate messages to the server.
     *
     * @param input The user's input string.
     */
    private void parseCommand(String input) {
        try {
            if (input.equals("?")) {
                displayHelp();
            }

            else if (input.equalsIgnoreCase("logoff")) {
                sendDisconnectMessage();
            }

            else if (input.equalsIgnoreCase("who")) {
                System.out.println("Sending query users message...");
                sendQueryUsersMessage();
            }

            else if (input.startsWith("@")) {
                handleAtCommand(input);
            }

            else if (input.startsWith("!")) {
                handleExclamationCommand(input);
            }

            else {
                System.out.println("Unknown command. Type '?' for help.");
            }
        }

        catch (IOException e) {
            System.err.println("Error sending message to server.");
        }
    }

    /**
     * Handles commands starting with '@'.
     *
     * @param input The user's input string.
     * @throws IOException If an I/O error occurs.
     */
    private void handleAtCommand(String input) throws IOException {
        if (input.startsWith("@all")) {
            String message = input.substring(4).trim();

            if (message.isEmpty()) {
                System.out.println("Message cannot be empty.");
            }

            else {
                sendBroadcastMessage(message);
            }
        }

        else {
            int spaceIndex = input.indexOf(' ');

            if (spaceIndex > 1) {
                String recipient = input.substring(1, spaceIndex);
                String message = input.substring(spaceIndex + 1);

                if (message.isEmpty()) {
                    System.out.println("Message cannot be empty.");
                }

                else {
                    sendDirectMessage(recipient, message);
                }
            }

            else {
                System.out.println("Invalid direct message format. Use '@username message'.");
            }
        }
    }

    /**
     * Handles commands starting with '!'.
     *
     * @param input The user's input string.
     * @throws IOException If an I/O error occurs.
     */
    private void handleExclamationCommand(String input) throws IOException {
        String recipient = input.substring(1).trim();

        if (recipient.isEmpty()) {
            System.out.println("Recipient username is required. Use '!username'.");
        }

        else {
            sendInsultMessage(recipient);
        }
    }

    /**
     * Displays the help menu.
     */
    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("?: Display this help menu");
        System.out.println("logoff: Disconnect from the server");
        System.out.println("who: List connected users");
        System.out.println("@all message: Send a message to all users");
        System.out.println("@username message: Send a direct message to a user");
        System.out.println("!username: Send a random insult to a user");
    }

    /**
     * Sends a disconnect message to the server.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void sendDisconnectMessage() throws IOException {
        output.writeInt(MessageType.DISCONNECT_MESSAGE.getValue());
        MessageProtocol.writeString(output, username);
        isConnected = false;
    }

    /**
     * Sends a query connected users message to the server.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void sendQueryUsersMessage() throws IOException {
        output.writeInt(MessageType.QUERY_CONNECTED_USERS.getValue());
        MessageProtocol.writeString(output, username);
    }

    /**
     * Sends a broadcast message to the server.
     *
     * @param message The message to send.
     * @throws IOException If an I/O error occurs.
     */
    private void sendBroadcastMessage(String message) throws IOException {
        output.writeInt(MessageType.BROADCAST_MESSAGE.getValue());
        MessageProtocol.writeString(output, username);
        MessageProtocol.writeString(output, message);
    }

    /**
     * Sends a direct message to a specific user.
     *
     * @param recipient The recipient's username.
     * @param message   The message to send.
     * @throws IOException If an I/O error occurs.
     */
    private void sendDirectMessage(String recipient, String message) throws IOException {
        output.writeInt(MessageType.DIRECT_MESSAGE.getValue());
        MessageProtocol.writeString(output, username);
        MessageProtocol.writeString(output, recipient);
        MessageProtocol.writeString(output, message);
    }

    /**
     * Sends a send insult message to the server.
     *
     * @param recipient The recipient's username.
     * @throws IOException If an I/O error occurs.
     */
    private void sendInsultMessage(String recipient) throws IOException {
        output.writeInt(MessageType.SEND_INSULT.getValue());
        MessageProtocol.writeString(output, username);
        MessageProtocol.writeString(output, recipient);
    }

    /**
     * Gets the client's username.
     *
     * @return The client's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the connection status of the client.
     *
     * @param connected The connection status.
     */
    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    /**
     * Disconnects from the server and closes resources.
     */
    public void disconnect() {
        isConnected = false;

        try {
            input.close();
            output.close();
            serverSocket.close();
            System.out.println("Disconnected from server.");
        }

        catch (IOException e) {
            System.err.println("Error closing client resources.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatClient that = (ChatClient) o;

        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ChatClient{" +
                "username='" + username + '\'' +
                ", serverAddress='" + serverAddress + '\'' +
                ", port=" + port +
                '}';
    }
}