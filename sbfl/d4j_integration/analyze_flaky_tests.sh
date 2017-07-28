#!/usr/bin/env bash
#
# This script eases the debugging of flaky tests in Randoop. It performs the following
# high-level tasks:
# 1) Generate a Randoop test suite for the fixed program version of a given Defects4J bug.
# 2) Checkout the fixed program version of that given Defects4J bug.
# 3) Run the generated test suite and list all flaky tests.
# 4) Report the location of the generated test suite and of the log file that lists all
#    flaky tests (including the stack traces).
#
# Example:
# - check_flaky_tests.sh Time 1
#
# Requirements:
# - D4J_HOME must be set and point to the root directory of the Defects4J installation.
# - Defects4J must be initialized (run init.sh after cloning Defects4J)
# - Java 7

# The temporary directory to use
TMP_DIR=/tmp

#
# Print error message and exit
#
die() {
    echo $1
    exit 1
}

#
# Print usage message and exit
#
usage() {
    die "usage: $0 <Project ID> <Bug ID>"
}

# Check arguments and set PID and BID
[ $# -eq 2 ] || usage
PID=$1
BID=$2

# Test suite id
TID=1

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"

d4j="$D4J_HOME/framework/bin/defects4j"

# Run Randoop for given project id and bug id:
# -n -> test suite id (also controls the random seed for Randoop).
# -o -> directory to which the test suite and log files are written.
# -b -> time budget in seconds.
$D4J_HOME/framework/bin/run_randoop.pl -p $PID -v ${BID}f -n $TID -o $TMP_DIR/randoop_tests -b 60

WORK_DIR=$TMP_DIR/$PID-$BID
TEST_SUITE=$TMP_DIR/randoop_tests/$PID/randoop/$TID/$PID-${BID}f-randoop.$TID.tar.bz2

# Checkout program version and run test suite
$d4j checkout -p $PID -v ${BID}f -w $WORK_DIR
$d4j test -s $TEST_SUITE -w $WORK_DIR

# Let the user know where the test suite and log files are
echo "Test suite: $TEST_SUITE"
echo "Log file: $WORK_DIR/failing_tests.txt"
