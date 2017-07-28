#!/bin/bash

usage() {
  die "Usage: $0 <pid> <vid> <project dir>"
}

die(){
	echo "$@" >&2
	exit 1
}

# Check arguments and set PID and VID range
[ $# -eq 3 ] || usage
PID=$1
VID=$2
PROJ_DIR=$3
SPECTRA_RESULT="$(pwd)/spectra/$PID/$VID"
LOCATE_RESULT="$(pwd)/ochiai/$PID/$VID"
GZOLTAR_JAR="$(pwd)/lib/com.gzoltar-1.6.0-jar-with-dependencies.jar"

rm -rf $SPECTRA_RESULT $LOCATE_RESULT

COMP_SPECTRA="$(pwd)/gzoltar/run_gzoltar.sh $PID $VID $PROJ_DIR $SPECTRA_RESULT $GZOLTAR_JAR"
$COMP_SPECTRA

LOCATE="$(pwd)/analysis/pipeline-scripts/previously-studied-flts/sbfl.sh $PID $VID $(pwd)"
$LOCATE