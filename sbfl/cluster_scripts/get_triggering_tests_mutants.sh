#!/usr/bin/env bash

PWD=`pwd`

ICEBERG_LIMIT=2000

EXPS_DIR="$PWD/triggering_tests_for_artificial_faults"
rm -rf $EXPS_DIR
SCRIPTS=$EXPS_DIR"/scripts"

#
# Print error message and exit
#
die() {
  echo $1
  exit 1
}

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"

MUTANTS_FILE="$PWD/mutants.csv"
rm -f $MUTANTS_FILE

for pid in Chart Closure Lang Math Mockito Time; do
  MUTANTS_IN_SCOPE="$D4J_HOME/framework/projects/$pid/mutants_in_scope.csv"
  if [ -f $MUTANTS_IN_SCOPE ]; then
    cat $MUTANTS_IN_SCOPE >> $MUTANTS_FILE
  fi
done

PYTHON_SCRIPT="$PWD/get_triggering_tests_mutants.py"
python $PYTHON_SCRIPT $EXPS_DIR $MUTANTS_FILE $ICEBERG_LIMIT

pushd . > /dev/null 2>&1
cd $SCRIPTS

# submitting jobs
for s in $(find . -type f -name "*.sh" -printf '%f\n' | sort -n -t _ -k 3); do
  #echo $s
  qsub $s
done

popd > /dev/null 2>&1
echo "All jobs submitted!"

# EOF

