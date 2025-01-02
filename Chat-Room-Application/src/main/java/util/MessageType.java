package util;

/**
 * Enum representing the different types of messages used in the chat protocol.
 */
public enum MessageType {
    CONNECT_MESSAGE(19),
    CONNECT_RESPONSE(20),
    DISCONNECT_MESSAGE(21),
    QUERY_CONNECTED_USERS(22),
    QUERY_USER_RESPONSE(23),
    BROADCAST_MESSAGE(24),
    DIRECT_MESSAGE(25),
    FAILED_MESSAGE(26),
    SEND_INSULT(27);

    private final int value;

    /**
     * Constructs a MessageType with the specified integer value.
     *
     * @param value The integer value of the message type.
     */
    MessageType(int value) {
        this.value = value;
    }

    /**
     * Gets the integer value of the message type.
     *
     * @return The integer value of the message type.
     */
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "MessageType{" +
                "name=" + this.name() +
                ", value=" + value +
                '}';
    }
}