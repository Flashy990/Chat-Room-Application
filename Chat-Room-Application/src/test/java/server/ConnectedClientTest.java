package server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConnectedClientTest {

  private final String testUsername = "TestUser";
  private final String testUsernameNotEquals = "TestUserNotEquals";
  private ByteArrayOutputStream byteArrayOutputStream;
  private Socket mockSocket;
  private ConnectedClient client;
  private ConnectedClient client1;
  private ConnectedClient client2;
  private ConnectedClient client3;
  private ConnectedClient clientNotEquals;
  private ConnectedClient clientNullUsername;
  @BeforeEach
  void setUp() {
    mockSocket = new Socket();
    byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream mockOutput = new DataOutputStream(byteArrayOutputStream);
    client = new ConnectedClient(testUsername, mockSocket, mockOutput);
    client1 = new ConnectedClient(testUsername, mockSocket, mockOutput);
    client2 = new ConnectedClient(testUsername, mockSocket, mockOutput);
    client3 = new ConnectedClient(testUsername, mockSocket, mockOutput);
    clientNotEquals = new ConnectedClient(testUsernameNotEquals, mockSocket, mockOutput);
    clientNullUsername= new ConnectedClient(null, mockSocket, mockOutput);
  }
  @Test
  public void testGetUsername() {

    assertEquals(testUsername, client.getUsername());
  }

  @Test
  public void testSendMessage() throws IOException {

    int messageType = 1;
    String MESSAGE = "Hello There!";
    byte[] messageData = MESSAGE.getBytes();

    client.sendMessage(messageType, messageData);

    DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

    assertEquals(messageType, inputStream.readInt());

    byte[] receivedData = new byte[messageData.length];
    inputStream.readFully(receivedData);
    assertArrayEquals(messageData, receivedData);
  }

  @Test
  public void testClose() throws IOException {

    client.close();

    assertTrue(mockSocket.isClosed());
  }
  @Test
  void testEqualsBasic() {
    assertTrue(client1.equals(client2));
    assertFalse(client1.equals(clientNotEquals));
  }

  @Test
  void testEqualsNull() {
    assertFalse(client1.equals(null));
  }

  @Test
  void testEquals1() {
    assertTrue(client1.equals(client1));
  }

  @Test
  void testEquals2() {
    assertTrue(client1.equals(client2));
    assertTrue(client2.equals(client1));
  }

  @Test
  void testEquals3() {
    assertTrue(client1.equals(client2));
    assertTrue(client2.equals(client3));
    assertTrue(client3.equals(client1));
  }

  @Test
  void testDifferentClassObject() {
    assertFalse(client1.equals(new Object()));
  }

  @Test
  void testDifferentArgs() {
    assertFalse(client1.equals(clientNotEquals));
  }

  @Test
  void testHashCodeBasic() {
    assertEquals(client1.hashCode(), client2.hashCode());
    assertNotEquals(client1.hashCode(), clientNotEquals.hashCode());
    assertEquals(0, clientNullUsername.hashCode());
  }
  @Test
  public void testToString() {
    String toStringResult = client.toString();
    assertNotNull(toStringResult);
    assertTrue(toStringResult.contains("username='" + testUsername));
    assertTrue(toStringResult.contains("socket="));
  }
}