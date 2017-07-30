/* A classloader that should have the same classpath as the normal classloader,
 * but shares absolutely nothing with it. Ensures that no test will change static
 * state that will affect later tests.
 */

package killmap.runners.isolation;

import java.net.URLClassLoader;

public class IsolatingClassLoader extends URLClassLoader {
  public IsolatingClassLoader() {
    super(((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs(), null);
  }
}