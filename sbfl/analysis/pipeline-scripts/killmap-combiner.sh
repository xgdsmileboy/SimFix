#!/bin/bash
#
# Combines a partial killmap with the result of an incremental run (presumably
# the first run timed out), to produce a single, full killmap.
#
# Usage:
#   killmap-combiner.sh first-half.killmap.csv.gz second-half.killmap.csv.gz full.killmap.csv.gz
#
# (The inputs should both be gzipped files; the output will also be gzipped.)
#
# Exits with status 1 if it can't figure out how to glue the two halves together.
#

cleanup() {
    [ -f "$TMP_BASE" ] && rm -f "$TMP_BASE"
    [ -f "$TMP_EXTENSION" ] && rm -f "$TMP_EXTENSION"
    [ -f "$TMP_FULL" ] && rm -f "$TMP_FULL"
    return 0
}

die() {
    cleanup
    echo "$@" >&2
    exit 1
}
KILLMAP_BASE=$1
KILLMAP_EXTENSION=$2
KILLMAP_FULL=$3

USAGE="$0 BASE EXTENSION OUTPUT"
[ "$#" = 3 ] || die "usage: $USAGE"

[ -f "$KILLMAP_BASE" ] || die "$KILLMAP_BASE does not exist"
[ -f "$KILLMAP_EXTENSION" ] || die "$KILLMAP_EXTENSION does not exist"

FIRST_EXTENSION_TEST_AND_MUTANT=$(zcat "$KILLMAP_EXTENSION" | head -n 1 | cut -d , -f 1,2)

TMP_FULL="/tmp/killmap-combiner-$$-full.killmap.csv"

(zcat "$KILLMAP_BASE" | sed -e "/^$FIRST_EXTENSION_TEST_AND_MUTANT,/q" | head -n -1;
 zcat "$KILLMAP_EXTENSION") | gzip > "$KILLMAP_FULL"

cleanup
