package addition.test;

import junit.framework.TestCase;
import org.junit.Test;

public class AdderTest extends TestCase {

  @Test
  public void testThatFailsSameWayOnAllMutants() {
    assertEquals(0, 1);
  }

  @Test
  public void testThatPassesOnAllMutants() {
    assertEquals(0, 0);
  }

  // JUnit doesn't seem to respect my timeouts...
  // @Test(timeout=10)
  // public void testThatTimesOut() throws InterruptedException {
  //   Thread.sleep(10000);
  // }

  @Test
  public void testThatFailsAndFailsDifferentlyWithMutants() {
    assertEquals(0, addition.Adder.add(1, 1));
  }

  @Test
  public void testThatPassesButFailsWithMutants() {
    assertEquals(2, addition.Adder.add(1, 1));
  }

  @Test
  public void testThatPassesButThrowsWithMutants() {
    assertEquals(1, addition.Adder.add(1, 0)); // Mutants "+ => /" and "+ => %" should throw errors.
  }

}
