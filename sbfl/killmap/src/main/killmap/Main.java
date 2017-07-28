/* Builds a ((test,mutant) => outcome) matrix and prints it to stdout.
 *
 * Usage:
 *   java -cp ...:killmap/bin:$DEFECTS4J_HOME/major/config/config.jar:$DEFECTS4J_HOME/major/lib/junit-4.11.jar killmap.Main triggering-tests.txt relevant-test-classes.txt partial-run.csv
 *
 * The printed lines look like
 *   mypackage.MyTestClass#testMethod,<mutantId>,<timeout>,<summary>,<runtime>,<digest>,<mutants>,<message>
 * where <mutantId> (e.g. "0", "4") is which mutant is enabled
 *       <timeout> (e.g. "100", "376") is the time allotted for the test to run
 *       <summary> (i.e. "PASS", "FAIL", "TIMEOUT" or "CRASH") is the general
 *         category the outcome lies in (CRASH is if the worker JVM dies or seems to hang)
 *       <runtime> (e.g. "100", "376") is how long the test actually took to run
 *       <digest> (e.g. "d5a89...443f") is a hash of what the test wrote to stdout/stderr
 *       <mutants> (e.g. "", "2 5 8 ") is a space-separated list of mutants covered
 *         by the test (maybe followed by a space), if the mutant number is 0;
 *         or "" otherwise
 *       <message> (e.g. "", "java.lang.IOException: ...") is the failure message,
 *         if any
 *
 * This is almost definitely more information than we currently think we need.
 *
 * (Also logs some information to stderr.)
 */

package killmap;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.Arrays;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashSet;

import java.io.IOException;

import killmap.runners.RemoteTestRunner;
import killmap.runners.communication.Outcome;
import killmap.runners.communication.WorkOrder;

public class Main {

  private static final Long TIMEOUT_FOR_UNMUTATED_TESTS = (long)60000;

  private static long timeoutFromOriginalRunTime(long originalRunTime) {
    // If an unmutated test originally took ____ms, how long should we allow the mutated versions?
    if (originalRunTime < 25) return 200;
    if (originalRunTime < 50) return 16*originalRunTime;
    if (originalRunTime < 100) return 8*originalRunTime;
    if (originalRunTime < 200) return 4*originalRunTime;
    return 2*originalRunTime;
  }

  private static Outcome runOrExit(RemoteTestRunner runner, WorkOrder workOrder) {
    Outcome cachedResult = cache.tryGet(workOrder);
    if (cachedResult != null) {
      System.out.println(workOrder+","+cachedResult);
      return cachedResult;
    }
    try {
      Outcome result = runner.runTest(workOrder);
      System.out.println(workOrder+","+result);
      return result;
    } catch (RemoteTestRunner.WorkerCreationError e) {
      System.err.println("error creating child process");
      e.printStackTrace();
      System.exit(1);
    } catch (RemoteTestRunner.WorkerCommunicationError e) {
      System.err.println("error communicating with child process");
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  private static Collection<TestMethod> getTestsOrExit(String testClassNamesFileName) {
    try {
      return TestFinder.getTestsFromTestClassNameFile(testClassNamesFileName);
    } catch (IOException|ClassNotFoundException|NoSuchMethodException e) {
      System.err.println("failed to load tests");
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  private static Collection<TestMethod> getTriggeringTestsOrExit(String testNamesFileName) {
    try {
      return TestFinder.parseTriggeringTestsFile(testNamesFileName);
    } catch (IOException|ClassNotFoundException|NoSuchMethodException e) {
      System.err.println("failed to load triggering tests");
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  private static RemoteTestRunner createRunnerOrExit() {
    try {
      return new RemoteTestRunner();
    } catch (IOException e) {
      System.err.println("unable to open socket");
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  private static void closeRunnerOrExit(RemoteTestRunner runner) {
    try {
      runner.close();
    } catch (IOException e) {
      System.err.println("unable to close runner");
      e.printStackTrace();
      System.exit(1);
    }
  }


  public static Collection<Integer> behaviorChangingMutants(Outcome originalOutcome, Map<WorkOrder,Outcome> outcomes) {
    // Given a bunch of results for a test with various mutants enabled,
    // figure out which mutants had any significant effect on the test outcome.
    Collection<Integer> result = new HashSet<Integer>();
    for (WorkOrder order : outcomes.keySet()) {
      Outcome outcome = outcomes.get(order);
      if (!(outcome.type.equals(originalOutcome.type) && outcome.stackTrace.equals(originalOutcome.stackTrace))) {
        result.add(order.mutantId);
      }
    }
    return result;
  }

  public static Map<WorkOrder, Outcome> runTestWithAllMutantsIntersectGiven(RemoteTestRunner runner, TestMethod test, Collection<Integer> givenMutants) {
    /* Runs the given test (a) without any mutant, to gather coverage information,
       and then (b) with each mutant in the given set.
       Returns a map describing the result of every test-run thus performed.
       If givenMutants is null, runs the test with each covered mutant.
     */
    Map<WorkOrder, Outcome> result = new LinkedHashMap<WorkOrder, Outcome>();

    // Run the test without mutants.
    WorkOrder workOrder = new WorkOrder(test, 0, TIMEOUT_FOR_UNMUTATED_TESTS);
    Outcome outcome = runOrExit(runner, workOrder);
    result.put(workOrder, outcome);
    Long timeout = timeoutFromOriginalRunTime(outcome.runTime);

    // Figure out which mutants we need to run.
    Vector<Integer> mutantsToRun = new Vector<Integer>(outcome.coveredMutants);
    int originalNm = mutantsToRun.size();
    if (givenMutants != null) mutantsToRun.retainAll(givenMutants);
    Collections.sort(mutantsToRun);

    { // Just logging stuff.
      int nm=mutantsToRun.size();
      long t=timeout+(runner.workerTimeoutGracePeriod==null ? 0 : runner.workerTimeoutGracePeriod);
      System.err.println("[should take at most: "+nm+" mutants (originally "+originalNm+") * "+t+"ms/mutant = "+(nm*t/1000.0)+"s]");
    }

    // Run all those mutants.
    Long t0 = System.currentTimeMillis(); // (just logging stuff)
    for (Integer mutantId : mutantsToRun) {
      workOrder = new WorkOrder(test, mutantId, timeout);
      outcome = runOrExit(runner, workOrder);
      result.put(workOrder, outcome);
    }

    { // Just logging stuff.
      Long t1 = System.currentTimeMillis();
      int nPass=0, nFail=0, nTimeout=0, nCrash=0;
      for (Outcome o : result.values()) {
        if (o.type == Outcome.Type.PASS) nPass += 1;
        else if (o.type == Outcome.Type.FAIL) nFail += 1;
        else if (o.type == Outcome.Type.TIMEOUT) nTimeout += 1;
        else if (o.type == Outcome.Type.CRASH) nCrash += 1;
        else System.err.println("[unrecognized outcome type: "+o.type.toString()+"]");
      }
      System.err.println("[actually took "+((t1-t0)/1000.0)+"s; "+
                         nPass+"/"+nFail+"/"+nTimeout+"/"+nCrash+" pass/fail/timeout/crash]");
    }

    return result;
  }

  private static boolean ignoreMutantsUncoveredByFailingTests = true;
  private static Collection<TestMethod> triggeringTests;
  private static Collection<TestMethod> nontriggeringTests;
  private static TestRunCache cache;
  private static TestMethod onlyTestToRun = null;
  private static Collection<Integer> mutantsToRun = null;

  private static final String USAGE = "java -cp ...:killmap/bin:$DEFECTS4J_HOME/major/config/config.jar:$DEFECTS4J_HOME/major/lib/junit-4.11.jar killmap.Main [--help] [--run-mutants-unkilled-by-failing-tests] [--only-test-to-run CLASS#METHOD] [--mutants-to-run MUT,MUT,...] triggering-tests.txt relevant-test-classes.txt partial-run.csv";

  private static void parseArgs(String[] argArray) {
    Vector<String> argv = new Vector(Arrays.asList(argArray));

    while (argv.get(0).startsWith("--")) {
      switch (argv.get(0)) {
        case "--help":
          System.err.println("usage: "+USAGE);
          System.exit(0);
        case "--run-mutants-unkilled-by-failing-tests":
          ignoreMutantsUncoveredByFailingTests = false;
          argv.remove(0);
          break;
        case "--only-test-to-run":
          try {
            onlyTestToRun = TestFinder.parseTestFullName(argv.get(1), "#");
          }
          catch (ClassNotFoundException|NoSuchMethodException e) {
            System.err.println("no such test: "+argv.get(1));
            System.exit(1);
          }
          argv.remove(0); argv.remove(0);
          break;
        case "--mutants-to-run":
          mutantsToRun = new HashSet(Arrays.asList(argv.get(1).split(",")));
          argv.remove(0); argv.remove(0);
          break;
        case "--":
          break;
        default:
          System.err.println("usage: "+USAGE);
          System.exit(1);
      }
    }

    if (argv.size() != 3) {
      System.err.println("usage: "+USAGE);
      System.exit(1);
    }

    triggeringTests = getTriggeringTestsOrExit(argv.get(0));
    nontriggeringTests = getTestsOrExit(argv.get(1));
    nontriggeringTests.removeAll(triggeringTests);
    try {
      cache = new TestRunCache(argv.get(2));
    } catch (java.io.FileNotFoundException e) {
      System.err.println("unable to open "+argv.get(2));
      e.printStackTrace();
      System.exit(1);
    }

    System.err.println("Only test to run: "+onlyTestToRun);
    System.err.println("Only considering mutants: "+mutantsToRun);
  }

  public static void main(String... args) {
    /* Run as
         java -cp ... killmap.Main triggering-tests.txt relevant-test-classes.txt partial-run.csv
       prints out CSV-formatted data of the form
         test,mutant,timeout,outcomeType,runTime,mutantsCovered,stackTrace
       (see WorkOrder and Outcome classes for descriptions of those fields).

       This is a sparse representation of the (test,mutant)->outcome matrix
         from which all our further data analysis is done.

       For every test-method in every test-class named in "relevant-test-classes.txt",
         there will be a row in the output corresponding to running that test with
         no mutants enabled; and a row for every mutant that both (a) is covered by that test,
         and (b) changes the behavior of one of the tests named in "triggering-tests.txt".

       (That is a *lower bound* on the printed rows. There may be more.)

       "partial-run.csv" is the path to a file that contains the output of a previous run of this program (perhaps it didn't finish). If there was no previous run, `/dev/null` will do; if the previous run's output was compressed, you can use process substitution, e.g. `<(zcat partial-run.gz)`.
     */

    System.err.println("Args: " + Arrays.toString(args));
    parseArgs(args);

    RemoteTestRunner runner = createRunnerOrExit();

    // (just for logging)
    Integer nTests = triggeringTests.size() + nontriggeringTests.size();
    Integer nTestsRun = 0;

    // Run the triggering tests and determine which mutants change their behavior.
    Collection<Integer> mutantsCoveredByTriggeringTests = new HashSet<Integer>();
    Collection<Integer> mutantsChangingBehaviorOfTriggeringTests = new HashSet<Integer>();
    for (TestMethod test : triggeringTests) {
      if (onlyTestToRun != null && !test.equals(onlyTestToRun)) continue;
      // For each triggering test, run it with all the mutants; figure out which mutants change its behavior, and add them to the set.
      System.err.println("[starting test "+(++nTestsRun)+"/"+nTests+": "+test+"]");
      Map<WorkOrder, Outcome> outcomes = runTestWithAllMutantsIntersectGiven(runner, test, null);
      Outcome originalOutcome = outcomes.get(new WorkOrder(test, 0, TIMEOUT_FOR_UNMUTATED_TESTS));
      System.err.println("[covered "+originalOutcome.coveredMutants.size()+" mutants]");
      mutantsCoveredByTriggeringTests.addAll(originalOutcome.coveredMutants);
      mutantsChangingBehaviorOfTriggeringTests.addAll(behaviorChangingMutants(originalOutcome, outcomes));
    }

    System.err.println("["+mutantsChangingBehaviorOfTriggeringTests.size()+" mutants change behavior of triggering tests]");

    Collection<Integer> interestingMutants =
      (mutantsToRun != null) ? mutantsToRun :
      ignoreMutantsUncoveredByFailingTests ? mutantsChangingBehaviorOfTriggeringTests :
      mutantsCoveredByTriggeringTests;

    System.err.println("["+interestingMutants.size()+" mutants are interesting to run passing tests on]");

    // Run the non-triggering tests on all necessary mutants.
    for (TestMethod test : nontriggeringTests) {
      if (onlyTestToRun != null && !onlyTestToRun.equals(test)) continue;
      System.err.println("[starting test "+(++nTestsRun)+"/"+nTests+": "+test+"]");
      runTestWithAllMutantsIntersectGiven(runner, test, interestingMutants);
    }

    closeRunnerOrExit(runner);

    System.err.println("Completed successfully!");
    System.exit(0);
  }
}
