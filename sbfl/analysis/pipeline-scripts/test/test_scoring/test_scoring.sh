#!/bin/sh

#
# Use Math-35 as the program version as it has exactly 800 lines of code (sloc).
#
PID=Math
BID=35

score() {
    local test=$1
    local scheme=$2
    ../../score-ranking --project $PID --bug $BID \
                 --scoring-scheme $scheme \
                 --line-susps $test.ranking \
                 --sloc-csv ../../buggy-lines/sloc.csv \
                 --buggy-lines $test.buggy.lines \
                 --output $test.$scheme.score \
    > $test.$scheme.log 2>&1
    if diff -q $test.$scheme.score $test.$scheme.score.expected > /dev/null; then
        echo "$test.$scheme PASSED"
    else
        echo ">>> $test.$scheme FAILED!"
    fi
}

test_all() {
    local test=$1
    for scheme in first last mean median; do
        score $test $scheme
    done
}

# Remove previous results
rm -f *.log *.score

# Test a complete ranking with candidates
test_all complete

# Test a complete ranking with ties for first and last
test_all ties

# Test a missing ranking (only last should result in the expected EXAM score for
# a missing line)
test_all incomplete

# Test a missing ranking (first and last should result in the expected EXAM
# score for a missing line)
test_all na

# Test whether the unrankable file is considered
test_all unrankable

# Test whether the candidates file is considered for all types of faults
test_all candidates
