
package uk.ac.shef;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

public class JUnitRunner {

  /**
   * args[0] test class
   * args[1] test method (optional)
   * 
   * @param args
   */
  public static void main(String[] args) {

    if (args.length < 1 || args.length > 2) {
      System.err.println("Usage: java -cp .:JUnitRunner-0.0.1-SNAPSHOT.jar:<project cp> uk.ac.shef.JUnitRunner <full test class name> [test method name]");
      System.exit(-1);
    }

    Class<?> clazz = null;
    try {
      clazz = Class.forName(args[0], false, JUnitRunner.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.exit(-1);
    }

    Request request = null;
    if (args.length == 1) {
      request = Request.aClass(clazz);
    } else if (args.length == 2) {
      request = Request.method(clazz, args[1]);
    }

    JUnitListener listener = new JUnitListener();

    JUnitCore runner = new JUnitCore();
    runner.addListener(listener);
    runner.run(request); // run test method

    System.exit(0);
  }
}
