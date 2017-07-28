
package uk.ac.shef;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Parser {

  private Map<Integer, Set<Integer>> statements = new LinkedHashMap<Integer, Set<Integer>>();

  private final String sourceName;

  /**
   * Parser constructor
   *
   * @param Statement-suspiciousness vector
   * @param Path to a Java source code
   */
  public Parser(String sourceName) {
    this.sourceName = sourceName;
  }

  public Map<Integer, Set<Integer>> getStatements() {
    return this.statements;
  }

  /**
   * 
   * @throws Exception
   */
  public void parse() throws Exception {

    // creates an input stream for the file to be parsed
    FileInputStream in = new FileInputStream(this.sourceName);

    CompilationUnit cu;
    try {
      // parse the file
      cu = JavaParser.parse(in);
    } finally {
      in.close();
    }

    // explore tree
    explore(" ", cu);

    /*for (Integer statement : this.statements.keySet()) {
      System.out.println(statement + " -> " + this.statements.get(statement).toString());
    }*/
  }

  private void explore(String space, Node node) {
    /*System.out.println(space + node.getBeginLine() + ":" + node.getEndLine() + " type: "
        + node.getClass().getCanonicalName() + " has children? "
        + (node.getChildrenNodes().isEmpty() ? "*false*" : "true"));*/

    // ignore everything related to comments
    if (node.getClass().getCanonicalName()
        .startsWith("com.github.javaparser.ast.comments.")) {
      return ;
    }
    if (node.getClass().getCanonicalName()
        .equals("com.github.javaparser.ast.body.EnumConstantDeclaration")) {
      return ;
    }

    if (node.getChildrenNodes().isEmpty()) {
      Integer line_number = node.getParentNode().getBeginLine();

      // is it a statement?
      if (node.getClass().getCanonicalName()
          .startsWith("com.github.javaparser.ast.stmt.") &&
          node.getBeginLine() == node.getEndLine()) {
        line_number = node.getBeginLine();
      } else if (node.getParentNode().getBeginLine() == node.getParentNode().getEndLine()) {

        Node clone = node;
        Node parent = null;

        // to handle special cases: parameters, binary expressions, etc
        // search for the next 'Declaration' or 'Statement'
        while ((parent = clone.getParentNode()) != null) {
          if ((parent.getClass().getCanonicalName().startsWith("com.github.javaparser.ast.stmt."))
              || (parent.getClass().getCanonicalName()
                  .equals("com.github.javaparser.ast.body.VariableDeclarator"))
              || (parent.getClass().getCanonicalName().startsWith("com.github.javaparser.ast.body.")
                  && parent.getClass().getCanonicalName().endsWith("Declaration"))) {
            line_number = parent.getBeginLine();
            break;
          }

          clone = parent;
        }
      }

      Set<Integer> lines = null;

      if (this.statements.containsKey(line_number)) {
        lines = this.statements.get(line_number);
      } else {
        lines = new LinkedHashSet<Integer>();
        lines.add(line_number);
      }

      lines.add(node.getBeginLine());
      this.statements.put(line_number, lines);
    } else {
      for (Node child : node.getChildrenNodes()) {
        explore(space + " ", child);
      }
    }
  }
}
