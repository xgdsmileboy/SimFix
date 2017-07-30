package uk.ac.shef;

import org.junit.runner.Description;
import org.junit.runner.Request;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JUnitFinder {

  /**
   * args[0] test class(es) separated by ":"
   * 
   * @param args
   */
  public static void main(String[] args) {

    if (args.length != 1) {
      System.err.println("Usage: java -cp .:JUnitRunner-0.0.1-SNAPSHOT.jar:<project cp> uk.ac.shef.JUnitFinder <full test class name>");
      System.exit(-1);
    }

    List<String> methods = new ArrayList<String>();

    for (String testClassName : args[0].split(":")) {
      Class<?> clazz = null;
      try {
        clazz = Class.forName(testClassName);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
        continue ;
      }

      for (Description test : Request.aClass(clazz).getRunner().getDescription().getChildren()) {
        // a parameterized atomic test case does not have a method name
        if (test.getMethodName() == null) {
          for (Method m : clazz.getMethods()) {
            // JUnit 3: an atomic test case is "public", does not return anything ("void"), has 0
            // parameters and starts with the word "test"
            // JUnit 4: an atomic test case is annotated with @Test
            if (m.isAnnotationPresent(org.junit.Test.class)
                || Modifier.isPublic(m.getModifiers()) && m.getReturnType().equals(Void.TYPE)
                    && m.getParameterTypes().length == 0 && m.getName().startsWith("test")) {
              methods.add(testClassName + "::" + (m.getName() + test.getDisplayName()));
            }
          }
        } else {
          // non-parameterized atomic test case
          methods.add(testClassName + "::" + test.getMethodName());
        }
      }
    }

    Collections.sort(methods, new Comparator<String>() {
      public int compare(String m1, String m2) {
        return m1.compareTo(m2);
      }
    });

    for (String m : methods) {
      System.out.println(m);
    }
  }
}
