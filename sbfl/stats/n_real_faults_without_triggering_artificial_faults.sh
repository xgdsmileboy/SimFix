#!/bin/bash

N_WITH=0
N_WITHOUT=0
for P in Lang Math Chart Time Closure; do
  for B in {1..133}; do
    [ -f "$D4J_HOME/framework/projects/$P/trigger_tests/$B" ] || continue
    ANY_ARTS=no
    for ART in "$D4J_HOME/framework/projects/$P/trigger_tests/$B"?????; do
      [ -f "$ART" ] || continue
      [ "$(wc -l < $ART)" -gt 1 ] && ANY_ARTS=yes && break
    done
    [ $ANY_ARTS = yes ] && ((N_WITH++))
    [ $ANY_ARTS = no ] && ((N_WITHOUT++))
  done
done

echo '\def\nRealFaultsWithTriggeringArtificialFaults{'"$N_WITH"'\xspace}'
echo '\def\nRealFaultsWithoutTriggeringArtificialFaults{'"$N_WITHOUT"'\xspace}'
