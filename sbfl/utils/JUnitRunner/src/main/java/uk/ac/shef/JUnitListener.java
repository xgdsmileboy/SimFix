
package uk.ac.shef;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Listener for JUnit test cases
 */
public class JUnitListener extends RunListener {

  /**
   * 
   */
  public JUnitListener() {
    super();
  }

  /**
   * Called before any tests have been run
   */
  @Override
  public void testRunStarted(Description description) {
    System.out.println("* Number of test cases to execute: " + description.testCount());
  }

  /**
   * Called when all tests have finished
   */
  @Override
  public void testRunFinished(Result result) {
    System.out.println("* Number of test cases executed: " + result.getRunCount());
  }

  /**
   * Called when an atomic test is about to be started
   */
  @Override
  public void testStarted(Description description) {
    System.out.println("* Started " + getName(description));
  }

  /**
   * Called when an atomic test has finished. whether the test successes or fails
   */
  @Override
  public void testFinished(Description description) {
    System.out.println("* Finished " + getName(description));
  }

  /**
   * Called when an atomic test fails
   */
  @Override
  public void testFailure(Failure failure) {
    // --- org.apache.commons.lang.EntitiesPerformanceTest::testEscapePrimitive
    System.out.println("--- " + failure.getDescription().getClassName() + "::" + failure.getDescription().getMethodName());
    System.out.println(failure.getTrace());
  }

  /**
   * Called when a test will not be run, generally because a test method is annotated with Ignore
   */
  @Override
  public void testIgnored(Description description) {
    System.out.println("* Ignored: " + getName(description));
  }

  private static String getName(Description description) {
    return description.getClassName() + "#" + description.getMethodName();
  }
}
