#!/usr/bin/env bash
#
# This script determines, for a given real fault, all mutants that are within
# scope of the real fault AND detected by the developer-written test suite.
#
# This script writes a csv-based output to STDOUT. The output contains a header
# and the following columns: ProjectId,RealFaultId,ArtificialFaultId,MutantId
#
# Example:
# - command:
#   ./get_killable_mutants_in_scope.sh Lang 11
# - output:
#   ProjectId,RealFaultId,ArtificialFaultId,MutantId
#   Lang,11,1100042,75
#   Lang,11,1100043,77 
#
# Requirements:
# - D4J_HOME must be set and point to the root directory of the Defects4J
#   installation that is populated with artificial faults (mutants).

# The temporary directory to use
TMP_DIR=/tmp/get_killable_$$

#
# Print error message and exit
#
die() {
    echo $1
    exit 1
}

#
# Print usage message and exit
#
usage() {
    die "usage: $0 <Project ID> <Bug ID>"
}

# Check arguments and set PID and BID
[ $# -eq 2 ] || usage
PID=$1
BID=$2

[ -e $D4J_HOME/framework/projects/$PID/trigger_tests/$BID.mutants.map.csv ] \
    || die "Cannot find mutation information -- is Defects4J in D4J_HOME populated with mutants?"

mkdir -p $TMP_DIR || die "Cannot create temporary directory"

# Extract the archive of source-code.line files if necessary
[ -d ../analysis/pipeline-scripts/source-code-lines ] \
    || $(cd ../analysis/pipeline-scripts && tar -xzf source-code-lines.tar.gz)

# Obtain the set of mutants that are within scope of the real fault
python mutants_in_scope.py \
    $D4J_HOME/framework/projects/$PID/trigger_tests/$BID.mutants.log \
    ../analysis/pipeline-scripts/source-code-lines/$PID-${BID}f.source-code.lines \
    ../analysis/pipeline-scripts/fixed-lines/$PID-$BID.fixed.lines | cut -f1 -d',' | tail -n+2 > $TMP_DIR/inscope.txt

# Obtain the set of mutants that are killed by the developer-written test suite
cut -f2 -d',' $D4J_HOME/framework/projects/$PID/trigger_tests/$BID.mutants.map.csv | tail -n+2 > $TMP_DIR/killed.txt

# Compute and output the intersection of mutants
echo "ProjectId,RealFaultId,ArtificialFaultId,MutantId"
for id in $(cat $TMP_DIR/inscope.txt); do
    # Obtain artificial fault id and mutant id
    mapping=$(grep ",$id\$" $D4J_HOME/framework/projects/$PID/trigger_tests/$BID.mutants.map.csv)
    grep -q "^$id\$" $TMP_DIR/killed.txt && echo $PID,$BID,$mapping;
done

# Clean up
rm -rf $TMP_DIR
