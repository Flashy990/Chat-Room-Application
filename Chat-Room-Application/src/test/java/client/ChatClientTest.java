package client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.ChatServer;
import server.ConnectedClient;

class ChatClientTest {
  private ChatClient client;
  private static ChatServer server;
  private static final String SERVER_ADDRESS = "localhost";
  private static final int SERVER_PORT = 8000;
  private static final String USERNAME = "myUser";

  private static final String COMMAND_HELP = "?";
  private static final String COMMAND_LOGOFF = "logoff";
  private static Thread serverThread;
  private final String testUsername = "TestUser";

  private ChatClient client1;
  private ChatClient client2;
  private ChatClient client3;
  private ChatClient clientNotEquals;
  private ChatClient clientNullUser;

  @BeforeAll
  static void setup() throws IOException {
    server = new ChatServer(SERVER_PORT);
    serverThread = new Thread(server::start);
    serverThread.start();
  }

  @BeforeEach
  void setUp() {
    client = new ChatClient(SERVER_ADDRESS, SERVER_PORT, USERNAME);
    client1 = new ChatClient(SERVER_ADDRESS, SERVER_PORT, USERNAME);
    client2 = new ChatClient(SERVER_ADDRESS, SERVER_PORT, USERNAME);
    client3 = new ChatClient(SERVER_ADDRESS, SERVER_PORT, USERNAME);
    clientNotEquals = new ChatClient(SERVER_ADDRESS, SERVER_PORT, testUsername);
    clientNullUser = new ChatClient(SERVER_ADDRESS, SERVER_PORT, null);

  }
  @AfterEach
  void tearDown() throws IOException {
    server.broadcastMessage("Server shutting down", "Server");
  }

  @AfterAll
  static void teardown() {
    serverThread.interrupt();
  }

  @Test
  void testGetUsername() {
    assertEquals(USERNAME, client.getUsername());
  }

  @Test
  void testSetConnected() {
    client.setConnected(true);
    assertTrue(client.isConnected());
  }


  @Test
  void testIsConnected() {
    assertFalse(client.isConnected());
  }
  @Test
  void testChatClientConnectsSuccessfully() throws IOException {
    client.start();
    assertTrue(client.isConnected());
    client.disconnect();
  }

  @Test
  void testChatClientHandlesInvalidServer() {
    ChatClient invalidClient = new ChatClient("invalid", 9999, USERNAME);
    invalidClient.start();
    assertFalse(invalidClient.isConnected());
  }

  @Test
  void testDisplayHelpCommand() throws IOException {
    ByteArrayInputStream in = new ByteArrayInputStream((COMMAND_HELP + System.lineSeparator()).getBytes());
    System.setIn(in);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    client.start();
    String output = out.toString();
    assertTrue(output.contains("Available commands:"));

    client.disconnect();
  }

  @Test
  void testLogoffCommand() throws IOException {
    ByteArrayInputStream in = new ByteArrayInputStream((COMMAND_LOGOFF + System.lineSeparator()).getBytes());
    System.setIn(in);
    client.start();
    assertFalse(client.isConnected());
  }

  @Test
  void testUnknownCommand() throws IOException {
    String unknownCommand = "some unknown command";

    ByteArrayInputStream in = new ByteArrayInputStream((unknownCommand + System.lineSeparator()).getBytes());
    System.setIn(in);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    client.start();
    String output = out.toString();
    assertTrue(output.contains("Unknown command. Type '?' for help."));

    client.disconnect();
  }

  @Test
  void testDirectMessageCommand() throws IOException {

    String directMessageCommand = "@bob Hello Bob!";
    ByteArrayInputStream in = new ByteArrayInputStream((directMessageCommand + System.lineSeparator()).getBytes());
    System.setIn(in);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    client.start();
    String output = out.toString();
    assertFalse(output.contains("Invalid direct message format"));

    client.disconnect();
  }

  @Test
  void testBroadcastMessageCommand() throws IOException {

    String broadcastMessageCommand = "@all Hello Everyone!";
    ByteArrayInputStream in = new ByteArrayInputStream((broadcastMessageCommand + System.lineSeparator()).getBytes());
    System.setIn(in);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    client.start();
    String output = out.toString();
    assertFalse(output.contains("Message cannot be empty."));

    client.disconnect();
  }
  @Test
  void testBroadcastEmptyMessageCommand() throws IOException {

    String broadcastMessageCommand = "@all";
    ByteArrayInputStream in = new ByteArrayInputStream((broadcastMessageCommand + System.lineSeparator()).getBytes());
    System.setIn(in);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    client.start();
    String output = out.toString();
    assertTrue(output.contains("Message cannot be empty."));

    client.disconnect();
  }

  @Test
  void testInsultCommand() throws IOException {

    Socket mockSocket = new Socket();
    DataOutputStream mockOutput = new DataOutputStream(new ByteArrayOutputStream());

    ConnectedClient userClient = new ConnectedClient(testUsername, mockSocket, mockOutput);

    server.addClient(userClient);

    String insultCommand = "!"+testUsername;
    ByteArrayInputStream in = new ByteArrayInputStream((insultCommand + System.lineSeparator()).getBytes());
    System.setIn(in);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    client.start();
    String output = out.toString();

    assertFalse(output.contains("Recipient username is required. Use '!username'."));

    client.disconnect();
  }
  @Test
  void testInsultNoReceiverCommand() throws IOException {

    String insultCommand = "!";
    ByteArrayInputStream in = new ByteArrayInputStream((insultCommand + System.lineSeparator()).getBytes());
    System.setIn(in);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    client.start();
    String output = out.toString();

    assertTrue(output.contains("Recipient username is required. Use '!username'."));

    client.disconnect();
  }
  @Test
  void testWhoCommand() throws IOException {

    String queryCommand = "who";
    ByteArrayInputStream in = new ByteArrayInputStream((queryCommand + System.lineSeparator()).getBytes());
    System.setIn(in);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    client.start();
    String output = out.toString();

    assertTrue(output.contains("Sending query users message..."));

    client.disconnect();
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
  }
  @Test
  void testHashCodeNullUser() {
    assertEquals(0, clientNullUser.hashCode());
  }
  @Test
  public void testToString() {
    String toStringResult = client1.toString();
    assertNotNull(toStringResult);
    assertTrue(toStringResult.contains("username='" + USERNAME));
    assertTrue(toStringResult.contains("port=" + SERVER_PORT));
  }
}