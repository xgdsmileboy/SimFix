#!/bin/bash

HERE=${BASH_SOURCE[0]}
source "$(dirname "$HERE")/utils.sh" || exit 1

USAGE="$0 [--no-preparation] [--partial-output FILE] [--relevant-test-classes FILE] [--triggering-tests FILE] PROJECT BUG DIR MUTANTS_LOG [--KILLMAP-OPTION ...]"

DOC="
usage: $USAGE

Generates the test-outcome matrix for the given Defects4J project,
printing it as a CSV to stdout. Uses the given DIR as a directory for scratch work.
Details about all generated mutants (i.e. Major's mutants.log output file)
will be written to MUTANTS_LOG.

If --no-preparation is given, the given DIR should already exist and already
have been prepared (i.e. compiled, mutated, etc.), probably by a previous
invocation of this script. The preparation step will then be skipped.

--partial-output may indicate the output of a previous run, possibly
missing some lines, as a cache to avoid re-running tests from previous iterations.

--relevant-test-classes and --triggering-tests may indicate files describing
which test-classes are relevant to the fault, and which tests fail because of
the fault; they have the same meaning and format as Defects4J's exports of
'tests.relevant' and 'tests.trigger'.

"

if user-is-asking-for-help "$@"; then
  echo "$DOC"
  exit 0
fi

PREPARE='true'

while [ "${1:0:2}" = '--' ]; do
  OPTION=$1; shift
  case "$OPTION" in
    ('--no-preparation') PREPARE='false' ;;
    ('--partial-output') PARTIAL=$1; shift ;;
    ('--relevant-test-classes') TESTCLASSES=$1; shift ;;
    ('--triggering-tests') TRIGGERS=$1; shift ;;
    (*) echo "Usage: $USAGE" >&2; exit 1 ;;
  esac
done

if [ "$#" -lt 4 ]; then
  echo "Usage: $USAGE" >&2
  exit 1
fi

PROJECT="$1"; shift
BUG="$1"; shift
DIR="$1"; shift
MUTANTS_LOG="$1"; shift
KILLMAP_OPTIONS=("$@")

if [ "$PREPARE" = 'true' ]; then
  d4j-checkout-and-prepare "$PROJECT" "$BUG" "$DIR" >&2 || exit 1
fi

cp "$DIR/mutants.log" "$MUTANTS_LOG" || exit 1

# Make all the paths absolute, because we change directories
# to actually generate the matrix.

if [ -z "$PARTIAL" ]; then
  PARTIAL='/dev/null'
elif [ "${PARTIAL:0:1}" != "/" ]; then
  PARTIAL=$(readlink --canonicalize "$PARTIAL")
fi
if [[ "$PARTIAL" = *.gz ]]; then
  NEW_PARTIAL="/tmp/killmap-$$-unzipped-partial-output"
  mkfifo "$NEW_PARTIAL"
  zcat "$PARTIAL" > "$NEW_PARTIAL" &
  PARTIAL="$NEW_PARTIAL"
fi

if [ -z "$TESTCLASSES" ]; then
  TESTCLASSES='relevant-test-classes.txt'
elif [ "${TESTCLASSES:0:1}" != "/" ]; then
  TESTCLASSES=$(readlink --canonicalize "$TESTCLASSES")
fi

if [ -z "$TRIGGERS" ]; then
  TRIGGERS='triggering-tests.txt'
elif [ "${TRIGGERS:0:1}" != "/" ]; then
  TRIGGERS=$(readlink --canonicalize "$TRIGGERS")
fi

cd "$DIR" || exit 1
d4j-generate-matrix-here "${KILLMAP_OPTIONS[@]}" "$TRIGGERS" "$TESTCLASSES" "$PARTIAL"
