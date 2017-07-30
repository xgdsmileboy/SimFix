#!/bin/bash
#
# Extends many killmaps read from a file.
# Usage: extend-killmaps.sh KILLMAPS_DIR TARGETS_CSV
#  where KILLMAPS_DIR points to a directory containing the killmaps to extend,
#  and TARGETS_CSV enumerates (project, bug) pairs whose killmaps to extend.
# Modifies the killmaps in-place.
#
#  KILLMAPS_DIR should contain subdirectories named after projects, containing subdirectories named with bug IDs, containing `killmap.csv.gz` files, e.g.
#    $KILLMAPS_DIR/
#      Closure/
#        3/
#          killmap.csv.gz
#        100484/
#          killmap.csv.gz
#      Lang/
#        1/
#          killmap.csv.gz
#
#  TARGETS_CSV should look like
#
#    Closure,100484
#    Lang,1
#

die() { echo "$@" >&2; exit 1; }
USAGE="$0 KILLMAPS_DIR TARGETS_CSV"
[ $# = 2 ] || die "usage: $USAGE"
[ "$FL_DATA_HOME" ] || die 'FL_DATA_HOME must be set'

KILLMAPS_DIR=$1
TARGETS_CSV=$2

while IFS=, read PROJECT BUG; do
  [ "$BUG" ] && [ "$PROJECT $BUG" = "Project Bug" ] && continue
  DIR="$KILLMAPS_DIR/$PROJECT/$BUG"
  [ -f "$DIR/killmap.csv.gz" ] || die "expected to find a killmap to extend at $DIR/killmap.csv.gz"

  echo "Extending killmap for $PROJECT-$BUG..." >&2
  "$FL_DATA_HOME/killmap/scripts/generate-matrix.sh" \
    --partial-output "$DIR/killmap.csv.gz" \
    "$PROJECT" "$BUG" \
    "/tmp/generate-killmap-$PROJECT-$BUG" "$DIR/mutants.log" \
    2>"$DIR/extension.log.err" \
    | gzip > "$DIR/extension.killmap.csv.gz" \
    || exit 1

  echo "Done generating extension! Combining with previous killmap..." >&2
  "$FL_DATA_HOME/analysis/pipeline-scripts/killmap-combiner.sh" \
    "$DIR/killmap.csv.gz" \
    "$DIR/extension.killmap.csv.gz" \
    "$DIR/full.killmap.csv.gz" \
    || exit 1
  mv "$DIR/full.killmap.csv.gz" "$DIR/killmap.csv.gz" || exit 1
  rm "$DIR/extension.killmap.csv.gz"

  echo "Done with $PROJECT-$BUG!" >&2

done < "$TARGETS_CSV"
