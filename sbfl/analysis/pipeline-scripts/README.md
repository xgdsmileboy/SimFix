This directory contains scripts to turn Killmap's/GZoltar's output into score-files.

This file describes the "pipeline" that does this, including:

- a bird's-eye view of how it works;
- a description of how to use it;
- file formats for the intermediate steps;
- the command-line interface each step offers.


Bird's-Eye View
===============

You've checked out a Defects4J project, and you've run GZoltar and Killmap on it. They've spit out four important output files:

- `cov-matrix.txt`: produced by GZoltar, indicates which tests cover which statements.
- `cov-spectra.txt`: produced by GZoltar, gives the class and line number in the source code on which each statement begins.
- `killmap.csv`: produced by Killmap, gives very detailed information (runtime, full stack trace) about running each each test with each mutant.
- `mutants.log`: produced by Major, gives the class and line number in the source code where each mutant lives (and other information that is not used by the pipeline).

These four files contain all the information we need to run and evaluate any FLT. We do that as follows:

- For SBFL techniques:

    * "Crush" the coverage matrix to produce a "statement-suspiciousness vector".  In the coverage matrix, rows correspond to tests and columns to statements.  Crushing converts each column into a single number; each fault localization technique defines how this conversion works.  A statement-suspiciousness vector is a mapping from statements to suspiciousnesses (real numbers, usually but not always between 0 and 1).
    * Expand the statement-suspiciousness vector to a "line-suspiciousness vector". (This comprises determining which source code line(s) each statement spans, and assigning the statement's suspiciousness to all those lines.)
    * Sort the lines by suspiciousness, to obtain a ranking of source code lines.
    * Find the first-ranked and last-ranked faulty lines, and calculate scores based on those. There's a lot of detail hidden in this step (see [Scoring](#Scoring) for details), but roughly speaking, the "first-score" is a number between zero and one: the rank of the first-ranked faulty line, divided by the number of lines of code in the program (and same for the "last-score," using the last-ranked faulty line).

- For MBFL techniques:

    * From the killmap file, construct a "kill-matrix," which has the same format as a coverage matrix. Each cell tells you whether a given test killed a given mutant.
    * Crush the kill-matrix, exactly the same way we would crush the coverage matrix, to produce a "mutant-suspiciousness vector."
    * "Aggregate" the values in the mutant-suspiciousness vector to produce a statement-suspiciousness vector.  That is, partition the mutants in the MSV based on which statement contains them, then basically average the suspiciousnesses of the mutants in each statement to get a suspiciousness for the statement.
    * Expand the statements to lines, sort the lines, and score the ranking, just like for SBFL.

- For hybrid techniques:

    * From the killmap file, construct a "mutant-coverage matrix", which has the same format and meaning as a SBFL coverage matrix (except the columns correspond to mutants, not statements).
    * Create the kill-matrix and crush it, exactly as in MBFL; except use the mutant-coverage matrix to adjust each mutant's suspiciousness, based on how many passing/failing tests covered it.
    * Aggregate, expand, sort, and score, exactly as in MBFL.

Different FLTs are parametrized by the method by which they (a) determine whether a mutant was "killed," (b) crush matrices, (c) aggregate mutant-suspiciousnesses, and (d) adjust mutant-suspiciousnesses for coverage.

Different steps are performed by different scripts in the pipeline; see the [Script Specifications](#script-specifications) section for a description of each step. The scripts produce intermediate files, which have formats described in the [File Formats](#file-formats) section.



Wrapper Scripts and Usage
=========================

Things you'll need to do to prepare your environment:

- ensure that `source-code-lines.tar.gz` is unzipped into `analysis/pipeline-scripts/source-code-lines/`;
- export the environment variable `FL_DATA_HOME`, pointing to the root of this repository.

Now that you've done that: suppose you've run GZoltar and Killmap to get the four important data files for Lang 1, using the developer-written test suite. To score all the FLTs' performance on that data, run either

        do-full-analysis Lang 1 developer \
          cov-matrix.txt cov-spectra.txt \
          killmap.csv mutants.log \
          /tmp/scoring/Lang/1/developer \
          scores.csv

or, if the big files are gzipped,

        do-full-analysis-from-gzips Lang 1 developer \
          cov-matrix.gz cov-spectra.gz \
          killmap.gz mutants.log \
          /tmp/scoring/Lang/1/developer \
          scores.csv

This will run every FLT on the given data, score the FLTs' output, and dump the results into `scores.csv`.

Under the hood, `do-full-analysis` invokes `do-mbfl-analysis` and `do-sbfl-analysis` (which score all the techniques in their respective families), and then it invokes `gather-scores-into-master-scoring-file.py`, which takes all those scores and builds a [master scoring file](#master-scoring-file) out of them.

Under the hood, `do-mbfl-analysis` and `do-sbfl-analysis` invoke the basic pipeline scripts described below, often repeatedly, to score every single FLT. For example, `do-sbfl-analysis` invokes `crush-matrix` 12 times (6 formulas times 2 total-defns). The intermediate files are placed in the specified working directory.



File Formats
============

A couple preliminary notes about how these file formats identify statements/lines/mutants:

- A "statement id" consists of the statement's class-name and and the line number on which it starts:

        mypackage.MyClass#202
        mypackage.MyClass$InnerClass#450

- A "mutant id" is a positive integer:

        11

- A "line id" consists of the line's filename (relative to the source code directory) and line-number:

        org/apache/commons/lang3/StringUtils.java#212


Now, here are the specifications for the intermediate file formats:

Test-outcome matrices
---------------------

Test-outcome matrices are generated by Killmap. They look like

        mypackage.MyTestClass::testSomething,0,1000,FAIL,128,da39...709,11 12 13,java.lang.NumberFormatException: ...
        mypackage.MyTestClass::testSomething,11,256,FAIL,128,da39...709,,java.lang.OutOfMemoryError: ...
        ...

where each line represents "the outcome of running `<test>` with `<mutant>` enabled", and has the form

        <test case>,<mutant id>,<timeout>,<outcome>,<runtime>,<output hash>,<covered mutants>,<stack trace>

Nonobvious notes:

- The "mutant id" is either a positive integer (i.e. a real mutant id) or 0, meaning no mutant was enabled.
- The "timeout" is the number of milliseconds allocated for the test case to run.
- The "outcome" is PASS/FAIL/TIMEOUT/CRASH, describing the general type of outcome.
- The "runtime" is the number of milliseconds the test case actually took to run.
- The "output hash" is the concatenation of two SHA-1 hashes: one of whatever the test wrote to stdout, one of whatever it wrote to stderr.
- The "covered mutants" is empty unless the "mutant id" column is 0.
- The "stack trace" is the thrown exception's stack trace, if any. Leading/trailing whitespace is stripped, and any bunch of whitespace including a newline is replaced by a single space (i.e. `\s*\n\s*` is replaced with ` `).
- The stack trace may contain commas. Beware! Parsing this as a CSV and reading column 8 may only give you the first fragment of the full stack trace.




Coverage matrices
-----------------

Coverage matrices are generated by:

- for SBFL, GZoltar
- for MBFL, `outcome-matrix-to-kill-matrix`
- for hybrid, `outcome-matrix-to-kill-matrix` and `outcome-matrix-to-coverage-matrix`

They look like

        0 0 1 0 ... 0 1 1 1 +
        1 0 0 0 ... 0 0 1 0 -
        ...

where a 1 in the Mth column of the Nth row indicates that the Nth test covered the Mth statement (as ordered by the `cov-spectra` file) (or killed the Mth mutant, as ordered by Major's `mutants.log`). The `+/-` at the end of each row indicates whether the test originally passed (`+`) or failed (`-`).



Statement-suspiciousness vectors
--------------------------------

Statement-suspiciousness vectors are generated by the `crush-matrix` script. They look like

        Statement,Suspiciousness
        mypackage.MyClass#303,0.3
        mypackage.MyClass#304,0.1
        mypackage.MyClass#308,0.5
        mypackage.OtherClass#212,1
        ...

where each row is of the form `<statement id>,<suspiciousness>`.


Mutant-suspiciousness vectors
-----------------------------

Mutant-suspiciousness vectors look the same as statement-suspiciousness vectors, but with mutant ids instead of statement ids, and the first column header is "Mutant" instead of "Statement". For MBFL and hybrid techniques, `crush-matrix` actually produces mutant-suspiciousness vectors, which are immediately translated into statement-suspiciousness vectors by the `aggregate-mutant-susps-by-stmt` script.



Line-suspiciousness vectors
---------------------------

Line-suspiciousness vectors are generated by the `stmt-susps-to-line-susps` script. They look like

        Line,Suspiciousness
        mypackage/MyClass.java#303,0.3
        mypackage/MyClass.java#304,0.1
        mypackage/MyClass.java#308,0.5
        mypackage/OtherClass.java#212,1
        ...

where each row is of the form `<line id>,<suspiciousness>`.




FLT scores
----------

FLT scores are generated by the `score-ranking` script. They look like

        0.1

These scores are very nearly the end goal of the entire pipeline. Each score tells you how well a given FLT does at localizing the fault in a given buggy version, using a given test suite. Smaller is better. (See the [Scoring](#scoring) section for details.)




Master scoring file
-------------------

The master scoring file is generated by `gather-scores-into-master-scoring-file`. **This file is the end result of the entire pipeline.** It looks like this:

        Project,Bug,TestSuite,ScoringScheme,Family,Formula,TotalDefn,KillDefn,HybridScheme,AggregationDefn,Score
        Chart,24,developer,first,mbfl,ochiai,tests,exact,none,avg,0.0461538461538
        Chart,24,developer,last,mbfl,ochiai,tests,exact,none,avg,0.0461538461538
        ...
        Closure,14,randoop,last,mbfl,ochiai,tests,type,none,max,N/A
        ...

Every row describes a score that a FLT got. The first four columns describe the evaluation method: "We ran the FLT on the bug with this id, for this project, with this test suite, and looked for the first/last faulty line in the ranking." The next six columns describe the FLT itself. The last column is *that* FLT's score when evaluated in *that* way. (If the FLT doesn't have a score for that evaluation method, for example because its ranking doesn't include any faulty lines, the last column is `N/A` instead.)





Script Specifications
=====================

There are 6 discrete steps in the process of turning Killmap's/GZoltar's output into score-files. Each step is performed by its own script:


1. `outcome-matrix-to-kill-matrix` (only used in MBFL/hybrid) turns a test-outcome matrix into a kill-matrix, indicating which tests kill which mutants.

    Conceptually, we define an equivalence relation over test-outcomes, and a mutant is "killed" by a test iff enabling the mutant changes the eq-class of the test's outcome. There is exactly one eq-class for "test passed," one for "test timed out," one for "test crashed the JVM," and several for "test threw an exception," depending on the "error partition scheme," described below.

    Usage:

        outcome-matrix-to-kill-matrix \
          --error-partition-scheme SCHEME \
          --outcomes FILE \
          --mutants FILE \
          --output FILE

    where SCHEME is one of:

    - `exact` (exceptions that have exactly the same stack trace are equivalent)
    - `type+message+location` (exceptions with the same type, message, and location are equivalent)
    - `type+message` (exceptions with the same type and message are equivalent)
    - `type` (exceptions of the same type are equivalent)
    - `all` (all exceptions are equivalent)
    - `pass/fail` (all exceptions are equivalent, *and* the eq-class for exceptions is merged with the eq-classes for timeouts and crashes. That is, there are only two eq-classes: "pass" and "not pass")

    and `--mutants` points to the `mutants.log` file produced by Major (needed to determine how many mutants there are, i.e. how many columns the matrix should have).

2. `outcome-matrix-to-coverage-matrix` (only used in MBFL/hybrid) turns a test-outcome matrix into a coverage matrix, indicating which tests cover which mutants. Usage:

        outcome-matrix-to-coverage-matrix \
          --outcomes FILE \
          --mutants FILE \
          --output FILE

    where `--mutants` points to the `mutants.log` file produced by Major (needed to determine how many mutants there are, i.e. how many columns the matrix should have).


3. `crush-matrix` (used in all families) turns a coverage/kill-matrix into a statement/mutant suspiciousness vector. Usage:

        crush-matrix \
          --formula (tarantula|ochiai|dstar2|barinel|opt2|muse|jaccard) \
          --matrix FILE \
          --element-type (Statement|Mutant) \
          --element-names FILE \
          --total-defn (tests|mutants) \
          [--hybrid-scheme (numerator|constant|mirror|coverage-only) \
           --hybrid-coverage-matrix FILE] \
          --output FILE

    where `--element-type` indicates whether the entries of the resulting vector correspond to statements or mutants;

    and `--element-names` is the path to a file whose nth line identifies the code element (statement/mutant) to which the nth matrix column refers

    and `--total-defn` indicates whether, in the formula, "totalpassed" should refer to the number of passing tests, or the number of times a passing test covers/kills an element. (And the same for "totalfailed".)

    `--hybrid-scheme` and `--hybrid-coverage-matrix` should come together or not at all. They should be present iff the FLT being performed belonged to the "hybrid" family; in this case,

      * `--matrix` indicates the *mutant kill-matrix* (telling which tests kill which mutants),
      * `--hybrid-coverage-matrix` indicates the *mutant coverage-matrix* (telling which tests cover which mutants), and
      * `--hybrid-scheme` indicates how the two matrices are combined: if `numerator`, mutants covered by failing tests will have some small/moderate increase in suspiciousness (usually by incrementing the numerator of some fraction by 1); if `constant`, mutants covered by failing tests will have some large increase in suspiciousness (incrementing the whole suspiciousness by 1); if `mirror`, each mutant's suspiciousness will be the sum of (the formula applied to its column in the kill-matrix) and (the formula applied to its column in the coverage-matrix); if  `coverage-only`, the formula will *only* be applied to its column in the coverage-matrix, and the kill-matrix will be ignored.

4. `aggregate-mutant-susps-by-stmt` (used in MBFL/hybrid only) takes a mutant-suspiciousness vector and generates a statement-suspiciousness vector. Usage:

        aggregate-mutant-susps-by-stmt \
          --accumulator (avg|max) \
          --mutants FILE \
          --source-code-lines FILE \
          --loaded-classes FILE \
          --mutant-susps FILE \
          --output FILE

    where `--source-code-lines` points to a file whose lines look like "PATH#LINE1:PATH#LINE2", indicating that the statement in file PATH, starting on line LINE1, extends into LINE2. (The `java-parser` tool produces these files.)

    `--loaded-classes` points to a file that lists all the project-classes that were loaded by any triggering test. (This file is provided by Defects4J. It's needed because sometimes, Major mutates statements in irrelevant classes, and we don't want those statements to gunk up the rest of the pipeline.)

5. `stmt-susps-to-line-susps` (used in all families) takes a statement-suspiciousness vector, and generates a line-suspiciousness vector. Usage:

        stmt-susps-to-line-susps \
          --source-code-lines FILE \
          --stmt-susps FILE \
          --output FILE

    where `--source-code-lines`, just like with the `aggregate-mutant-susps-by-stmt` script, points to a file produced by the `java-parser` tool.

6. `score-ranking` (used in all families) takes a line-suspiciousness vector and a Defects4J project-and-bug-id, and figures out what fraction of the program need not be examined. Usage:

        score-ranking \
          --project PROJECT \
          --bug N \
          --line-susps FILE \
          --sloc-csv FILE \
          --buggy-lines FILE \
          --scoring-scheme (first|last) \
          --output FILE

    `--sloc-csv` points to a CSV whose lines are like `Chart,1,13269`, indicating that there are 13269 lines of fault-relevant source code in Chart 1b.

    `--buggy-lines` points to the `.buggy.lines` file belonging to the given project/bug. (These files live in the `buggy-lines` directory.)

    `--scoring-scheme` indicates whether the FLT's score should be the fraction of the way through the ranking you have to go before encountering the first patched line, or the last.



Scoring
=======

How do we determine the quality of a ranking of source code lines?

To a first approximation: from the bugfix patch, we can identify a set of faulty lines. We find which of those lines has the lowest rank, and divide that rank by the number of lines in the program, getting a number between 0 and 1. (We do the same thing for the faulty line with the highest rank.)

Let's break that down and give all the details:

- To "identify a set of faulty lines," some script (`fault-localization-data/d4j_integration/get_buggy_lines.sh`) goes through all D4J's bugfix patches, and from that patch, generates all of the `.buggy.lines` files you see in the `buggy-lines` directory.

- Now, there are two kinds of fault: changes (including deletions) and omissions. Omissions are different in that there may have been many places the developer *could* have added code to fix the bug -- e.g. perhaps the bugfix involved adding a counter that's incremented every iteration of a loop; but it could be incremented anywhere in the loop, not *only* at the point where the developer actually added the code.

    Therefore, we manually examined every fault of omission identified by the `.buggy.lines` files, and determined which set of lines the omission could have been inserted at. We put that information into a corresponding `.candidates` file.

- The "lowest-ranked faulty line" is the first line in the ranking that occurs in either the `.buggy.lines` file or the `.candidates` file. The "highest-ranked faulty line" is the first line in the ranking at which *every* faulty line has been "accounted for" in the ranking. A faulty line is "accounted for" when either (a) it appears directly in the ranking, or (b) if it's a fault of omission, one of its candidates appears in the ranking.

- The "number of lines in the program" is the number of lines of source code in all the Java files loaded by at least one failing test. (Excluding comments, whitespace, and otherwise uninteresting lines.) This set of source code lines is guaranteed to be a superset of the set of lines in the ranking, so the score will always be less than 1.
