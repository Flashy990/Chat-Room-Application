package util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MessageTypeTest {
  @Test
  public void testMessageTypeEnumValues() {
    for (MessageType type : MessageType.values()) {
      assertNotNull(type.name());
      assertNotNull(type.toString());
      assertTrue(type.toString().contains(type.name()));
      assertEquals(type, MessageType.valueOf(type.name()));
    }
  }
  @Test
  public void testMessageTypeEnumGetValueMethod() {
    assertEquals(19, MessageType.CONNECT_MESSAGE.getValue());
    assertEquals(20, MessageType.CONNECT_RESPONSE.getValue());
    assertEquals(21, MessageType.DISCONNECT_MESSAGE.getValue());
    assertEquals(22, MessageType.QUERY_CONNECTED_USERS.getValue());
    assertEquals(23, MessageType.QUERY_USER_RESPONSE.getValue());
    assertEquals(24, MessageType.BROADCAST_MESSAGE.getValue());
    assertEquals(25, MessageType.DIRECT_MESSAGE.getValue());
    assertEquals(26, MessageType.FAILED_MESSAGE.getValue());
    assertEquals(27, MessageType.SEND_INSULT.getValue());
  }

}