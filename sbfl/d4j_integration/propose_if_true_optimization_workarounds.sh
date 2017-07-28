#!/bin/bash
#
# Proposes changes to D4J's patches to prevent conditions like "if (true)" from being optimized away
#   (since, for SBFL to catch those issues, the condition must appear in the bytecode)
#
# For each patch-file $D4J_HOME/framework/projects/*/patches/*.src.patch,
# modifies it by replacing every occurrence of "true" or "false" in a modified line
# with 'Boolean.parse("true")' or 'Boolean.parse("false")'.
#
# The idea is that a human will then selectively stage the changes,
# probably with "git add -p".
#


die() {
    echo "$@" >&2
    exit 1
}

[ "$D4J_HOME" ] || die 'D4J_HOME must be set'

cd "$D4J_HOME/framework/projects"

for patch in */patches/*.patch; do
    egrep -q '^[0-9]+.(src|test).patch$' <<< "$(basename "$patch")" || continue;
    dest="$(dirname "$patch")/proposed-$(basename "$patch")"
    sed -i 's ^\([+].*[^"]\)\(true\|false\) \1Boolean.parse("\2") g' "$patch"
done

echo 'Done! Now, selectively stage changes (with "git add --patch"), commit, and push.'
