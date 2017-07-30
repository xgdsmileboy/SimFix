#!/usr/bin/env bash

PWD=`pwd`

#
# Print error message and exit
#
die() {
  echo $1
  exit 1
}

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"

rm -f *.fixed.lines sloc.csv # remove previous data

for pid in Chart Closure Lang Math Time; do
  dir_project="$D4J_HOME/framework/projects/$pid"

  # Determine the number of bugs for this project
  num_bugs=$(cat $dir_project/commit-db | wc -l)

  # Iterate over all bugs for this project
  for bid in $(seq 1 $num_bugs); do
    sh ../../../d4j_integration/get_fixed_lines.sh $pid $bid .
  done
done

# EOF

