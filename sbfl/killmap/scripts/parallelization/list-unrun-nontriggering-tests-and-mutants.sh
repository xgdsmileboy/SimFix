#!/bin/bash
#
# Lists which mutants need to be run for which tests
# in order to finish a partially-completed killmap.
#
# Usage:
#   list-unrun-nontriggering-tests-and-mutants.sh D4J_PROJECT_DIR KILLMAP
# For example:
#   list-unrun-nontriggering-tests-and-mutants.sh /tmp/Lang-1b/ Lang-1b.killmap.csv
#

HERE="$(readlink --canonicalize "$(dirname "${BASH_SOURCE[0]}")")"

die() {
  echo "$@" >&2
  exit 1
}

[ $# = 2 ] || die "usage: $0 D4J_PROJECT_DIR KILLMAP"
[ "$FL_DATA_HOME" ] || die 'FL_DATA_HOME must be set'
[ "$D4J_HOME" ] || die 'D4J_HOME must be set'

D4J_PROJECT_DIR="$1"
KILLMAP=$(readlink --canonicalize "$2")

cd "$D4J_PROJECT_DIR" || die "unable to cd into $D4J_PROJECT_DIR"
$D4J_HOME/framework/bin/defects4j compile || die 'failed to compile the project'
[ -f triggering-tests.txt ] || $D4J_HOME/framework/bin/defects4j export -p tests.trigger -o triggering-tests.txt || die 'unable to export triggering tests'
[ -f relevant-test-classes.txt ] || $D4J_HOME/framework/bin/defects4j export -p tests.relevant -o relevant-test-classes.txt || die 'unable to export relevant test classes'
[ -f test-classpath.txt ] || $D4J_HOME/framework/bin/defects4j export -p cp.test -o test-classpath.txt || die 'unable to export relevant test-classpath'
java -cp "$D4J_HOME/major/lib/junit-4.11.jar:$FL_DATA_HOME/killmap/bin:$(cat test-classpath.txt)" killmap.TestFinder relevant-test-classes.txt > relevant-tests.txt || die 'unable to list all tests'
grep --fixed-strings --line-regexp --invert-match --file=triggering-tests.txt < relevant-tests.txt > nontriggering-tests.txt || die 'unable to filter out triggering tests'

zcat "$KILLMAP" | cut -d , -f 1 | uniq > tests-worked-on-in-killmap.txt
[ "$(grep '^' tests-worked-on-in-killmap.txt | wc -l)" -gt "$(grep '^' triggering-tests.txt | wc -l)" ] || die "the killmap isn't finished running the triggering tests!"


MUTANTS=$(python "$HERE/list-interesting-mutants.py" "$KILLMAP") || die 'unable to identify interesting mutants'

(echo "Test,Mutants"
for TEST in $(cat nontriggering-tests.txt); do
  echo "$TEST,$MUTANTS"
done) | python "$HERE/subtract-finished-mutants.py" "$KILLMAP"
