package util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility class providing methods to encode and decode messages according to the protocol.
 */
public class MessageProtocol {

    /**
     * Writes a string to the output stream, preceded by its length.
     *
     * @param output The DataOutputStream to write to.
     * @param data   The string data to write.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeString(DataOutputStream output, String data) throws IOException {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        output.writeInt(bytes.length);
        output.write(bytes);
    }

    /**
     * Reads a string from the input stream, based on its length.
     *
     * @param input The DataInputStream to read from.
     * @return The string read from the input stream.
     * @throws IOException If an I/O error occurs.
     */
    public static String readString(DataInputStream input) throws IOException {
        int length = input.readInt();
        byte[] bytes = new byte[length];
        input.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Writes an integer value to the output stream.
     *
     * @param output The DataOutputStream to write to.
     * @param value  The integer value to write.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeInt(DataOutputStream output, int value) throws IOException {
        output.writeInt(value);
    }

    /**
     * Reads an integer value from the input stream.
     *
     * @param input The DataInputStream to read from.
     * @return The integer value read from the input stream.
     * @throws IOException If an I/O error occurs.
     */
    public static int readInt(DataInputStream input) throws IOException {
        return input.readInt();
    }

    /**
     * Writes a boolean value to the output stream.
     *
     * @param output The DataOutputStream to write to.
     * @param value  The boolean value to write.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeBoolean(DataOutputStream output, boolean value) throws IOException {
        output.writeBoolean(value);
    }

    /**
     * Reads a boolean value from the input stream.
     *
     * @param input The DataInputStream to read from.
     * @return The boolean value read from the input stream.
     * @throws IOException If an I/O error occurs.
     */
    public static boolean readBoolean(DataInputStream input) throws IOException {
        return input.readBoolean();
    }
}