#!/bin/bash
#
# Usage: ./list_all_artificial_faults.sh
#
# Lists the files in the Defects4J directory that correspond to the
# artificial faults detailed by get_killable_mutants_in_scope
# for all Defects4J bugs.
#
# The script prints all file names to STDOUT and the list of all artificial
# faults (in scope of the real fault) to STDERR.
#
# Example:
# ./list_all_artificial_faults.sh > all_files.txt 2> artificial_faults_in_scope.csv

real-bug-ids() {
  case $1 in
     Chart) echo {1..26};;
     Closure) echo {1..133};;
     Lang) echo {1..65};;
     Math) echo {1..106};;
     Time) echo {1..27};;
     Mockito) echo {1..38};;
  esac
}

shopt -s globstar

for PID in Chart Closure Lang Math Time Mockito; do
  for RBID in $(real-bug-ids "$PID"); do
    ABIDS=$(./get_killable_mutants_in_scope.sh "$PID" "$RBID" | tail -n +2 | cut -d , -f 3)
    for ABID in $ABIDS; do
      echo framework/projects/$PID/modified_classes/$ABID.src
      echo framework/projects/$PID/patches/$ABID.src.patch
      echo framework/projects/$PID/trigger_tests/$ABID

      echo "$PID,$RBID,$ABID" >&2
    done
  done
done
