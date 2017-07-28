#!/usr/bin/env bash
#
# --------------------------------------------------------------------
# This script collects the time to run GZoltar on all D4J's faults.
# The runtime (in seconds) of each fault is written to the file:
#   <script directory>/../data/sbfl_runtimes.csv
#
# Usage:
# bash collect_sbfl_runtimes.sh <directory with GZoltar files>
#
# Environment variables:
# - D4J_HOME      Needs to be set and must point to the Defects4J
#                 installation.
#
# --------------------------------------------------------------------

HERE=$(cd `dirname $0` && pwd)

##
# Print error message and exit
##
die() {
  echo $1
  exit 1
}

##
# Convert `date` to epoch
#
# (based on https://stackoverflow.com/a/7241238/998816)
##
_convert_to_epoch() {
  local USAGE="Usage: _convert_to_epoch <output of \`date\`> <date format>"
  if [ "$#" != 2 ]; then
    echo "$USAGE" >&2
    return 1
  fi

  python - "$1" "$2" << END
import sys
import time
print(int(time.mktime(time.strptime(sys.argv[1], sys.argv[2]))))
END

  return "$?"
}

##
# Subtract two dates, i.e., <date 1> - <date 2>
##
_subtract_dates() {
  local USAGE="Usage: _subtract_dates <date 1> <date 2> <date format>"
  if [ "$#" != 3 ]; then
    echo "$USAGE" >&2
    return 1
  fi

  local a=$(_convert_to_epoch "$1" "$3")
  local b=$(_convert_to_epoch "$2" "$3")

  echo "$a - $b" | bc || return 1

  return 0
}

##
# Convert a string %H:%M:%S into seconds
##
_convert_to_seconds() {
  local USAGE="Usage: _convert_to_seconds <\`date\`, format %H:%M:%S>"
  if [ "$#" != 1 ]; then
    echo "$USAGE" >&2
    return 1
  fi

  local h=$(echo "$1" | cut -f1 -d':')
  local m=$(echo "$1" | cut -f2 -d':')
  local s=$(echo "$1" | cut -f3 -d':')

  echo "$h"*3600 + "$m"*60 + "$s" | bc || return 1

  return 0    
}

# -------------------------------------------------------- Envs & Args

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"

[ $# -eq 1 ] || die "collect_sbfl_runtimes.sh <directory with GZoltar files>";

GZ_DATA_DIR="$1"
if [ ! -d "$GZ_DATA_DIR" ]; then
  die "[ERROR] '$GZ_DATA_DIR' does not exist!"
fi

# ------------------------------------------------- Constant Variables

DATE_DEFAULT_FORMAT="%a %b %d %H:%M:%S %Z %Y"
HOUR_MIN_SEC_FORMAT="%H:%M:%S"

BLACKLIST="$HERE/../data/blacklist.csv"

TMP_DIR="/tmp/collect_runtimes_$$"
rm -rf "$TMP_DIR"; mkdir "$TMP_DIR"

RUNTIME_FILE="$HERE/../data/sbfl_runtimes.csv"
echo "project,fault,runtime" > "$RUNTIME_FILE"

# --------------------------------------------------------------- Main

for pid in Chart Closure Lang Math Mockito Time; do
  project_dir="$D4J_HOME/framework/projects/$pid"

  # Get list of real and artificial faults
  bids=$(cut -f1 -d',' $project_dir/commit-db)

  # Iterate over all bugs (real and artificial) for this project
  for bid in $bids; do
    if grep -q "^$pid,.*,$bid," "$BLACKLIST"; then
      continue;
    fi

    if [ "$bid" -gt "1000" ]; then
      MUTANTS_IN_SCOPE="$D4J_HOME/framework/projects/$pid/mutants_in_scope.csv"
      if ! grep -q "^$pid,.*,$bid$" "$MUTANTS_IN_SCOPE"; then
        continue; # not in scope
      fi
    fi

    gzoltar_file="$GZ_DATA_DIR/$pid-$bid-gzoltar-files.tar.gz"
    if [ ! -s "$gzoltar_file" ]; then
      echo "[ERROR] There is not any '$gzoltar_file' file!"
      exit 1;
    fi

    echo "* $pid-$bid"

    #tar -zxf "$GZ_DATA_DIR/$pid/$pid-$bid-gzoltar-files.tar.gz" -C "$TMP_DIR/" # FIXME uncomment me
    tar -zxf "$GZ_DATA_DIR/$pid-$bid-gzoltar-files.tar.gz" -C "$TMP_DIR/" # FIXME remove me
    if [ $? -ne "0" ]; then
      die "[ERROR] Extraction of '$GZ_DATA_DIR/$pid/$pid-$bid-gzoltar-files.tar.gz' to '$TMP_DIR/' has failed!"
    fi

    if grep --text -q "^\[INFO\] It tooks " "$TMP_DIR/gzoltars/$pid/$bid/log.txt"; then
      runtime=$(grep --text "^\[INFO\] It tooks " "$TMP_DIR/gzoltars/$pid/$bid/log.txt" | cut -f4 -d ' ')
      runtime=$(_convert_to_seconds "$runtime")
    elif grep --text -q "^\[INFO\] Start: " "$TMP_DIR/gzoltars/$pid/$bid/log.txt"; then
      # e.g., [INFO] Start: Thu Jul 20 23:31:44 WEST 2015
      start=$(grep --text "^\[INFO\] Start: " "$TMP_DIR/gzoltars/$pid/$bid/log.txt" | awk -F' ' '{printf "%s,%s,%s,%s,%s,%s\n", $3, $4, $5, $6, $7, $8;}')
      end=$(grep --text "^\[INFO\] End: " "$TMP_DIR/gzoltars/$pid/$bid/log.txt" | awk -F' ' '{printf "%s,%s,%s,%s,%s,%s\n", $3, $4, $5, $6, $7, $8;}')
      runtime=$(_subtract_dates "$end" "$start" "$DATE_DEFAULT_FORMAT")
    fi

    echo "$pid,$bid,$runtime" >> "$RUNTIME_FILE"

    rm -rf "$TMP_DIR/gzoltars/$pid/$bid"
  done
done

rm -rf "$TMP_DIR" # clean up

echo ""; echo "DONE!"
exit 0;

# EOF

