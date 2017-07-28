/* Represents a job that needs to be run, e.g.
 * "run MyTestClass#testSomething, with mutant 4 enabled, with a 300ms timeout."
 */

package killmap.runners.communication;

import killmap.TestFinder;
import killmap.TestMethod;

public class WorkOrder {

  public TestMethod test;
  public Integer mutantId;
  public Long timeout;

  public WorkOrder(TestMethod t, Integer m, Long time) {
    test = t;
    mutantId = m;
    timeout = time;
  }

  @Override
  public boolean equals(Object otherObj) {
    WorkOrder other = (WorkOrder)otherObj;
    return test.equals(other.test) && mutantId.equals(other.mutantId) && timeout.equals(other.timeout);
  }

  @Override
  public int hashCode() {
    return test.hashCode() + mutantId.hashCode() + timeout.hashCode();
  }

  @Override
  public String toString() {
    // Converts the WorkOrder into a string suitable for a line in a CSV file:
    //   test,mutant,timeout
    // where test is the full name of the test,
    //         as given by TestFinder.getTestFullName with separator "#"
    //       mutant is the ID of the mutant that should be run
    //       timeout is the how long the test should be allowed to run before aborting
    return (
      TestFinder.getTestFullName(test, "#")+","+
      mutantId+","+
      timeout);
  }

  public static WorkOrder fromString(String s) throws ClassNotFoundException, IllegalArgumentException, NoSuchMethodException {
    // Inverse of toString(), i.e.
    //   forall w:WorkOrder,
    //   WorkOrder.fromString(w.toString()).equals(w)
    String[] method_mutantId_timeout = s.split(",", 3);
    if (!(method_mutantId_timeout.length == 3))
      throw new IllegalArgumentException(s);

    TestMethod method = TestFinder.parseTestFullName(method_mutantId_timeout[0], "#");
    Integer mutantId = Integer.parseInt(method_mutantId_timeout[1]);
    Long timeout = Long.parseLong(method_mutantId_timeout[2]);

    return new WorkOrder(method, mutantId, timeout);
  }
}