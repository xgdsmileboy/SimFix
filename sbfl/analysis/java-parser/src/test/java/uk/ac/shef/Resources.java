
package uk.ac.shef;

import java.io.File;

public class Resources {

  /**
   * 
   * 
   * @param name
   * @return 
   */
  public static File getFile(String name) {
    return new File(Resources.class.getClassLoader().getResource(name).getFile());
  }
}
