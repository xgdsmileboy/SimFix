/* Runs tests in a subprocess, to insulate this process from any
 * possible damage inflicted by the test (e.g. OutOfMemoryErrors).
 *
 * A RemoteTestRunner has a single "worker" subprocess, which is
 * running TestRunner.main(). They communicate over a socket, the
 * RemoteTestRunner issuing WorkOrders and receiving Outcomes.
 * If the worker fails to respond within a reasonable amount of time
 * (slightly larger than the timeout associated with the test, to
 * allow for small random overheads), the worker is killed and a new
 * one is spawned, under the assumption that something terrible has
 * happened to the old one.
 */

package killmap.runners;

import java.lang.reflect.Field;

// For communicating with the TestRunner subprocess
import java.net.Socket;
import java.net.ServerSocket;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

// For waiting for the TestRunner subprocess to respond
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

// For obtaining the current classpath (to give the TestRunner subprocess)
import java.net.URL;
import java.net.URLClassLoader;

import killmap.runners.communication.Outcome;
import killmap.runners.communication.WorkOrder;

public class RemoteTestRunner {

  public static class SocketNotAvailableException extends IOException {
    private static final long serialVersionUID = 6806975492758375493L;

    public SocketNotAvailableException(String message) {super(message);}
    public SocketNotAvailableException(Throwable cause) {super(cause);}
  }

  public static class WorkerCreationError extends IOException {
    private static final long serialVersionUID = -4829153585761104267L;

    public WorkerCreationError(String message) {super(message);}
    public WorkerCreationError(Throwable cause) {super(cause);}
  }

  public static class WorkerCommunicationError extends IOException {
    private static final long serialVersionUID = -4959683287508208392L;

    public WorkerCommunicationError(String message) {super(message);}
    public WorkerCommunicationError(Throwable cause) {super(cause);}
  }

  private ServerSocket server; // socket the worker should connect to
  private Process worker;
  private PrintStream workOrderStream; // to send WorkOrders to the worker
  private BufferedReader outcomeStream; // to read the worker's responses
  public Long workerTimeoutGracePeriod; // time the worker has to respond, on top of the test timeout, before we kill it

  public RemoteTestRunner() throws SocketNotAvailableException {
    try {
      server = new ServerSocket(0/*any available port*/);
    } catch (IOException e) {
      throw new SocketNotAvailableException(e);
    }
    worker = null;
    workOrderStream = null;
    outcomeStream = null;
    workerTimeoutGracePeriod = null;
  }

  private Integer waitForExit(final Process p, Long timeout) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<Integer> futureExitValue = executor.submit(new Callable<Integer>() {
      public Integer call() throws Exception {
        p.waitFor();
        return p.exitValue();
      }
    });
    try {
      return futureExitValue.get(timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException|InterruptedException|ExecutionException e) {
      futureExitValue.cancel(true);
      return null;
    }
  }

  private Integer getPid(Process p) {
    // Reflection black magic.
    try {
      Field pidField = p.getClass().getDeclaredField("pid");
      pidField.setAccessible(true);
      return (Integer)pidField.get(p);
    } catch (NoSuchFieldException|IllegalAccessException e) {
      System.err.println("unable to get PID of "+p);
      e.printStackTrace();
      return null;
    }
  }

  private void kill9(Process p) {
    Integer pid = getPid(p);

    Process killer;
    try {
      killer = (new ProcessBuilder()).redirectError(ProcessBuilder.Redirect.INHERIT).command(new String[]{"kill", "-9", pid.toString()}).start();
    } catch (IOException e) {
      System.err.println("Arrrgh: unable start 'kill -9 "+pid+"'");
      e.printStackTrace();
      return;
    }

    Integer killerExitValue = waitForExit(killer, (long)500);
    if (killerExitValue == null) {
      System.err.println("Warning: 'kill -9 "+pid+"' did not exit in 0.5s");
    } else if (killerExitValue != 0) {
      System.err.println("Warning: 'kill -9 "+pid+"' exited with status "+killerExitValue);
    }

  }

  private void superDuperKillProcess(Process p) {
    p.destroy();
    if (waitForExit(p, (long)100) == null) return;
    kill9(p);
    try {p.waitFor();} catch (InterruptedException e) {}
  }

  private void killWorker() {
    Process oldWorker = worker;
    worker = null;
    workOrderStream = null;
    outcomeStream = null;
    workerTimeoutGracePeriod = null;
    if (oldWorker != null) {
      System.err.println("killing worker");
      superDuperKillProcess(oldWorker);
    }
  }

  private void createFreshWorker() throws WorkerCreationError, WorkerCommunicationError {
    killWorker();

    // Fork the worker
    String classpath = System.getenv("KILLMAP_CLASSPATH");
    try {
      String[] command = {"java", "-Djava.awt.headless=true", "-XX:ReservedCodeCacheSize=512M", "-XX:MaxPermSize=1G", "-XX:-OmitStackTraceInFastThrow", "-cp", classpath, "killmap.runners.TestRunner", Integer.toString(server.getLocalPort())};
      worker = new ProcessBuilder().redirectError(ProcessBuilder.Redirect.INHERIT).command(command).start();
    } catch (IOException e) {
      throw new WorkerCreationError(e);
    }

    // Let the worker connect
    Socket socket = null;
    try {
      socket = server.accept();
      workOrderStream = new PrintStream(socket.getOutputStream());
      outcomeStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      throw new WorkerCreationError(e);
    }

    // Do the startup handshake and figure out the grace period.
    Long pingTime = null;
    try {
      pingTime = TestRunner.initializeSocketToTestRunner(socket);
    } catch (IOException e) {
      throw new WorkerCommunicationError(e);
    }
    workerTimeoutGracePeriod = gracePeriodFromPingTime(pingTime);

    System.err.println("Created worker with pid "+getPid(worker));
  }

  private void ensureWorkerExists() throws WorkerCreationError, WorkerCommunicationError {
    if (worker == null) {
      createFreshWorker();
    }
  }

  public Outcome runTest(WorkOrder workOrder) throws WorkerCreationError, WorkerCommunicationError {
    // Runs the specified test in a worker JVM, gets the outcome from the worker,
    // and returns it.
    // If the worker doesn't respect the timeout named in the WorkOrder, kills the worker.

    // For some reason the GC doesn't clean up after us very well,
    // so let's trigger it manually if there seem to be "too many" loose threads.
    // (Yeah, this is a bit of a hack.)
    if (Thread.activeCount() > 50) System.gc();

    // Send the command to the worker.
    ensureWorkerExists();
    workOrderStream.println(workOrder.toString());

    // Read the response from the worker.
    final BufferedReader thisWorkerOutcomeStream = outcomeStream;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<String> futureResponse = executor.submit(new Callable<String>() {
      public String call() throws IOException {
        return thisWorkerOutcomeStream.readLine();
      }
    });
    String response = null;
    try {
      response = futureResponse.get(workOrder.timeout.intValue() + workerTimeoutGracePeriod, TimeUnit.MILLISECONDS);
    } catch (TimeoutException|InterruptedException|ExecutionException e) {
      System.err.println("worker timed out without responding");
      futureResponse.cancel(true);
      killWorker();
      return Outcome.createCrash();
    }

    if (response == null) {
      System.err.println("worker exited without responding");
      killWorker();
      return Outcome.createCrash();
    }

    Outcome result = null;
    try {
      result = Outcome.fromString(response);
    } catch (IllegalArgumentException e) {
      System.err.println("worker printed nonsense to socket");
      e.printStackTrace();
      killWorker();
      return Outcome.createCrash();
    }

    // If the worker experienced an OutOfMemoryError, resource exhaustion might be an issue,
    // even if it's healthy enough to tell us the outcome.
    // Let's not take any chances about corrupting future test results: kill the worker.
    // (This is a rare occurrence, so this isn't computationally expensive.)
    if (result.stackTrace.startsWith("java.lang.RuntimeException: java.lang.OutOfMemoryError")) {
      killWorker();
    }
    return result;
  }

  private static long gracePeriodFromPingTime(long pingTime) {
    if (pingTime < 25) return 100;
    return 8*pingTime;
  }

  public void close() throws IOException {
    killWorker();
  }
}
