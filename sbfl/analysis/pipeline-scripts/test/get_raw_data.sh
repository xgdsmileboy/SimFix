#!/bin/sh
#
# Download the raw data (i.e., matrices and mutants log) for a particular bug.
#
[ $# -ne 2 ] && echo "usage: $0 <PID> <BID>" && exit 1
PID=$1
BID=$2

wget -O $PID-$BID.cov-matrix.gz http://evosuite.org/files/fl/gzoltar_files/developer/$PID/$BID/matrix.gz
wget -O $PID-$BID.cov-spectra.gz http://evosuite.org/files/fl/gzoltar_files/developer/$PID/$BID/spectra.gz

wget -O $PID-$BID.killmap.gz http://evosuite.org/files/fl/mutation_files/developer/$PID/$BID/$BID.killmap.gz
wget -O $PID-$BID.mutants.log http://evosuite.org/files/fl/mutation_files/developer/$PID/$BID/mutants.log
