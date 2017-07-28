/* Represents the result of a test-run.
 * Records: the general kind of outcome (pass/fail/timeout/crash)
 *          the hashes of what was printed to stdout/stderr
 *          how long the test took to run
 *          the stack trace of the failure (or "")
 *          the set of covered mutants
 */

package killmap.runners.communication;

import org.junit.runner.Result;

import java.util.Collection;
import java.util.ArrayList;

public class Outcome {
  public static enum Type {
    PASS, FAIL, TIMEOUT, CRASH;
  }

  public Type type;
  public String digest;
  public Long runTime;
  public String stackTrace;
  public Collection<Integer> coveredMutants;

  public static String normalizeStackTrace(String stackTrace) {
    // Given a multi-line stack trace, crushes it down to one line
    // and removes potentially-variable formatting.
    // Ideally, any two stack traces which represent "the same error"
    // should be normalized to identical strings.
    return stackTrace.replaceAll(" *\r?\n[ \t]*", " ") // kill newlines and surrounding space
                     .replaceAll("^[ \t\r\n]*|[ \t\r\n]*$", ""); // strip whitespace
  }

  public Outcome(Result result, String digest_) {
    // Constructs an Outcome from a JUnit result.
    // Only PASS/FAIL outcomes can be made this way: if a test times out
    // or crashes the JVM, there won't be a JUnit result to base the Outcome on.
    // In those cases, use createTimeout or createCrash.
    digest = digest_;
    coveredMutants = new ArrayList<Integer>();
    if (result.wasSuccessful()) {
      type = Type.PASS;
      runTime = result.getRunTime();
      stackTrace = "";
    } else {
      type = Type.FAIL;
      runTime = result.getRunTime();
      stackTrace = normalizeStackTrace(result.getFailures().get(0).getTrace());
    }
  }

  private Outcome(Type type_) {
    type = type_;
    digest = "";
    runTime = (long)-1;
    stackTrace = "";
    coveredMutants = new ArrayList<Integer>();
  }

  public static Outcome createTimeout(WorkOrder workOrder) {
    Outcome result = new Outcome(Type.TIMEOUT);
    result.runTime = workOrder.timeout;
    return result;
  }

  public static Outcome createCrash() {
    return new Outcome(Type.CRASH);
  }

  @Override
  public boolean equals(Object otherObj) {
    Outcome other = (Outcome)otherObj;
    return type == other.type && digest.equals(other.digest) && runTime.equals(other.runTime) && stackTrace.equals(other.stackTrace) && coveredMutants.equals(other.coveredMutants);
  }

  @Override
  public int hashCode() {
    return type.hashCode() + digest.hashCode() + runTime.hashCode() + stackTrace.hashCode() + coveredMutants.hashCode();
  }

  @Override
  public String toString() {
    // Converts the Outcome into a string suitable for a line in a CSV file:
    //   type,runTime,mutants,stackTrace
    // where type is in {PASS,FAIL,TIMEOUT,CRASH}
    //       runTime is how long the test took to run in ms (or -1 for crashes)
    //       mutants is a space-separated list of the covered mutants
    //       stackTrace is the normalized stack trace of the failure (or empty)
    // (Note: the stack trace may contain commas, so to parse the CSV you should
    //  only split on the first 3 commas.)
    String mutants = "";
    for (Integer m : coveredMutants) {
      mutants = mutants + m + " ";
    }
    if (mutants.length() > 0) mutants = mutants.substring(0, mutants.length()-1);
    return type.toString() + "," + runTime.toString() + "," + digest.toString() + "," + mutants + "," + stackTrace.toString();
  }

  public static Outcome fromString(String s) throws IllegalArgumentException {
    // Inverse of toString(), i.e.
    //   forall o:Outcome,
    //   Outcome.fromString(o.toString()).equals(o)

    String[] type_runTime_digest_mutants_stackTrace = s.split(",", 5);
    if (!(type_runTime_digest_mutants_stackTrace.length == 5))
      throw new IllegalArgumentException(s);
    Type type = Type.valueOf(type_runTime_digest_mutants_stackTrace[0]);
    Long runTime = Long.parseLong(type_runTime_digest_mutants_stackTrace[1]);
    String digest = type_runTime_digest_mutants_stackTrace[2];
    String[] mutantStrs = type_runTime_digest_mutants_stackTrace[3].split(" ");
    String stackTrace = type_runTime_digest_mutants_stackTrace[4];

    ArrayList<Integer> coveredMutants = new ArrayList<Integer>();
    for (String mutantStr : mutantStrs) {
      if (mutantStr.matches("[0-9]+")) {
        coveredMutants.add(Integer.parseInt(mutantStr));
      }
    }

    Outcome result = new Outcome(type);
    result.runTime = runTime;
    result.digest = digest;
    result.stackTrace = stackTrace;
    result.coveredMutants = coveredMutants;

    return result;
  }
}