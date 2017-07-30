#!/usr/bin/env bash
#
################################################################################
# This script determines the number of projects and the number of real faults
# for each project in Defects4J. It outputs these numbers as LaTex macros.

# Usage:
# ./get_fault_stats.sh
#
# Requirements:
# - The environment variable D4J_HOME needs to be set and must point to the Defects4J
#   installation.
#
################################################################################

#
# Print error message and exit
#
die() {
    echo $1
    exit 1
}

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"

num_bugs_total=0
num_subjects=0

# Iterate over all projects in Defects4J
for pid in Chart Closure Lang Math Mockito Time; do
    dir_project="$D4J_HOME/framework/projects/$pid"
    # Determine the number of real bugs for this project
    num_bugs=$(egrep "^[0-9][0-9]?[0-9]?," $dir_project/commit-db | wc -l | grep -o "[0-9]\+")
    echo "\\def\\nReal${pid}Faults{$num_bugs\\xspace}"

    num_bugs_total=$(( num_bugs_total + num_bugs ))
    num_subjects=$(( num_subjects + 1 ))
done

echo "\\def\\nRealFaults{$num_bugs_total\\xspace}"
echo "\\def\\nSubjectPrograms{$num_subjects\\xspace}"
