package server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InsultGeneratorTest {
  private InsultGenerator insultGenerator;

  private InsultGenerator insultGenerator1;
  private InsultGenerator insultGenerator2;
  @BeforeEach
  void setUp() {
    insultGenerator = new InsultGenerator();
    insultGenerator1 = new InsultGenerator();
    insultGenerator2 = new InsultGenerator();
  }
  @Test
  public void testGenerateInsult() {


    String insult = insultGenerator.generateInsult();
    assertNotNull(insult);
    assertFalse(insult.isEmpty());
  }

  @Test
  void testEqualsSameObject() {
    assertEquals(insultGenerator1, insultGenerator1);
  }

  @Test
  void testEqualsNullObject() {
    assertNotEquals(insultGenerator1, null);
  }

  @Test
  void testEqualsDifferentClass() {
    assertNotEquals(insultGenerator1, new Object());
  }

  @Test
  void testEqualsDifferentInstances() {
    assertEquals(insultGenerator1, insultGenerator2);
  }

  @Test
  void testHashCode() {
    assertEquals(insultGenerator1.hashCode(), insultGenerator2.hashCode());
  }


  @Test
  public void testToString() {
    InsultGenerator insultGenerator = new InsultGenerator();
    String toStringResult = insultGenerator.toString();
    String EXPECTED_STRING = "insults";

    assertNotNull(toStringResult);
    assertTrue(toStringResult.contains(EXPECTED_STRING));
  }
}