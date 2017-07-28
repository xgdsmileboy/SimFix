#!/bin/bash

HERE=$(readlink --canonicalize "$(dirname "${BASH_SOURCE[0]}")")
SCRIPTS=$(dirname "$HERE")
PATH="$SCRIPTS:$PATH"

if [ "$#" != 4 ]; then echo "usage: $0 PROJECT BUG OUTCOME_MATRIX MUTANTS_LOG" >&2; exit 1; fi
PROJECT=$1
BUG=$2
OUTCOME_MATRIX="$(readlink --canonicalize "$3")"; if [ ! -f "$OUTCOME_MATRIX" ]; then echo "given outcome matrix does not exist" >&2; exit 1; fi
MUTANTS="$(readlink --canonicalize "$4")"; if [ ! -f "$MUTANTS" ]; then echo "given mutants.log does not exist" >&2; exit 1; fi

MUTANT_NAMES=$(pwd)/mutant-names.txt
MUTANT_COVERAGES=$(pwd)/mutant-coverages.txt

cut -f 1 -d ':' <"$MUTANTS" >"$MUTANT_NAMES"

for PARTITIONER in passfail; do
  DIR="killdefn-$PARTITIONER"; mkdir -p "$DIR"; pushd "$DIR" >/dev/null
  KILLS_FILE="$(pwd)/kills.txt"
  outcome-matrix-to-kill-matrix \
    --error-partition-scheme "$PARTITIONER" \
    --outcomes "$OUTCOME_MATRIX" --mutants "$MUTANTS" \
    --output "$KILLS_FILE" || exit 1

  for FORMULA in muse; do
    DIR="formula-$FORMULA"; mkdir -p "$DIR"; pushd "$DIR" >/dev/null

    for TOTAL_DEFN in elements; do
      DIR="totaldefn-$TOTAL_DEFN"; mkdir -p "$DIR"; pushd "$DIR" >/dev/null

      for HYBRID in none; do

        DIR="hybrid-$HYBRID"; mkdir -p "$DIR"; pushd "$DIR" >/dev/null
        MUTANT_SUSPS_FILE="$(pwd)/mutant-susps.txt"
        crush-matrix --formula "$FORMULA" --matrix "$KILLS_FILE" \
                     --element-type 'Mutant' \
                     --element-names "$MUTANT_NAMES" \
                     --total-defn "$TOTAL_DEFN" \
                     --output "$MUTANT_SUSPS_FILE" || exit 1

        for AGGREGATOR in avg; do
          DIR="aggregator-$AGGREGATOR"; mkdir -p "$DIR"; pushd "$DIR" >/dev/null
          STMT_SUSPS_FILE="$(pwd)/stmt-susps.txt"
          aggregate-mutant-susps-by-stmt \
                    --accumulator "$AGGREGATOR" --mutants "$MUTANTS" \
                    --source-code-lines "$SCRIPTS/source-code-lines/$PROJECT-${BUG}b.source-code.lines" \
                    --loaded-classes "$DEFECTS4J_HOME/framework/projects/$PROJECT/loaded_classes/$BUG.src" \
                    --mutant-susps "$MUTANT_SUSPS_FILE" \
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

      popd >/dev/null
    done

    popd >/dev/null
  done

  popd
done