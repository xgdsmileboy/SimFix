#!/bin/bash
#$ -l h_rt=8:00:00
#$ -l mem=10G
#$ -l rmem=9G
#$ -e /dev/null
#$ -o /dev/null
if module > /dev/null 2>&1; then
  module load apps/java/1.7.0u55
fi
export MALLOC_ARENA_MAX=1
export TZ='America/Los_Angeles'
export _JAVA_OPTIONS="-XX:MaxHeapSize=1024m -Xmx4096m"

D4J_HOME=$DEFECTS4J_HOME

# --------------------------------------------------------------------
# 
# This script runs GZoltar on a specified D4J project/bug using either
# manually written test cases or automatically generated.
# 
# Usage:
# ./run_gzoltar.sh <pid> <vid> <data dir> <tool> [tests dir]
# 
# Parameters:
# - <pid>         Defects4J's project ID: Chart, Closure, Lang, Math, Mockito or Time.
# - <vid>         Version ID
# - <data dir>    Directory to where data will be written by GZoltar
# - <tool>        Either 'developer', 'evosuite' or 'randoop'
# - [tests dir]   (mandatory if <tool=evosuite | tool=randoop>) Directory containing the generated test cases
# 
# Examples:
# 
# - Executing GZoltar on bug Chart-5 and using manually written tests
#   $ ./run_gzoltar.sh Chart 5 /home/user/developer/Chart/5 developer
# 
# Environment variables:
# - D4J_HOME      Needs to be set and must point to the Defects4J
#                 installation.
# - GZOLTAR_JAR   GZoltar jar file
# - EVOSUITE_JAR  EvoSuite jar file if <tool=evosuite>
# 
# --------------------------------------------------------------------

##
# Print error message and exit
##
die() {
  echo "$@" >&2
  if [ ! -z $LOG_FILE ]; then
    echo "$@" >> $LOG_FILE 2>&1
  fi
  # if [ -d $TMP_DIR ]; then
  #   rm -rf $TMP_DIR # do not leave anything behind
  # fi
  exit 1
}

##
# Print usage message and exit
##
usage() {
  die "Usage: $0 <pid> <vid> <project dir> <data dir> <GZOLTAR_JAR>"
}

# Check arguments and set PID and VID range
[ $# -eq 5 ] || usage
PID=$1
VID=$2
TMP_DIR=$3
DATA_DIR=$4
GZOLTAR_JAR=$5
TOOL="developer"
TESTS_DIR=""

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"
# Check whether GZOLTAR_JAR is set
[ "$GZOLTAR_JAR" != "" ] || die "GZOLTAR_JAR is not set!"
if [ "$TOOL" == "evosuite" ]; then
  # Check whether EVOSUITE_JAR is set
  [ "$EVOSUITE_JAR" != "" ] || die "EVOSUITE_JAR is not set!"
fi

# Set temporary directory used to checkout the project versions
# TMP_DIR="/tmp/$USER/gzoltar_"$$"_$PID-$VID"

# Clean and create the temporary directory, if necessary
# rm -rf $TMP_DIR
# mkdir -p $TMP_DIR
if [ "$TOOL" == "developer" ]; then
  rm -rf $DATA_DIR
fi

mkdir -p $DATA_DIR
rm -f "$DATA_DIR/matrix" "$DATA_DIR/spectra" "$DATA_DIR/statistics.csv"

# Set the full path to the data directory to make sure that the forked GZoltar
# process uses the correct one.
DATA_DIR=$(cd $DATA_DIR && pwd)

# --------------------------------------------------------------------
# Prepare Project under test

# Defects4J directories for given project id (PID)
DIR_PROJECT="$D4J_HOME/framework/projects/${PID^}"
DIR_LOADED_CLASSES="$DIR_PROJECT/loaded_classes"
DIR_RELEVANT_TESTS="$DIR_PROJECT/relevant_tests"
DIR_TRIGGER_TESTS="$DIR_PROJECT/trigger_tests"

LOG_FILE="$DATA_DIR/log.txt"

echo "[DEBUG] PID: $PID" > $LOG_FILE 2>&1
echo "[DEBUG] VID: $VID" >> $LOG_FILE 2>&1
echo "[DEBUG] DATA_DIR: $DATA_DIR" >> $LOG_FILE 2>&1
echo "[DEBUG] TOOL: $TOOL" >> $LOG_FILE 2>&1
echo "[DEBUG] TESTS_DIR: $TESTS_DIR" >> $LOG_FILE 2>&1
echo "[DEBUG] TMP_DIR: $TMP_DIR" >> $LOG_FILE 2>&1
echo "[DEBUG] DIR_PROJECT: $DIR_PROJECT" >> $LOG_FILE 2>&1
echo "[DEBUG] DIR_LOADED_CLASSES: $DIR_LOADED_CLASSES" >> $LOG_FILE 2>&1
echo "[DEBUG] DIR_RELEVANT_TESTS: $DIR_RELEVANT_TESTS" >> $LOG_FILE 2>&1
echo "[DEBUG] DIR_TRIGGER_TESTS: $DIR_TRIGGER_TESTS" >> $LOG_FILE 2>&1
echo "[DEBUG] JAVA_VERSION:" >> $LOG_FILE 2>&1
java -version >> $LOG_FILE 2>&1

# Obtain buggy project version
# echo "[INFO] Checking out $PID-$VID to $TMP_DIR" >> $LOG_FILE 2>&1
# "$D4J_HOME/framework/bin/defects4j" checkout -p $PID -v ${VID}b -w $TMP_DIR >> $LOG_FILE 2>&1 || die "Checkout failed!"

pushd . > /dev/null 2>&1
cd $TMP_DIR

  # Collect loaded classes and convert new line "\n" to ":"
  LOADED_CLASSES=$(cat "$DIR_LOADED_CLASSES/$VID.src" | tr '\n' ':')
  num_loaded_classes=$(echo "$LOADED_CLASSES" | wc -l)
  if [ "$num_loaded_classes" -eq 0 ]; then
    die "WTF! Number of loaded classes is zero!"
  fi
  echo "[DEBUG] LOADED_CLASSES: $LOADED_CLASSES" >> $LOG_FILE 2>&1

  LOADED_SUB_CLASSES=$(cat "$DIR_LOADED_CLASSES/$VID.src" | sed 's/$/\$*/' | tr '\n' ':')
  echo "[DEBUG] LOADED_SUB_CLASSES: $LOADED_SUB_CLASSES" >> $LOG_FILE 2>&1

  # Collect relevant tests (i.e., those that touch at least one of the modified sources)
  if [ "$TOOL" == "evosuite" ]; then
    RELEVANT_TESTS="*_ESTest" # EvoSuite test cases
  elif [ "$TOOL" == "randoop" ]; then
    RELEVANT_TESTS="RegressionTest*"
  else
    RELEVANT_TESTS=$(cat "$DIR_RELEVANT_TESTS/$VID" | tr '\n' ':')
    NUM_RELEVANT_TESTS=$(wc -l "$DIR_RELEVANT_TESTS/$VID" | cut -f1 -d' ')
  fi
  echo "[DEBUG] RELEVANT_TESTS: $RELEVANT_TESTS" >> $LOG_FILE 2>&1

  # Trigger test cases file
  TRIGGER_TESTS_FILE="$DIR_TRIGGER_TESTS/$VID"
  echo "[DEBUG] TRIGGER_TESTS_FILE: $TRIGGER_TESTS_FILE" >> $LOG_FILE 2>&1

  # Get project classpah (classes directory, test-classes directory, and jar files)
  if [ "$TOOL" == "developer" ]; then
    D4J_CP=$("$D4J_HOME/framework/bin/defects4j" export -p cp.test)
    D4J_CP="$D4J_HOME/framework/projects/lib/junit-4.11.jar:$D4J_CP"
  else
    D4J_CP=$("$D4J_HOME/framework/bin/defects4j" export -p cp.compile) # get just the classpath to compile
    if [ "$TOOL" == "evosuite" ]; then
      D4J_CP="$D4J_CP:$EVOSUITE_JAR"
    elif [ "$TOOL" == "randoop" ]; then
      D4J_CP="$D4J_CP:$D4J_HOME/framework/projects/lib/junit-4.11.jar"
    fi
  fi
  CP="$D4J_CP"
  echo "[DEBUG] CP: $CP" >> $LOG_FILE 2>&1

  # Compile project under test
  echo "[INFO] Compiling $PID-$VID" >> "$LOG_FILE"
  "$D4J_HOME/framework/bin/defects4j" compile >> "$LOG_FILE" 2>&1 || die "Compilation of the project under test failed!"

popd > /dev/null 2>&1

# --------------------------------------------------------------------
# Compile generated test cases

if [ "$TOOL" != "developer" ]; then
  echo "[INFO] Compiling generated test cases" >> $LOG_FILE 2>&1

  pushd . > /dev/null 2>&1 
  cd $TESTS_DIR || die "There is not a directory with generated test cases!"

  find . -type f -name "*.java" > java.files
  NUM_RELEVANT_TESTS=$(wc -l java.files | cut -f1 -d' ')
  echo "* $NUM_RELEVANT_TESTS generated test suites by $TOOL to be compiled" >> "$LOG_FILE"
  if [ "$NUM_RELEVANT_TESTS" -eq 0 ]; then
    rm -f java.files
    die "There is not any .java file!"
  fi

  #javac -cp .:$CP @java.files >> "$LOG_FILE" 2>&1 || die "Compilation of the generated test cases failed!"
  for java_file in $(cat java.files); do
    echo "* Compiling $java_file" >> "$LOG_FILE"
    javac -cp .:$CP $java_file >> "$LOG_FILE" 2>&1
    if [ $? -ne 0 ]; then
      rm -f java.files
      die "Compilation of the generated test cases failed!"
    fi
  done

  rm -f java.files
  echo "[INFO] OK!" >> $LOG_FILE 2>&1
else
  pushd . > /dev/null 2>&1 
  cd $TMP_DIR
fi

# --------------------------------------------------------------------
# Run GZoltar

# reset options
unset _JAVA_OPTIONS

echo "[INFO] Running GZoltar" >> "$LOG_FILE"
#START=$(date +%s)
echo "[INFO] Start: $(date)" >> $LOG_FILE
java -Xmx4096M -jar $GZOLTAR_JAR \
  -Dproject_cp=$CP \
  -Dgzoltar_data_dir=$DATA_DIR \
  -Dtargetclasses=$LOADED_CLASSES \
  -Dtestclasses="$RELEVANT_TESTS" \
  -DprojectID="$PID-$VID" \
  -DconfigurationID="$PID-$VID-$TOOL" \
  -Dverbose_spectra=false \
  -Dtimelimit=25200 \
  -Dtest_case_timeout=-1 \
  -Dmax_client_memory=4096 \
  -Dmax_perm_size=1024 \
  -Dinclude_suspiciousness_value=false \
  -Dstatistics_backend=NONE \
  -diagnose >> "$LOG_FILE" 2>&1
#END=$(date +%s)
#IN_SECONDS=$(echo "$END - $START" | bc)
#SECONDS_IN_HUMAN_FORMAT=$(date -d@$IN_SECONDS -u +%H:%M:%S)
#echo "[INFO] It tooks $SECONDS_IN_HUMAN_FORMAT to run GZoltar!" >> "$LOG_FILE" 2>&1
echo "[INFO] End: $(date)" >> $LOG_FILE

popd > /dev/null 2>&1

##
# ------------------------------------------------------ sanity checks

## Matrix file check ##

matrix_file="$DATA_DIR/matrix"
if [ ! -f "$matrix_file" ]; then
  die "There is not a $matrix_file file!"
fi
if [ ! -s "$matrix_file" ]; then
  die "$matrix_file is empty!"
fi
echo "[DEBUG] MATRIX OK" >> $LOG_FILE 2>&1

## Spectra file check ##

spectra_file="$DATA_DIR/spectra"
if [ ! -f "$spectra_file" ]; then
  die "There is not any '$spectra_file' file!"  
fi
if [ ! -s "$spectra_file" ]; then
  die "$spectra_file is empty!"
fi
echo "[DEBUG] SPECTRA OK" >> $LOG_FILE 2>&1

# latest version of GZoltar (i.e., 1.6.0) creates a 'spectra' file
# with a header. we should get rid of it. otherwise it might brake
# any step of our pipeline
tail -n +2 "$spectra_file" > "$spectra_file.tmp"
mv -f "$spectra_file.tmp" "$spectra_file" >> $LOG_FILE 2>&1

export _JAVA_OPTIONS="-XX:MaxHeapSize=1024m -Xmx4096m" # to run things with D4J, Ant, etc
##
# Does the number of failing test cases reported by GZoltar is
# equal to the number of failing test cases reported by D4J

# num_failing_tests_by_GZ=$(grep " -$" "$matrix_file" | wc -l)
# num_failing_tests_by_D4J=$(grep --text "^\--- " $TRIGGER_TESTS_FILE | wc -l)

# if [ "$num_failing_tests_by_GZ" -lt "$num_failing_tests_by_D4J" ]; then
#   die "Number of failing test cases reported by GZoltar ($num_failing_tests_by_GZ) is less than the number of failing test cases reported by D4J ($num_failing_tests_by_D4J)"
# elif [ "$num_failing_tests_by_GZ" -gt "$num_failing_tests_by_D4J" ]; then
#   echo "[ERROR] Number of failing test cases reported by GZoltar ($num_failing_tests_by_GZ) is greater than the number of failing test cases reported by D4J ($num_failing_tests_by_D4J)!" >> $LOG_FILE 2>&1
# fi

# rm -rf $TMP_DIR >> $LOG_FILE 2>&1 # do not leave anything behind

echo "" >> $LOG_FILE 2>&1
echo "DONE!" >> $LOG_FILE 2>&1

## compress data

# pushd . > /dev/null 2>&1
# cd "$DATA_DIR/../../../"
#   tar -czf "$PID-$VID-$TOOL-gzoltar-files.tar.gz" "gzoltars/$PID/$VID/"
# popd > /dev/null 2>&1

# EOF

