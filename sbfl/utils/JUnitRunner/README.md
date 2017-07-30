JUnitRunner
===========

This directory provides Java programs that
 run test classes/cases and
determine find which test cases are in a test class.

1. JUnitRunner
2. JUnitFinder


### How to compile it?

First, compile the files:

```
  mvn clean package
```


JUnitRunner
-----------

JUnitRunner is able to execute all test cases of a test class, or
just a single test case. To run a test class:

```
  java -cp <project cp>:JUnitRunner-0.0.1-SNAPSHOT.jar uk.ac.shef.JUnitRunner \
    <test class name>
```

To run a single test case:

```
  java -cp <project cp>:JUnitRunner-0.0.1-SNAPSHOT.jar uk.ac.shef.JUnitRunner \
    <test class name> \
    <test case name>
```

where `<project cp>` is the classpath of the project that contains the
`<test class name>`.

`JUnitRunner` does not return anything. By using a custom listener,
`JUnitListener`, `JUnitRunner` only prints to the stdout the name of
the test cases executed and the trace of failing test cases (if any).
The failure message of a failing test case follows the following
format:

```
--- <test class name>::<test method name>
<trace>
```


JUnitFinder
-----------

Given a list of JUnit test classes (separated by ':'), JUnitFinder
prints to stdout the name of all test cases in the test classes.
JUnitFinder should be executed like:

```
  java -cp <project cp>:JUnitRunner-0.0.1-SNAPSHOT.jar uk.ac.shef.JUnitFinder \
    <name of all test classes separated by ':'>
```

