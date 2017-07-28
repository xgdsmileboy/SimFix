#!/bin/bash

die() {
    echo "$@" >&2
    exit 1
}

USAGE="$0 KILLMAP_STDERR_FILE"
[ $# = 1 ] || die "usage: $USAGE"

sed -n -e 's_\[actually took \([0-9.]*\)s;.*_\1_p' "$1" | python -c 'import sys; print(int(round(sum(float(line) for line in sys.stdin))))'
