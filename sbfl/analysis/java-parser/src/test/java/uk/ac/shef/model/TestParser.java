
package uk.ac.shef.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Map;
import java.util.Set;

import uk.ac.shef.Parser;
import uk.ac.shef.Resources;

public class TestParser {

  @Test
  public void testClassDefinition() throws Exception {
    String javaFile = Resources.getFile("examples/ClassDefinition.java").getAbsolutePath();

    Parser parser = new Parser(javaFile);
    parser.parse();

    Map<Integer, Set<Integer>> statements = parser.getStatements();
    assertEquals(statements.size(), 1);

    Set<Integer> statement_on_line_1 = statements.get(1);
    assertEquals(statement_on_line_1.size(), 2);
    assertTrue(statement_on_line_1.contains(1));
    assertTrue(statement_on_line_1.contains(2));
  }

  @Test
  public void testFields() throws Exception {
    String javaFile = Resources.getFile("examples/Fields.java").getAbsolutePath();

    Parser parser = new Parser(javaFile);
    parser.parse();

    Map<Integer, Set<Integer>> statements = parser.getStatements();
    assertEquals(statements.size(), 3);

    Set<Integer> statement_on_line_2 = statements.get(2);
    assertEquals(statement_on_line_2.size(), 4);
    assertTrue(statement_on_line_2.contains(2));
    assertTrue(statement_on_line_2.contains(3));
    assertTrue(statement_on_line_2.contains(4));
    assertTrue(statement_on_line_2.contains(5));

    Set<Integer> statement_on_line_8 = statements.get(8);
    assertEquals(statement_on_line_8.size(), 1);
    assertTrue(statement_on_line_8.contains(8));

    Set<Integer> statement_on_line_10 = statements.get(10);
    assertEquals(statement_on_line_10.size(), 2);
    assertTrue(statement_on_line_10.contains(10));
    assertTrue(statement_on_line_10.contains(11));
  }

  @Test
  public void testMethodArguments() throws Exception {
    String javaFile = Resources.getFile("examples/MethodArguments.java").getAbsolutePath();

    Parser parser = new Parser(javaFile);
    parser.parse();

    Map<Integer, Set<Integer>> statements = parser.getStatements();
    assertEquals(statements.size(), 1);

    Set<Integer> statement_on_line_2 = statements.get(2);
    assertEquals(statement_on_line_2.size(), 2);
    assertTrue(statement_on_line_2.contains(2));
    assertTrue(statement_on_line_2.contains(3));
  }

  @Test
  public void testIfCondition() throws Exception {
    String javaFile = Resources.getFile("examples/IfCondition.java").getAbsolutePath();

    Parser parser = new Parser(javaFile);
    parser.parse();

    Map<Integer, Set<Integer>> statements = parser.getStatements();
    assertEquals(statements.size(), 4);

    Set<Integer> statement_on_line_3 = statements.get(3);
    assertEquals(statement_on_line_3.size(), 2);
    assertTrue(statement_on_line_3.contains(3));
    assertTrue(statement_on_line_3.contains(4));
  }

  @Test
  public void testForLoop() throws Exception {
    String javaFile = Resources.getFile("examples/ForLoop.java").getAbsolutePath();

    Parser parser = new Parser(javaFile);
    parser.parse();

    Map<Integer, Set<Integer>> statements = parser.getStatements();
    assertEquals(statements.size(), 2);

    Set<Integer> statement_on_line_3 = statements.get(3);
    assertEquals(statement_on_line_3.size(), 3);
    assertTrue(statement_on_line_3.contains(3));
    assertTrue(statement_on_line_3.contains(4));
    assertTrue(statement_on_line_3.contains(5));
  }

  @Test
  public void testWhileLoop() throws Exception {
    String javaFile = Resources.getFile("examples/WhileLoop.java").getAbsolutePath();

    Parser parser = new Parser(javaFile);
    parser.parse();

    Map<Integer, Set<Integer>> statements = parser.getStatements();
    assertEquals(statements.size(), 2);

    Set<Integer> statement_on_line_3 = statements.get(3);
    assertEquals(statement_on_line_3.size(), 2);
    assertTrue(statement_on_line_3.contains(3));
    assertTrue(statement_on_line_3.contains(4));
  }

  @Test
  public void testComments() throws Exception {
    String javaFile = Resources.getFile("examples/Comments.java").getAbsolutePath();

    Parser parser = new Parser(javaFile);
    parser.parse();

    Map<Integer, Set<Integer>> statements = parser.getStatements();
    assertEquals(statements.size(), 0);
  }

  @Test
  public void testEnumDeclaration() throws Exception {
    String javaFile = Resources.getFile("examples/EnumDeclaration.java").getAbsolutePath();

    Parser parser = new Parser(javaFile);
    parser.parse();

    Map<Integer, Set<Integer>> statements = parser.getStatements();
    assertEquals(statements.size(), 4);

    assertEquals(statements.get(12).size(), 2);
    assertTrue(statements.get(12).contains(12));
    assertTrue(statements.get(12).contains(13));

    assertEquals(statements.get(14).size(), 2);
    assertTrue(statements.get(14).contains(14));
    assertTrue(statements.get(14).contains(15));

    assertEquals(statements.get(16).size(), 1);
    assertTrue(statements.get(16).contains(16));

    assertEquals(statements.get(18).size(), 1);
    assertTrue(statements.get(18).contains(18));
  }

  @Test
  public void testAnnotations() throws Exception {
    String javaFile = Resources.getFile("examples/Annotation.java").getAbsolutePath();

    Parser parser = new Parser(javaFile);
    parser.parse();

    Map<Integer, Set<Integer>> statements = parser.getStatements();
    assertEquals(statements.size(), 2);

    assertEquals(statements.get(3).size(), 2);
    assertTrue(statements.get(3).contains(3));
    assertTrue(statements.get(3).contains(4));

    assertEquals(statements.get(5).size(), 1);
    assertTrue(statements.get(5).contains(5));
  }
}
