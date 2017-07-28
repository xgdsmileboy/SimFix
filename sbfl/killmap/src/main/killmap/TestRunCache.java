/* Presents a TestRunner-like interface, but just reads outcomes from a file instead of running them.

   When you create a TestRunCache, you pass in a file. Then, every time you ask it for the outcome you'd get by running <WorkOrder> (using the `tryGet` method), it looks in the file to try to determine the Outcome associated with that WorkOrder.

   To avoid loading the entire (possibly very large) file at once, TestRunCache makes some compromises: the cache-file should only contain a subset of the anticipated test-runs, in the order you anticipate making them. Any reordering, and any additional test-runs in the cache-file, will cause the cache to miss all following queries.

   (That said, a TestRunCache *will* skip over poorly-formatted lines, just in case something malformed sneaks into a test-run file -- perhaps the process writing the file got interrupted, say.)
 */

package killmap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import killmap.runners.communication.Outcome;
import killmap.runners.communication.WorkOrder;

public class TestRunCache {

  BufferedReader reader;
  WorkOrder lastReadWorkOrder;
  Outcome lastReadOutcome;
  boolean reachedEOF;

  public TestRunCache(String path) throws java.io.FileNotFoundException {
    reader = new BufferedReader(new FileReader(path));
    reachedEOF = false;
    advance();
  }

  public Outcome tryGet(WorkOrder workOrder) {
    if (reachedEOF) return null;
    if (workOrder.equals(lastReadWorkOrder)) {
      Outcome result = lastReadOutcome;
      advance();
      return result;
    }
    return null;
  }

  private boolean setLastReadFromLine(String line) {
    /* Parses the given line and sets the last-read outcome and work order.
     * Returns whether `line` was successfully parsed.
     */
    String[] test_mutant_timeout_outcome = line.split(",", 4);
    if (test_mutant_timeout_outcome.length != 4) {
      return false;
    }

    try {
      lastReadWorkOrder = WorkOrder.fromString(
        test_mutant_timeout_outcome[0] + "," +
        test_mutant_timeout_outcome[1] + "," +
        test_mutant_timeout_outcome[2]);
    } catch (ClassNotFoundException|NoSuchMethodException|IllegalArgumentException e) {
      return false;
    }

    try {
      lastReadOutcome = Outcome.fromString(test_mutant_timeout_outcome[3]);
    } catch (IllegalArgumentException e) {
      return false;
    }

    return true;
  }

  private void advance() {
    String line = null;
    boolean lineIsWellFormed = false;
    while (!lineIsWellFormed) {
      try {
        line = reader.readLine();
      } catch (IOException e) {
        reachedEOF = true;
        return;
      }
      if (line == null || readerFinished()) {
        reachedEOF = true;
        return;
      }

      lineIsWellFormed = setLastReadFromLine(line);
    }
  }

  private boolean readerFinished() {
    try {
      reader.mark(1);
      boolean result = (reader.read() == -1);
      reader.reset();
      return result;
    } catch (IOException e) {
      return true;
    }
  }

}
