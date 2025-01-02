package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

/**
 * Represents a connected client with their username and output stream.
 */
public class ConnectedClient {
    private String username;
    private Socket socket;
    private DataOutputStream output;

    /**
     * Constructs a ConnectedClient with the specified username, socket, and output stream.
     *
     * @param username The username of the client.
     * @param socket   The client's socket connection.
     * @param output   The DataOutputStream to send messages to the client.
     */
    public ConnectedClient(String username, Socket socket, DataOutputStream output) {
        this.username = username;
        this.socket = socket;
        this.output = output;
    }

    /**
     * Gets the username of the client.
     *
     * @return The client's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sends a message to the client in a thread-safe manner.
     *
     * @param messageType The type of message to send.
     * @param messageData The message data to send.
     * @throws IOException If an I/O error occurs.
     */
    public synchronized void sendMessage(int messageType, byte[] messageData) throws IOException {
        output.writeInt(messageType);
        output.write(messageData);
        output.flush();
    }

    /**
     * Closes the client's socket and output stream.
     */
    public void close() {
        try {
            output.close();
            socket.close();
        }

        catch (IOException e) {
            System.err.println("Error closing client resources for " + username);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectedClient that = (ConnectedClient) o;

        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ConnectedClient{" +
                "username='" + username + '\'' +
                ", socket=" + socket +
                '}';
    }
}