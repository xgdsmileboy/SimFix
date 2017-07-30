Overview
========

This directory contains scripts that perform mutation analysis.
The resultant files are called "killmaps," and specify how each test behaves on each mutant.


Installation
============

- Clone this repository.
- Make sure your `DEFECTS4J_HOME` environment variable points to a Defects4J installation.
- `cd killmap; ant compile`


Usage
=====

Now that you've installed this, whenever you want to generate a matrix,

    ./killmap/scripts/generate-matrix.sh Lang 1 tmp-dir Lang-1-mutants.log

will print it as a CSV to stdout, and save information about the mutants thus analyzed to `Lang-1-mutants.log`. Probably, you instead want to compress and save the output, in which case you can do something like

    ./killmap/scripts/generate-matrix.sh Lang 1 tmp-dir Lang-1-mutants.log 2>lang-1-err.txt | gzip > lang-1-matrix.gz

For details on the printed CSV, see [../analysis/README.md](../analysis/README.md).

The script will attempt to reuse information from a previous run, rather than re-running tests; to this end, `previous-run.csv` can contain any subset of lines from a previous run of the script, in the order they were produced. Some examples:

- The first time you run the script, `/dev/null` is a good choice.
- If you run `generate-matrix.sh ... > matrix.csv` but the process is interrupted halfway through, you can run `generate-matrix.sh --partial-output matrix.csv ... > matrix-2.csv`; it will reuse the results in `matrix.csv`.
- If you compressed the output of a previous run, e.g. with `generate-matrix.sh ... | gzip > matrix.gz`, you can use process substitution: `generate-matrix.sh --partial-output <(zcat matrix.gz) ...`.
- If you think something went wrong with a particular test-run, you can delete that line from the matrix and re-run the script, passing in that matrix; every test-result from the original run will be reused, except the deleted result, which will be re-calculated.


How it Works
============

High-level view
---------------

`generate-matrix.sh` just does some preparation, then invokes the Java class `killmap.Main`.

`Main` needs to determine the outcome of every test, run with every (or no) mutant. It makes use of two optimizations:

- if test T doesn't cover mutant M, its outcome with M enabled will be the same as its outcome with no mutants enabled;
- if no triggering test changes because of M, the outcomes of passing tests with M enabled are irrelevant. (This isn't obvious; the formulas we use just happen to have the feature that if $failed(s)=0$ then $S(s)=0$.)

So `Main` needs to run every test with every mutant that (a) the test covers and (b) at least one triggering test changes behavior because of. To do this, it:

- runs each failing test once with no mutants, then once for each mutant it covers, recording which mutants change its behavior;
- runs each passing test once with no mutants, then once for each mutant it covers if that mutant changed the behavior of any failing test.

And as it goes, it prints the result of every test-run as a line of a CSV.


How a test is run
-----------------

What happens when we run a test?

Most importantly, the tests are all run in a subprocess "worker JVM," because tests might do nasty stuff like eat all the memory. From the perspective of the "host JVM" (the main process), running tests looks like this:

1. (If necessary) Spawn a worker subprocess, by executing `java ... killmap.TestRunner ...`. Listen on a certain port for the worker to connect.
2. Over the socket, give the worker a "work order," consisting of a test to run, a mutant to enable, and a timeout.
3. Wait for the worker to respond with the test-run's outcome.

If the worker ever fails to respond in step 3, the host kills it and, next time a test needs to be run, it'll spawn a new worker. All of this logic lives in the `RemoteTestRunner` class.

From the worker's perspective, running a test looks like this:

1. Read a work order from the socket.
2. Replace `System.out` and `System.err` with dummy streams that can easily eat up infinite amounts of data (because some tests will print infinite amounts of data, and we have to deal with that).
3. In a new thread, replace the thread's classloader with a fresh one (to isolate the effects of the impending test-run); enable the given mutant; then run the given test.
4. If that thread returns before the timeout expires, take the returned outcome; otherwise, create an outcome meaning "timed out".
5. Write that outcome to the socket.

Almost all of that logic lives in the `TestRunner`. A little bit lives in `IsolatingClassLoader` and `DeadEndDigestOutputStream`.

There are four kinds of outcome:

- `PASS`: the test completed in time, with all assertions passing.
- `FAIL`: the test completed in time, but by raising an exception rather than passing.
- `TIMEOUT`: the test didn't complete in time, but the worker JVM was able to terminate it and clean up successfully.
- `CRASH`: everything else. The test must've done something nasty (e.g. raised an `OutOfMemoryError`; made the worker completely unresponsive; refused to halt when the thread was interrupted).
