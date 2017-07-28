
package killmap;

public class TestMethod implements Comparable<TestMethod> {

  private final Class<?> testClass;

  private final String name;

  private static final char SEPARATOR = '#';

  public TestMethod(Class<?> testClass, String name) {
    this.testClass = testClass;
    this.name = name;
  }

  public Class<?> getTestClass() {
    return this.testClass;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return 37 * 19 * this.toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TestMethod) {
      TestMethod other = (TestMethod) obj;
      return this.toString().equals(other.toString());
    }
    return false;
  }

  @Override
  public int compareTo(TestMethod obj) {
    if (obj instanceof TestMethod) {
      TestMethod other = (TestMethod) obj;
      return this.toString().compareTo(other.toString());
    }
    return -1;
  }

  @Override
  public String toString() {
    return this.testClass.getName() + SEPARATOR + this.name;
  }
}
