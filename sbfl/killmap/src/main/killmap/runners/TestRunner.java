/* Runs JUnit tests.
 *
 * main() will connect to a given socket,
 * participate in a startup handshake with the other side,
 * and then loop until the other side closes the socket:
 *   read a WorkOrder from the socket
 *   execute that test with that mutant and timeout
 *   write the Outcome to the socket
 *   repeat forever
 *
 * Sometimes, a test will crash the JVM. It's the other side's responsibility
 * to notice when that happens, kill this process, and fork a new one.
 */

package killmap.runners;

import major.mutation.Config;

import java.lang.reflect.InvocationTargetException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Request;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import java.net.Socket;

import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import killmap.runners.communication.Outcome;
import killmap.runners.communication.WorkOrder;
import killmap.runners.isolation.DeadEndDigestOutputStream;
import killmap.runners.isolation.IsolatingClassLoader;

public class TestRunner {

  public static Outcome simpleBlockingRunTest(WorkOrder workOrder) {
    // Just run the test and record the covered mutants. No fancy stuff.
    Config.reset();
    Config.__M_NO = workOrder.mutantId;
    Result result = (new JUnitCore()).run(Request.method(workOrder.test.getTestClass(), workOrder.test.getName()));
    Outcome outcome = new Outcome(result, "");
    outcome.coveredMutants = Config.getCoverageList();
    Config.reset();
    return outcome;
  }

  private static Outcome simpleBlockingRunTestInOtherClassLoader(ClassLoader classLoader, WorkOrder workOrder) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    // Call simpleBlockingRunTest in the other class loader, doing all the necessary translation
    // between objects of classes loaded by our loader, and objects of the corresponding
    // classes loaded by the other loader.

    // The translation is done by converting to a string and back again.

    Class<?> theirWorkOrderClass = classLoader.loadClass(WorkOrder.class.getCanonicalName());
    Class<?> theirTestRunnerClass = classLoader.loadClass(TestRunner.class.getCanonicalName());

    Object theirWorkOrder =
      theirWorkOrderClass.getDeclaredMethod("fromString", String.class)
                         .invoke(null, workOrder.toString());
    Object theirOutcome =
      theirTestRunnerClass.getDeclaredMethod("simpleBlockingRunTest", theirWorkOrderClass)
                          .invoke(null, theirWorkOrder);

    return Outcome.fromString(theirOutcome.toString());
  }

  public static Outcome isolatedBlockingRunTest(WorkOrder workOrder) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

    // Create a fresh, isolated classloader with the same classpath as the current one.
    ClassLoader theirClassLoader = new IsolatingClassLoader();
    // Make the isolated classloader our thread's new classloader.
    // This method is called in a dedicated thread that ends right after
    // this method returns, so we don't need to worry about restoring
    // the old classloader when we're done.
    Thread.currentThread().setContextClassLoader(theirClassLoader);

    Long t0 = System.currentTimeMillis();
    Outcome outcome = simpleBlockingRunTestInOtherClassLoader(theirClassLoader, workOrder);
    Long t1 = System.currentTimeMillis();
    outcome.runTime = t1-t0;
    return outcome;
  }

  private static void killThreadGroup(ThreadGroup group) {
    Thread[] activeThreads = new Thread[group.activeCount()];
    for (int i=0; i<group.enumerate(activeThreads); ++i) {
      activeThreads[i].interrupt();
    }
  }

  private static Outcome runTestWithoutCatchingStdStreams(final WorkOrder workOrder) {

    FutureTask<Outcome> futureOutcome = new FutureTask<Outcome>(new Callable<Outcome>() {
      public Outcome call() throws Exception {
        try {return TestRunner.isolatedBlockingRunTest(workOrder);}
        catch (Exception e) {e.printStackTrace(); throw e;}
      }
    });

    ThreadGroup group = new ThreadGroup("[test thread group for "+workOrder+"]");
    Thread thread = new Thread(group, futureOutcome, "[test thread for "+workOrder+"]");
    thread.start();

    try {
      return futureOutcome.get(workOrder.timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException|ExecutionException|InterruptedException e) {
      killThreadGroup(group);
      if (group.activeCount() == 0) return Outcome.createTimeout(workOrder);
      try {Thread.sleep(20);} catch (InterruptedException e2) {System.exit(1);}
      if (group.activeCount() == 0) return Outcome.createTimeout(workOrder);

      System.exit(1);
      return null;
    }
  }

  public static Outcome runTest(WorkOrder workOrder) {
    /* Run the given test, with the given mutant, for the given time.
       If the test tries to print anything to stdout/stderr, intercept that output
         and just remember the hash. (Mutants can print out zillions of lines.)
       The test is run in a fresh classloader so it's not easy to change static state
         for future tests.
     */
    PrintStream originalStdout = System.out;
    PrintStream originalStderr = System.err;
    DeadEndDigestOutputStream fakeStdout = new DeadEndDigestOutputStream();
    DeadEndDigestOutputStream fakeStderr = new DeadEndDigestOutputStream();
    System.setOut(new PrintStream(fakeStdout));
    System.setErr(new PrintStream(fakeStderr));
    try {
      Outcome outcome = runTestWithoutCatchingStdStreams(workOrder);
      outcome.digest = fakeStdout.getDigestString() + fakeStderr.getDigestString();
      return outcome;
    } finally {
      System.setOut(originalStdout);
      System.setErr(originalStderr);
    }
  }

  public static void main(String[] args) throws Exception {
    /*
       Run as
         java -cp ... killmap.runners.TestRunner 12345
       connects to port 12345 and, until the other end hangs up,
         - reads a WorkOrder from the socket
         - runs the test, thoroughly isolated
         - writes the Outcome to the socket
       It requires a short startup handshake with the other end, described in
         the initializeSocketToTestRunner and initializeSocketAsTestRunner methods:

         TestRunner          Other end
           "awake"   ----->
                     <-----    "ping"
            "pong"   ----->
                     <----- [work order]
          [outcome]  ----->
     */
    int port = Integer.parseInt(args[0]);
    Socket socket = new Socket("localhost", port);
    initializeSocketAsTestRunner(socket);

    BufferedReader instructionStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    PrintStream outcomeStream = new PrintStream(socket.getOutputStream());

    while (true) {
      String workOrderString = instructionStream.readLine();
      if (workOrderString == null)
        System.exit(0);

      WorkOrder workOrder = WorkOrder.fromString(workOrderString);

      Outcome outcome = runTest(workOrder);
      if (outcome.type == Outcome.Type.TIMEOUT) outcome.runTime = workOrder.timeout;

      outcomeStream.println(outcome.toString()); outcomeStream.flush();
    }
  }

  public static Long initializeSocketToTestRunner(Socket socket) throws IOException {
    // Takes care of the "other side" of the startup handshake described in "main",
    // and returns the number of milliseconds a round-trip message takes.
    PrintStream toRunner = new PrintStream(socket.getOutputStream());
    BufferedReader fromRunner = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    String response = null;
    response = fromRunner.readLine();
    if (!response.equals("awake"))
      throw new IOException("expected TestRunner to start conversation with 'awake', got '"+response+"'");

    Long t0 = System.currentTimeMillis();
    toRunner.println("ping"); toRunner.flush();
    response = fromRunner.readLine();
    if (!response.equals("pong"))
      throw new IOException("expected TestRunner to respond to ping with 'pong', got '"+response+"'");
    Long t1 = System.currentTimeMillis();

    return t1-t0;
  }
  private static void initializeSocketAsTestRunner(Socket socket) throws IOException {
    // Takes care of the TestRunner's side of the startup handshake described in "main".
    PrintStream toHost = new PrintStream(socket.getOutputStream());
    BufferedReader fromHost = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    toHost.println("awake");

    String ping = fromHost.readLine();
    if (!ping.equals("ping")) throw new java.io.IOException("expected to hear 'ping', got '"+ping+"'");
    toHost.println("pong"); toHost.flush();
  }
}
