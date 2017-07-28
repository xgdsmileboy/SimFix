java-parser
===========

`java-parser` is an utility program that parses .java files to extract
the set of lines of code that are part of the same statement.

Considering the following snippet of code from Chart-1b, class
`org.jfree.chart.renderer.category.LineAndShapeRenderer`:

```
772.  if (this.useSeriesOffset) {
773.    x0 = domainAxis.getCategorySeriesMiddle(
774.          column - 1, dataset.getColumnCount(),
775.          visibleRow, visibleRowCount,
776.          this.itemMargin, dataArea,
777.          plot.getDomainAxisEdge());
```

It can be seen there are 6 lines of code, but only 2 statements (as
lines 773-777 are part of the same statement). So, `java-parser`
reads a list of .java files and prints to a file, information of each
java statement. Each row of the file contains two columns: First
column represents a statement, and the second column represents a line
of code that is part of the statement. For example:

```
...
org/jfree/chart/renderer/category/LineAndShapeRenderer.java#773:org/jfree/chart/renderer/category/LineAndShapeRenderer.java#774
org/jfree/chart/renderer/category/LineAndShapeRenderer.java#773:org/jfree/chart/renderer/category/LineAndShapeRenderer.java#775
org/jfree/chart/renderer/category/LineAndShapeRenderer.java#773:org/jfree/chart/renderer/category/LineAndShapeRenderer.java#776
org/jfree/chart/renderer/category/LineAndShapeRenderer.java#773:org/jfree/chart/renderer/category/LineAndShapeRenderer.java#777
...
```

I.e., lines #774, #775, #776, #777 are part of statement #773.


### How to compile it?

```
  mvn clean package
```

### How to use it?


```
  java -jar java-parser-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
    <dir.src.classes> \
    <list of loaded classes> \
    <output file name>
```

Where `<dir.src.classes>` is the source directory of classes (relative
to working directory), `<list of loaded classes>` set of loaded
classes (i.e., the output of `cat $D4J_HOME/framework/projects/<project_name>/loaded_classes/<bug id>.src | tr '\n' ':'`),
and `<output file name>` the file to which the output of `java-parser`
will be written.


run_java-parser.sh
------------------

`run_java-parser.sh` and `_run_java-parser.sh` are two scripts
to automatically run `java-parser` on all Defect4J's projects and bugs.

Basically, `run_java-parser.sh` compiles `java-parser` and runs it on
each Defect4J's bugs, by calling `_run_java-parser.sh` with the
following parameters:

* `<project name>`
* `<bug id>`
* `<output dir>`

Note: `_run_java-parser.sh` is called by `run_java-parser.sh` using
Sun's Grid cluster interface.
