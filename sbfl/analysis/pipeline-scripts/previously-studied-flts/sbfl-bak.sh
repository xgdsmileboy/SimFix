#!/bin/bash

HERE=$(readlink --canonicalize "$(dirname "${BASH_SOURCE[0]}")")
SCRIPTS=$(dirname "$HERE")
PATH="$SCRIPTS:$PATH"

if [ "$#" != 4 ]; then echo "usage: $0 PROJECT BUG COVERAGE_MATRIX STATEMENT_NAMES" >&2; exit 1; fi
PROJECT=$1
BUG=$2
COVERAGE_MATRIX="$(readlink --canonicalize "$3")"; if [ ! -f "$COVERAGE_MATRIX" ]; then echo "given coverage matrix does not exist" >&2; exit 1; fi
STATEMENT_NAMES="$(readlink --canonicalize "$4")"; if [ ! -f "$STATEMENT_NAMES" ]; then echo "given statement-names file does not exist" >&2; exit 1; fi

for FORMULA in tarantula ochiai opt2 barinel dstar2 jaccard; do
  DIR="formula-$FORMULA"; mkdir -p "$DIR"; pushd "$DIR" >/dev/null

  for TOTAL_DEFN in tests; do
    DIR="totaldefn-$TOTAL_DEFN"; mkdir -p "$DIR"; pushd "$DIR" >/dev/null

    STMT_SUSPS_FILE="$(pwd)/stmt-susps.txt"
    crush-matrix --formula "$FORMULA" --matrix "$COVERAGE_MATRIX" \
                 --element-type 'Statement' \
                 --element-names "$STATEMENT_NAMES" \
                 --total-defn "$TOTAL_DEFN" \
                 --output "$STMT_SUSPS_FILE" || exit 1

    LINE_SUSPS_FILE=$(pwd)/line-susps.txt
    stmt-susps-to-line-susps --stmt-susps "$STMT_SUSPS_FILE" \
                             --source-code-lines "$SCRIPTS/source-code-lines/$PROJECT-${BUG}b.source-code.lines" \
                             --output "$LINE_SUSPS_FILE"
    for SCORING_SCHEME in first; do
      DIR="scoring-$SCORING_SCHEME"; mkdir -p "$DIR"; pushd "$DIR" >/dev/null
      DEST="$(pwd)/score.txt"
      score-ranking --project "$PROJECT" --bug "$BUG" \
                    --line-susps <(tail -n +2 "$LINE_SUSPS_FILE") \
                    --scoring-scheme "$SCORING_SCHEME" \
                    --sloc-csv "$SCRIPTS/buggy-lines/sloc.csv" \
                    --buggy-lines "$SCRIPTS/buggy-lines/$PROJECT-$BUG.buggy.lines" \
                    --output "$DEST"
      popd >/dev/null
    done

    popd >/dev/null
  done

  popd >/dev/null
done
