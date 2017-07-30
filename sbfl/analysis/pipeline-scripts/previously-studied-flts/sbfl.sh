#!/bin/bash

HERE=$(readlink --canonicalize "$(dirname "${BASH_SOURCE[0]}")")
SCRIPTS=$(dirname "$HERE")
PATH="$SCRIPTS:$PATH"

if [ "$#" != 3 ]; then echo "usage: $0 PROJECT BUG BASENAME" >&2; exit 1; fi
PROJECT=$1
BUG=$2
BASE="$3/spectra/$PROJECT/$BUG"
COVERAGE_MATRIX="$BASE/matrix"
STATEMENT_NAMES="$BASE/spectra"

# echo $COVERAGE_MATRIX
# echo $STATEMENT_NAMES

for FORMULA in ochiai; do
  DIR="$FORMULA"; mkdir -p "$DIR"; pushd "$DIR" >/dev/null

  for TOTAL_DEFN in tests; do
    DIR="$PROJECT/$BUG"; mkdir -p "$DIR"; pushd "$DIR" >/dev/null
    STMT_SUSPS_FILE="$(pwd)/stmt-susps.txt"
    crush-matrix --formula "$FORMULA" --matrix "$COVERAGE_MATRIX" \
                 --element-type 'Statement' \
                 --element-names "$STATEMENT_NAMES" \
                 --total-defn "$TOTAL_DEFN" \
                 --output "$STMT_SUSPS_FILE" || exit 1
    popd >/dev/null
  done

  popd >/dev/null
done
