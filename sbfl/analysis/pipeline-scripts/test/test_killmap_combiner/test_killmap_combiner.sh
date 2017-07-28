#!/bin/bash

cd "$(dirname "${BASH_SOURCE[0]}")" || exit 1

COMBINE=$(readlink --canonicalize ../../killmap-combiner.sh)

die() {
  echo "$@" >&2
  exit 1
}

assert-combination-equals() {
  "$COMBINE" "$1" "$2" "/tmp/test_killmap_combiner_$$.csv.gz" || die "combination failed: $1 + $2"
  diff <(zcat "/tmp/test_killmap_combiner_$$.csv.gz") <(zcat "$3") || die "assertion failed: $1 + $2 == $3"
}
assert-combination-fails() {
  "$COMBINE" "$1" "$2" "/tmp/test_killmap_combiner_$$" 2>/dev/null && die "combination should have failed: $1 + $2"
  return 0
}

assert-combination-equals abc.csv.gz cd.csv.gz abcd.csv.gz
assert-combination-equals abc.csv.gz abcd.csv.gz abcd.csv.gz
assert-combination-equals abcd.csv.gz abcd.csv.gz abcd.csv.gz
assert-combination-equals ab-partial-c.csv.gz abcd.csv.gz abcd.csv.gz
assert-combination-equals abc-partial-d.csv.gz abcd.csv.gz abcd.csv.gz
