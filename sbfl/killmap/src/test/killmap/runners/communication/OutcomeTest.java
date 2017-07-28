package killmap.runners.communication;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Request;

import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.equalTo;

import killmap.TestMethod;
import killmap.runners.communication.Outcome;
import killmap.runners.communication.WorkOrder;

public class OutcomeTest extends TestCase {

  public static class DummyTestCase extends TestCase {
    @Test public void testThatPasses() {}
    @Test public void testThatFails() {assertEquals(0, 1);}
  }

  public static String csvLine(String status, String runTime, String digest, String coveredMutants, String stackTrace) {
    return status+","+runTime+","+digest+","+coveredMutants+","+stackTrace;
  }

  public static WorkOrder dummyWorkOrder(Integer mutantId, Long timeout) {
    TestMethod method = new TestMethod(DummyTestCase.class, "testThatPasses");
    return new WorkOrder(method, mutantId, timeout);
  }

  public static Result passingJUnitResult() {
    return (new JUnitCore()).run(Request.method(OutcomeTest.DummyTestCase.class, "testThatPasses"));
  }

  public static Result failingJUnitResult() {
    return (new JUnitCore()).run(Request.method(OutcomeTest.DummyTestCase.class, "testThatFails"));
  }

  @Test public void testCrashToString() {
    assertEquals(csvLine("CRASH","-1","","",""), Outcome.createCrash().toString());
  }

  @Test public void testTimeoutToString() {
    assertEquals(csvLine("TIMEOUT","123","","",""), Outcome.createTimeout(dummyWorkOrder(0, (long)123)).toString());
  }

  @Test public void testPassToString() {
    Result result = passingJUnitResult();
    Outcome o = new Outcome(result, "0123456789abcdef");
    assertEquals(
      csvLine("PASS",Long.toString(result.getRunTime()),"0123456789abcdef","",""),
      o.toString());
    o.coveredMutants.add(10); o.coveredMutants.add(11);
    assertEquals(
      csvLine("PASS",Long.toString(result.getRunTime()),"0123456789abcdef","10 11",""),
      o.toString());
  }

  @Test public void testFailToString() {
    Result result = failingJUnitResult();
    Outcome o = new Outcome(result, "0123456789abcdef");
    assertEquals(
      csvLine("FAIL",Long.toString(result.getRunTime()),"0123456789abcdef","",Outcome.normalizeStackTrace(result.getFailures().get(0).getTrace())),
      o.toString());
  }

  @Test public void testNormalizeStackTrace() {
    assertEquals("", Outcome.normalizeStackTrace(""));
    assertEquals("failure message", Outcome.normalizeStackTrace("  \nfailure message\t\r\n "));
    assertEquals("one two three", Outcome.normalizeStackTrace("one  \n \ttwo\r\n\t three\n"));
  }

  private static void assertEqualsAndHashEquals(Object x, Object y) {
    assertEquals(x, y);
    assertEquals(x.hashCode(), y.hashCode());
  }
  @Test public void testEquality() {
    Result passingResult = failingJUnitResult();
    assertEqualsAndHashEquals(new Outcome(passingResult, "digest"), new Outcome(passingResult, "digest"));
    assertThat(new Outcome(passingResult, "a"), not(equalTo(new Outcome(passingResult, "b"))));

    Result failingResult = passingJUnitResult();
    assertEqualsAndHashEquals(new Outcome(failingResult, "digest"), new Outcome(failingResult, "digest"));
    assertThat(new Outcome(failingResult, "a"), not(equalTo(new Outcome(failingResult, "b"))));
    assertThat(new Outcome(passingResult, "a"), not(equalTo(new Outcome(failingResult, "a"))));

    assertEqualsAndHashEquals(Outcome.createCrash(), Outcome.createCrash());
    assertThat(new Outcome(passingResult, ""), not(equalTo(Outcome.createCrash())));
    assertThat(new Outcome(failingResult, ""), not(equalTo(Outcome.createCrash())));

    WorkOrder workOrder = dummyWorkOrder(3, (long)123);
    assertEqualsAndHashEquals(Outcome.createTimeout(workOrder), Outcome.createTimeout(workOrder));
    assertThat(Outcome.createTimeout(workOrder), not(equalTo(Outcome.createTimeout(dummyWorkOrder(workOrder.mutantId, workOrder.timeout+1)))));
    assertThat(new Outcome(passingResult, ""), not(equalTo(Outcome.createTimeout(workOrder))));
    assertThat(new Outcome(failingResult, ""), not(equalTo(Outcome.createTimeout(workOrder))));
    assertThat(Outcome.createCrash(), not(equalTo(Outcome.createTimeout(workOrder))));
  }

  @Test public void testFromStringIsInverseOfToString() {
    for (Outcome o : new Outcome[] {
                       new Outcome(passingJUnitResult(), "digest"),
                       new Outcome(failingJUnitResult(), "digest"),
                       Outcome.createCrash(),
                       Outcome.createTimeout(dummyWorkOrder(1, (long)1))}) {
      assertEquals(o, Outcome.fromString(o.toString()));
    }
  }

}