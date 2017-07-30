package killmap.runners;

import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.equalTo;

import killmap.TestMethod;
import killmap.runners.TestRunner;
import killmap.runners.communication.Outcome;
import killmap.runners.communication.WorkOrder;

public class TestRunnerTest extends TestCase {

  public static class DummyTest extends TestCase {
    public static boolean isFirstRun = true;
    @Test public void testThatPassesOnFirstRunOnly() {
      assertTrue(isFirstRun);
      isFirstRun = false;
    }

    @Test public void testThatPrintsNothing() {
    }
    @Test public void testThatPrintsToStdout() {
      System.out.println("hi");
    }
    @Test public void testThatPrintsToStderr() {
      System.err.println("hi");
    }
  }

  private static WorkOrder getWorkOrderForName(String testName) throws NoSuchMethodException {
    TestMethod test = new TestMethod(DummyTest.class, testName);
    return new WorkOrder(test, 0, (long)100);
  }

  @Test public void testSimpleBlockingRunTestIsSimple() throws Exception {
    DummyTest.isFirstRun = true;
    Outcome outcome1 = TestRunner.simpleBlockingRunTest(getWorkOrderForName("testThatPassesOnFirstRunOnly"));
    Outcome outcome2 = TestRunner.simpleBlockingRunTest(getWorkOrderForName("testThatPassesOnFirstRunOnly"));
    assertEquals(Outcome.Type.PASS, outcome1.type);
    assertEquals(Outcome.Type.FAIL, outcome2.type);
  }

  @Test public void testIsolation() throws Exception {
    Outcome outcome1 = TestRunner.isolatedBlockingRunTest(getWorkOrderForName("testThatPassesOnFirstRunOnly"));
    Outcome outcome2 = TestRunner.isolatedBlockingRunTest(getWorkOrderForName("testThatPassesOnFirstRunOnly"));
    assertEquals(Outcome.Type.PASS, outcome1.type);
    assertEquals(Outcome.Type.PASS, outcome2.type);
  }

  @Test public void testStreamCatching() throws Exception {
    Outcome reference = TestRunner.runTest(getWorkOrderForName("testThatPrintsNothing"));
    Outcome outOutcome = TestRunner.runTest(getWorkOrderForName("testThatPrintsToStdout"));
    Outcome errOutcome = TestRunner.runTest(getWorkOrderForName("testThatPrintsToStderr"));
    assertThat(reference.digest, not(equalTo(outOutcome.digest)));
    assertThat(reference.digest, not(equalTo(errOutcome.digest)));
  }
}
