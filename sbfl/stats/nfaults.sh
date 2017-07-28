#!/bin/bash

SCORES_FILE=$1

PROJECTS_AND_BUGS="/tmp/nfaults_$$"

tail -n +2 "$SCORES_FILE" \
  | cut -d , -f 1-2 \
  | uniq | sort | uniq \
  > "$PROJECTS_AND_BUGS"

N_REAL=$(egrep ',[0-9]{1,3}$' "$PROJECTS_AND_BUGS" | wc -l)
N_ARTIFICIAL=$(egrep ',[0-9]{4,}$' "$PROJECTS_AND_BUGS" | wc -l)

echo '\def\nRealFaults{'"$N_REAL"'\xspace}'
echo '\def\nArtificialFaults{'"$N_ARTIFICIAL"'\xspace}'
