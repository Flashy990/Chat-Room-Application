package server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatServerTest {
  private final String testUsername = "TestUser";
  private final String testUser1 = "User1";
  private final String testUser2 = "User2";



  private final String MESSAGE = "Hello there!";
  private final String MESSAGE_USER1 = "Hello there User1!";
  private final String MESSAGE_USER2 = "Hello there User2!";
  private final int PORT_NUMBER = 9000;
  private final int PORT_NUMBER_NOT_EQUALS = 8080;
  private final String SENDER = "Server";
  private final String LOCALHOST = "localhost";

  private ChatServer server;
  private ChatServer server1;
  private ChatServer server2;
  private ChatServer server3;
  private ChatServer serverNotEquals;

  @BeforeEach
  void setUp() {
    server = new ChatServer(PORT_NUMBER);
    server1 = new ChatServer(PORT_NUMBER);
    server2 = new ChatServer(PORT_NUMBER);
    server3 = new ChatServer(PORT_NUMBER);
    serverNotEquals = new ChatServer(PORT_NUMBER_NOT_EQUALS);
  }

  @Test
  void testStartServerSuccess() {
    String FAILED_TO_CONNECT = "Failed to connect to the server: ";
    Thread serverThread = new Thread(server::start);
    serverThread.start();

    try (Socket socket = new Socket(LOCALHOST, PORT_NUMBER)) {
      assertTrue(socket.isConnected());
    } catch (IOException e) {
      fail(FAILED_TO_CONNECT + e.getMessage());
    } finally {
      serverThread.interrupt();
    }
  }

  @Test
  void testStartServerIOException() {
    String SETUP_FAILED = "Setup for IOException test failed: ";
    try (ServerSocket conflictingSocket = new ServerSocket(PORT_NUMBER)) {
      Thread serverThread = new Thread(server::start);
      serverThread.start();
      Thread.sleep(500);

      assertFalse(serverThread.isAlive());
    } catch (IOException | InterruptedException e) {
      fail(SETUP_FAILED + e.getMessage());
    }
  }

  @Test
  public void testAddAndRemoveClient() throws IOException {
    Socket mockSocket = new Socket();
    DataOutputStream mockOutput = new DataOutputStream(new ByteArrayOutputStream());

    ConnectedClient client = new ConnectedClient(testUsername, mockSocket, mockOutput);

    server.addClient(client);
    assertNotNull(server.getClientByUsername(testUsername));

    server.removeClient(client);
    assertNull(server.getClientByUsername(testUsername));
  }

  @Test
  public void testGetConnectedUsernames() throws IOException {
    Socket mockSocket1 = new Socket();
    Socket mockSocket2 = new Socket();
    DataOutputStream mockOutput1 = new DataOutputStream(new ByteArrayOutputStream());
    DataOutputStream mockOutput2 = new DataOutputStream(new ByteArrayOutputStream());

    ConnectedClient client1 = new ConnectedClient(testUser1, mockSocket1, mockOutput1);
    ConnectedClient client2 = new ConnectedClient(testUser2, mockSocket2, mockOutput2);

    server.addClient(client1);
    server.addClient(client2);

    List<String> usernames = server.getConnectedUsernames(testUser1);
    assertTrue(usernames.contains(testUser2));
    assertFalse(usernames.contains(testUser1));
  }

  @Test
  public void testBroadcastMessage() throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream mockOutput = new DataOutputStream(byteArrayOutputStream);
    Socket mockSocket = new Socket();

    ConnectedClient client = new ConnectedClient(testUsername, mockSocket, mockOutput);
    server.addClient(client);

    server.broadcastMessage(MESSAGE, SENDER);

    DataOutputStream outputStream = new DataOutputStream(new ByteArrayOutputStream());
    byte[] messageData = byteArrayOutputStream.toByteArray();
    assertTrue(messageData.length > 0);
  }

  @Test
  public void testDirectMessage() throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream mockOutput = new DataOutputStream(byteArrayOutputStream);
    Socket mockSocket = new Socket();

    ConnectedClient client = new ConnectedClient(testUser1, mockSocket, mockOutput);
    server.addClient(client);

    server.directMessage(MESSAGE_USER1, SENDER, testUser1);

    assertTrue(byteArrayOutputStream.size() > 0);
  }

  @Test
  public void testDirectMessageUserNotFound() throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream mockOutput = new DataOutputStream(byteArrayOutputStream);
    Socket mockSocket = new Socket();

    ConnectedClient sender = new ConnectedClient(testUser1, mockSocket, mockOutput);
    server.addClient(sender);

    server.directMessage(MESSAGE_USER2, testUser1, testUser2);

    assertTrue(byteArrayOutputStream.size() > 0);
  }
  @Test
  void testEqualsBasic() {
    assertTrue(server1.equals(server2));
    assertFalse(server1.equals(serverNotEquals));
  }

  @Test
  void testEqualsNull() {
    assertFalse(server1.equals(null));
  }

  @Test
  void testEquals1() {
    assertTrue(server1.equals(server1));
  }

  @Test
  void testEquals2() {
    assertTrue(server1.equals(server2));
    assertTrue(server2.equals(server1));
  }

  @Test
  void testEquals3() {
    assertTrue(server1.equals(server2));
    assertTrue(server2.equals(server3));
    assertTrue(server3.equals(server1));
  }

  @Test
  void testDifferentClassObject() {
    assertFalse(server1.equals(new Object()));
  }

  @Test
  void testDifferentArgs() {
    assertFalse(server1.equals(serverNotEquals));
  }

  @Test
  void testHashCodeBasic() {
    assertEquals(server1.hashCode(), server2.hashCode());
    assertNotEquals(server1.hashCode(), serverNotEquals.hashCode());
  }
  @Test
  public void testToString() {
    String toStringResult = server.toString();
    assertNotNull(toStringResult);
    assertTrue(toStringResult.contains("port=" + PORT_NUMBER));
  }
}