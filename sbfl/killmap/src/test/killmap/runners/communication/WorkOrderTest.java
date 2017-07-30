package killmap.runners.communication;

import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.equalTo;

import killmap.TestMethod;
import killmap.runners.communication.WorkOrder;

public class WorkOrderTest extends TestCase {

  public static class DummyTestCase extends TestCase {
    @Test public void testThatPasses() {}
    @Test public void testThatFails() {assertEquals(0, 1);}
  }

  private static TestMethod passingTest;
  private static TestMethod failingTest;
  static{
    passingTest = new TestMethod(DummyTestCase.class, "testThatPasses");
    failingTest = new TestMethod(DummyTestCase.class, "testThatFails");
  } 

  @Test public void testToString() {
    assertEquals(
      "killmap.runners.communication.WorkOrderTest$DummyTestCase#testThatPasses,3,100",
      (new WorkOrder(passingTest, 3, (long)100)).toString());
  }

  private static void assertEqualsAndHashEquals(Object x, Object y) {
    assertEquals(x, y);
    assertEquals(x.hashCode(), y.hashCode());
  }
  @Test public void testEquality() {
    assertEqualsAndHashEquals(new WorkOrder(passingTest, 0, (long)1), new WorkOrder(passingTest, 0, (long)1));
    assertThat(new WorkOrder(passingTest, 0, (long)1), not(equalTo(new WorkOrder(failingTest, 0, (long)1))));    
    assertThat(new WorkOrder(passingTest, 0, (long)1), not(equalTo(new WorkOrder(passingTest, 0, (long)0))));    
    assertThat(new WorkOrder(passingTest, 0, (long)1), not(equalTo(new WorkOrder(passingTest, 1, (long)1))));    
  }

  @Test public void testFromStringIsInverseOfToString() throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException {
    WorkOrder workOrder = new WorkOrder(passingTest, 3, (long)100);
    assertEquals(workOrder, WorkOrder.fromString(workOrder.toString()));
  }

}