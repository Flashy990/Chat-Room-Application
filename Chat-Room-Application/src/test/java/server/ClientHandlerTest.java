package server;

import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {
  private ChatServer server;
  private ClientHandler clientHandler;
  private ByteArrayInputStream inputStream;
  private ByteArrayOutputStream outputStream;
  private DataInputStream dataInputStream;
  private DataOutputStream dataOutputStream;
  private Socket mockSocket;
  private final String USERNAME = "TestUser";

  private ClientHandler clientHandler1;
  private ClientHandler clientHandler2;
  private ClientHandler clientHandler3;
  private ClientHandler clientHandlerNotEquals;

  private final int PORT_NUMBER = 8000;
  @BeforeEach
  void setUp() throws Exception {
    server = new ChatServer(PORT_NUMBER);

    inputStream = new ByteArrayInputStream(new byte[1024]);
    outputStream = new ByteArrayOutputStream();
    dataInputStream = new DataInputStream(inputStream);
    dataOutputStream = new DataOutputStream(outputStream);

    mockSocket = new Socket() {
      @Override
      public DataInputStream getInputStream() {
        return dataInputStream;
      }

      @Override
      public DataOutputStream getOutputStream() {
        return dataOutputStream;
      }
    };


    clientHandler = new ClientHandler(mockSocket, server);
    clientHandler1 = new ClientHandler(mockSocket, server);
    clientHandler2 = new ClientHandler(mockSocket, server);
    clientHandler3 = new ClientHandler(mockSocket, server);
    clientHandlerNotEquals = new ClientHandler(new Socket(), server);
  }

  @AfterEach
  void tearDown() throws Exception {
    clientHandler = null;
    server = null;
    mockSocket.close();
  }

  @Test
  void testHandleConnectMessage_UsernameTaken() throws Exception {
    server.addClient(new ConnectedClient(USERNAME, mockSocket, dataOutputStream));

    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
    DataOutputStream tempOutput = new DataOutputStream(byteArray);

    tempOutput.writeInt(19);
    tempOutput.writeInt(USERNAME.length());
    tempOutput.writeBytes(USERNAME);

    inputStream = new ByteArrayInputStream(byteArray.toByteArray());
    dataInputStream = new DataInputStream(inputStream);
    clientHandler = new ClientHandler(mockSocket, server);

    clientHandler.run();
    String response = new String(outputStream.toByteArray());
    assertTrue(response.contains("Username already taken"));
  }

  @Test
  void testHandleUnknownMessageType() throws Exception {
    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
    DataOutputStream tempOutput = new DataOutputStream(byteArray);

    tempOutput.writeInt(99);

    inputStream = new ByteArrayInputStream(byteArray.toByteArray());
    dataInputStream = new DataInputStream(inputStream);
    clientHandler = new ClientHandler(mockSocket, server);

    clientHandler.run();
    String response = new String(outputStream.toByteArray());
    assertTrue(response.contains("Unknown message type"));
  }
  @Test
  void testEqualsBasic() {
    assertTrue(clientHandler1.equals(clientHandler2));
    assertFalse(clientHandler1.equals(clientHandlerNotEquals));
  }

  @Test
  void testEqualsNull() {
    assertFalse(clientHandler1.equals(null));
  }

  @Test
  void testEquals1() {
    assertTrue(clientHandler1.equals(clientHandler1));
  }

  @Test
  void testEquals2() {
    assertTrue(clientHandler1.equals(clientHandler2));
    assertTrue(clientHandler2.equals(clientHandler1));
  }

  @Test
  void testEquals3() {
    assertTrue(clientHandler1.equals(clientHandler2));
    assertTrue(clientHandler2.equals(clientHandler3));
    assertTrue(clientHandler3.equals(clientHandler1));
  }

  @Test
  void testDifferentClassObject() {
    assertFalse(clientHandler1.equals(new Object()));
  }

  @Test
  void testDifferentArgs() {
    assertFalse(clientHandler1.equals(clientHandlerNotEquals));
  }

  @Test
  void testHashCodeBasic() {
    assertEquals(clientHandler1.hashCode(), clientHandler2.hashCode());
    assertNotEquals(clientHandler1.hashCode(), clientHandlerNotEquals.hashCode());
  }
  @Test
  public void testToString() {
    String toStringResult = clientHandler1.toString();
    assertNotNull(toStringResult);
    assertTrue(toStringResult.contains("username='"));
    assertTrue(toStringResult.contains("clientSocket="));

  }
}
