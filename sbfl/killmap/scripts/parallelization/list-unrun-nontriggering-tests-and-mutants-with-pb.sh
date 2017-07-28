#!/bin/bash
#
# Lists which mutants need to be run for which tests
# in order to finish a partially-completed killmap.
#
# Usage:
#   list-unrun-nontriggering-tests-and-mutants.sh PROJECT BUG OUTPUT_FILE D4J_PROJECT_DIR KILLMAP
# For example:
#   list-unrun-nontriggering-tests-and-mutants.sh Lang 1 Lang-1b.unrun-nontriggering-tests-and-mutants.txt /tmp/Lang-1b/ Lang-1b.killmap.csv
#

die() {
  echo "$@" >&2
  exit 1
}
[ $# -eq 5 ] || die "usage: $0 PROJECT BUG OUTPUT_FILE D4J_PROJECT_DIR KILLMAP"

PROJECT=$1; shift
BUG=$1; shift
OUTPUT_FILE=$1; shift

HERE="$(readlink --canonicalize "$(dirname "${BASH_SOURCE[0]}")")"

"$HERE/list-unrun-nontriggering-tests-and-mutants.sh" "$@" | (
  IFS= read HEADER_LINE;
  echo "Project,Bug,$HEADER_LINE" > "$OUTPUT_FILE"
  while IFS= read LINE; do
    echo "$PROJECT,$BUG,$LINE" >> "$OUTPUT_FILE"
  done
)

echo "DONE!"
