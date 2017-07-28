#!/usr/bin/env bash
#
################################################################################
# This script takes a generated test suite (generatied for a particular Defects4J
# version) and determines:
# 1) Whether the test suite detects the real bug of the corresponding Defects4J
#    version.
#
# 2) The list of triggering tests
#
# 3) The list of fault-related classes
#
# The script writes the results of 2) and 3) to a provided output directory. It
# also writes a jar archive with all compiled classes of the generated test
# suite to the output directory.
#
# This script writes  the following files:
# <output directory>
#         |
#         |-- <test suite archive>.triggering_tests
#         |
#         |-- <test suite archive>.loaded_classes
#         |
#         |-- <test suite archive>.jar
#
# Note that if the test suite does not detect the real bug, then 2) and 3) are
# skipped and no files are written to the output directory.
#
# Environment variables:
# - D4J_HOME     Needs to be set and must point to the Defects4J installation that
#                is used to checkout, compile, test, etc.
# - D4J_TMP_DIR  Set the temporary directory for this script (optional).
#                The default is: /tmp/get_trigger_rel_classes_<process_id_of_this_script>.
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
export PATH=$D4J_HOME/framework/bin:$PATH

# Set temporary directory used to checkout the project versions
TMP_DIR="${D4J_TMP_DIR:-"/tmp/get_trigger_rel_classes_$$"}"

# Check arguments
[ $# == 2 ] || die "usage: $0 <test suite archive> <output directory>"
TEST_SUITE=$1
OUT_DIR=$2

[ -f $TEST_SUITE ] || die "Test suite archive ($TEST_SUITE) doesn't exist!"
#mkdir -p $OUT_DIR

archive=$(basename "$TEST_SUITE")
pid=$(echo $archive | cut -f1 -d'-')
vid=$(echo $archive | cut -f2 -d'-')
bid=$(echo $vid | tr -d 'f')
tid=$(echo $archive | cut -f2 -d'.')

OUT_DIR=$OUT_DIR/$pid/$bid
mkdir -p $OUT_DIR

work_dir="$TMP_DIR/$pid-$vid"
mkdir -p $TMP_DIR

file_trigger="$OUT_DIR/$archive.triggering_tests"
file_classes="$OUT_DIR/$archive.loaded_classes"
file_jar="$OUT_DIR/$archive.jar"

if [ -e $OUT_DIR/${bid}.killmap-err.txt -a -e $file_trigger -a -e $file_classes -a -e $file_jar ]; then

if $(grep -q "Completed successfully" $OUT_DIR/${bid}.killmap-err.txt); then
  echo "Already finished"
else
  echo "Continuing partial run"
  TMPDIR=$(mktemp -d 2>/dev/null || mktemp -d -t 'killmaptmp')
  CLASSPATH=$file_jar $KILLMAP_HOME/scripts/generate-matrix.sh --partial-output <(zcat $OUT_DIR/${bid}.killmap.gz) --relevant-test-classes $file_classes --triggering-tests $file_trigger $pid ${bid} $TMPDIR $OUT_DIR/${bid}.mutants.log 2> $OUT_DIR/${bid}.killmap-err-1.txt | gzip > $OUT_DIR/${bid}.killmap-1.gz
fi

else

# Checkout buggy version
defects4j checkout -p$pid -v${bid}b -w$work_dir
# Determine number and set of triggering tests
defects4j test -s$TEST_SUITE -w$work_dir
# If the test suite doesn't detect the bug, we are done -> exit
num_trig=$(cat $work_dir/failing_tests | wc -l)
[ $num_trig == 0 ] && exit 0

# Write file for triggering tests
triggers=$(grep "\---" $work_dir/failing_tests | tr -d '-' | tr -d ' ')
for trig in $triggers; do
    echo $trig >> $file_trigger
done
#cp $work_dir/failing_tests $file_trigger

# Checkout fixed version
defects4j checkout -p$pid -v${bid}f -w$work_dir
# Monitor each triggering test and determine set of loaded classes
triggers=$(cat $file_trigger) #$(grep "\---" $file_trigger | tr -d '-' | tr -d ' ')
for trig in $triggers; do
    defects4j monitor.test -t$trig -s$TEST_SUITE -w$work_dir
    cat $work_dir/loaded_classes.test >> $file_classes
done
# Write file for set of loaded classes
sort $file_classes | uniq > $TMP_DIR/.tmp
mv $TMP_DIR/.tmp $file_classes

# Jar all test classes
gen_dir=$(find $work_dir -name "gen-tests")
pushd . > /dev/null
cd $gen_dir && jar cf $file_jar *
popd > /dev/null

TMPDIR=$(mktemp -d 2>/dev/null || mktemp -d -t 'killmaptmp')
CLASSPATH=$file_jar $KILLMAP_HOME/scripts/generate-matrix.sh --relevant-test-classes $file_classes --triggering-tests $file_trigger $pid ${bid} $TMPDIR $OUT_DIR/${bid}.mutants.log 2> $OUT_DIR/${bid}.killmap-err.txt | gzip > $OUT_DIR/${bid}.killmap.gz

fi

rm -rf $work_dir
rm -rf $TMPDIR



