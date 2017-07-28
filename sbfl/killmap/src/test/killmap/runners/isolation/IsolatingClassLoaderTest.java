package killmap.runners.isolation;

import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.equalTo;

import killmap.runners.isolation.IsolatingClassLoader;

public class IsolatingClassLoaderTest extends TestCase {

  public static Integer foo = 0;

  @SuppressWarnings({ "rawtypes", "resource" })
  @Test
  public void testDoesNotShareClassesWithParent() throws Exception {
    ClassLoader isolatingLoader1 = new IsolatingClassLoader();
    ClassLoader isolatingLoader2 = new IsolatingClassLoader();

    String className = "killmap.runners.isolation.IsolatingClassLoaderTest";
    Class isolatedClass1 = isolatingLoader1.loadClass(className);
    Class isolatedClass2 = isolatingLoader2.loadClass(className);

    assertThat(isolatedClass1, not(equalTo(isolatedClass2)));

    // Ensure that the isolated classes' static states are independent
    isolatedClass1.getField("foo").set(isolatedClass1, (Integer)1);
    isolatedClass2.getField("foo").set(isolatedClass2, (Integer)2);
    assertEquals((Integer)1, (Integer)isolatedClass1.getField("foo").get(isolatedClass1));
    assertEquals((Integer)2, (Integer)isolatedClass2.getField("foo").get(isolatedClass2));
  }
}
