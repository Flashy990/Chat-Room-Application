package util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class MessageProtocolTest {
  private static final String MESSAGE = "Hi, this is a test message!";
  @Test
  void testWriteAndReadString() throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    MessageProtocol.writeString(dataOutputStream, MESSAGE);

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

    String readMessage = MessageProtocol.readString(dataInputStream);
    assertEquals(MESSAGE, readMessage);
  }

  @Test
  void testWriteAndReadInt() throws IOException {
    int testInt = 12345;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    MessageProtocol.writeInt(dataOutputStream, testInt);

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

    int readInt = MessageProtocol.readInt(dataInputStream);
    assertEquals(testInt, readInt);
  }
  @Test
  public void testWriteAndReadBoolean() throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    boolean testBoolean = true;
    MessageProtocol.writeBoolean(dataOutputStream, testBoolean);

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

    boolean result = MessageProtocol.readBoolean(dataInputStream);

    assertEquals(testBoolean, result);
  }

  @Test
  public void testReadStringEmpty() throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    String emptyString = "";
    MessageProtocol.writeString(dataOutputStream, emptyString);

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

    String result = MessageProtocol.readString(dataInputStream);

    assertEquals(emptyString, result);
  }

  @Test
  public void testReadStringInvalidLength() {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[] { 0, 0, 0, -1 }); // Invalid length
    DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

    assertThrows(IOException.class, () -> {
      MessageProtocol.readString(dataInputStream);
    });
  }
}