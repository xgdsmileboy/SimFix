package uk.ac.shef;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Set;

public class Main {

  public static final String PATH_SEPARATOR = System.getProperty("path.separator");
  public static final String FILE_SEPARATOR = System.getProperty("file.separator");

  public static void main(String[] args) throws Exception {

    if (args.length < 3) {
      System.err.println("Usage \n"
          + "  $ java -jar java-parser-0.0.1-SNAPSHOT-jar-with-dependencies.jar "
          + "<dir.src.classes> "
          + "<list of loaded classes> "
          + "<output file name>");
      System.exit(-1);
    }

    File file = new File(args[2]);
    // if file does not exist, then create it
    if (!file.exists()) {
      file.createNewFile();
    }

    FileWriter fw = new FileWriter(file.getAbsoluteFile());
    BufferedWriter bw = new BufferedWriter(fw);

    for (String clazz : args[1].split(PATH_SEPARATOR)) {
      String filePath = args[0] + FILE_SEPARATOR + clazz.replace(".", FILE_SEPARATOR) + ".java";

      // parse a Java file and get a Map<Statement number, List of *all* lines
      // that compose a Statement
      Parser p = new Parser(filePath);
      p.parse();

      Map<Integer, Set<Integer>> statements = p.getStatements();      
      for (Integer statement_number : statements.keySet()) {
        Set<Integer> lines = statements.get(statement_number);
        for (Integer line : lines) {
          if (line.equals(statement_number)) {
            // skip it
            continue;
          }

          /*System.out.println(clazz.replace(".", "/") + ".java#" + statement_number
              + ":"
              + clazz.replace(".", "/") + ".java#" + line);*/
          bw.write(clazz.replace(".", "/") + ".java#" + statement_number
              + ":"
              + clazz.replace(".", "/") + ".java#" + line + "\n");
        }
      }
    }

    bw.close();
    System.exit(0);
  }
}
