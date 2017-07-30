#!/bin/bash
#
# --------------------------------------------------------------------
# This script creates one job per D4J fault. Each job contains as many
# executions of killmap program as the number of test cases of each
# fault.
#
# Usage:
# extend_incomplete_killmaps.sh <UN-RUN FILE> <DATA DIR>
#                               <MAX HOURS PER JOB>
#
# Parameters:
# <UN-RUN FILE>       File with list of test cases than need to be
#                     re-executed. Each line should follow the
#                     following structure:
#                       project_name,bug_id,test_name,mutants    
# <DATA DIR>          Directory to which the killmaps will be generated.
#                     The script will create the following structure:
#                       DATA DIR/killmaps/project_name/bug_id/test_name/
# <MAX HOURS PER JOB> Timeout (hours) to run each job
#
# Environment variables:
#   D4J_HOME          Needs to be set and must point to the Defects4J
#                     installation that is used to checkout each fault.
#   KILLMAP_HOME      Needs to be set and must point to the Killmap
#                     directory.
# --------------------------------------------------------------------

##
# Print error message to the stdout and exit.
##
die() {
  echo "$@" >&2
  exit 1
}

# -------------------------------------------------------- Envs & Args

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"
export DEFECTS4J_HOME="$D4J_HOME"

# Check whether KILLMAP_HOME is set
[ "$KILLMAP_HOME" != "" ] || die "KILLMAP_HOME is not set!"
source "$KILLMAP_HOME/scripts/utils.sh" || die "Source of 'util.sh' failed";

USAGE="Usage: $0 <un-run file> <data dir> <max hours per job>"
[ $# -eq 3 ] || die "$USAGE";

UNRUN_FILE="$1"
if [ ! -s "$UNRUN_FILE" ]; then
  die "'$UNRUN_FILE' file does not exist or is empty!"
fi

DATA_DIR="$2"
mkdir -p "$DATA_DIR"
MAX_HOURS_PER_JOB="$3"

SCRIPTS_DIR="/tmp/extend_incomplete_killmaps-scripts_$$"
rm -rf "$SCRIPTS_DIR"; mkdir -p "$SCRIPTS_DIR"

host_name=$(hostname)

# --------------------------------------------------------------- Main

_iceberg_header() {
  echo "#$ -l h_rt=$MAX_HOURS_PER_JOB:00:00"
  echo "#$ -l mem=8G"
  echo "#$ -l rmem=6G"
  echo "#$ -e /dev/null"
  echo "#$ -o /dev/null"
  echo "module load apps/java/1.7.0u55"
  echo "export MALLOC_ARENA_MAX=1"

  return 0
}

tail -n +2 "$UNRUN_FILE" | awk -F',' '{printf "%s,%s\n", $1, $2;}' | sort -u | while read -r item; do
  pid=$(echo "$item" | cut -f1 -d',')
  bid=$(echo "$item" | cut -f2 -d',')

  pid_bid_dir="/tmp/extend_incomplete_killmaps-projects/$pid/$bid"

  ##
  # First need to checkout the pid-$bid, if it was not already
  if [ ! -d ""$pid_bid_dir"" ]; then
    mkdir -p "$pid_bid_dir"

    d4j-checkout-and-prepare "$pid" "$bid" "$pid_bid_dir";
    if [ $? -ne 0 ]; then
      echo "'d4j-checkout-and-prepare' failed for $item!"
    fi
  fi

  ##
  # Create a job for it

  echo "#!/bin/bash" > "$SCRIPTS_DIR/$pid-$bid.sh"

  if [[ $host_name == "iceberg-"* ]]; then
    _iceberg_header >> "$SCRIPTS_DIR/$pid-$bid.sh"
  else
    # local machine
    echo "" >> "$SCRIPTS_DIR/$pid-$bid.sh"
  fi

  echo "export _JAVA_OPTIONS=\"-XX:MaxHeapSize=1024m -Xmx4096m\"" >> "$SCRIPTS_DIR/$pid-$bid.sh"
  echo "export D4J_HOME=\"$D4J_HOME\"" >> "$SCRIPTS_DIR/$pid-$bid.sh"
  echo "export DEFECTS4J_HOME=\"$D4J_HOME\"" >> "$SCRIPTS_DIR/$pid-$bid.sh"
  echo "export KILLMAP_HOME=\"$KILLMAP_HOME\"" >> "$SCRIPTS_DIR/$pid-$bid.sh"
  echo "" >> "$SCRIPTS_DIR/$pid-$bid.sh"

  grep "^$item," "$UNRUN_FILE" | while read -r data; do
    test_name=$(echo "$data" | cut -f3 -d',')
    mutants=$(echo "$data" | cut -f4 -d',' | tr ' ' ',')

    out_dir="$DATA_DIR/killmaps/$pid/$bid/$test_name"
    killmap_csv="$out_dir/killmap.csv"
    log_file="$out_dir/log.txt"
    mutants_log="$out_dir/mutants.log"

    echo "" >> "$SCRIPTS_DIR/$pid-$bid.sh"
    echo "rm -rf \"$out_dir\"; mkdir -p \"$out_dir\"" >> "$SCRIPTS_DIR/$pid-$bid.sh"

    echo "pushd . > /dev/null 2>&1" >> "$SCRIPTS_DIR/$pid-$bid.sh"
    echo "cd \"$KILLMAP_HOME/scripts/\"" >> "$SCRIPTS_DIR/$pid-$bid.sh"
    echo "  bash \"generate-matrix.sh\" --no-preparation \"$pid\" \"$bid\" \"$pid_bid_dir\" \"$mutants_log\" --only-test-to-run \"$test_name\" --mutants-to-run \"$mutants\" 2>$log_file > \"$killmap_csv\"" >> "$SCRIPTS_DIR/$pid-$bid.sh"

    echo "  if ! grep -q --text \"^Completed successfully\!$\" \"$log_file\"; then" >> "$SCRIPTS_DIR/$pid-$bid.sh"
    echo "    echo \"[ERROR] There is not any 'successfully' message!\" >> \"$log_file\"" >> "$SCRIPTS_DIR/$pid-$bid.sh"
    echo "  fi" >> "$SCRIPTS_DIR/$pid-$bid.sh"
    echo "popd > /dev/null 2>&1" >> "$SCRIPTS_DIR/$pid-$bid.sh"
  done

  if [[ $host_name == "iceberg-"* ]]; then
    qsub -N "_$bid-$pid" "$SCRIPTS_DIR/$pid-$bid.sh"
  else
    # local machine
    bash "$SCRIPTS_DIR/$pid-$bid.sh"
  fi
done

echo "All jobs submitted!"

# EOF

