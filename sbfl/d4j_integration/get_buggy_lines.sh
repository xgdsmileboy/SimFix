#!/usr/bin/env bash
#
################################################################################
# This script determines all buggy source code lines in a buggy Defects4J project
# version. It writes the result to a file in the provided output directory; the
# file name is: <project_id>-<bug_id>.buggy.lines
#
# Considering removed and added lines (from buggy to fixed), the script works as
# follows:
#
# 1) For each removed line, the script outputs the line number of the removed
#    line.
#
# 2) For each block of added lines, the script distinguishes two cases:
#    2.1) If the block of added lines is immediately preceded by a removed line,
#         the block is associated with that preceding line -- the script doesn't
#         output a line number in this case.
#    2.2) If the block of added lines is not immediately preceded by a removed
#         line, the script outputs the line number of the line immediately
#         following the block of added lines.
#
# Usage:
# get_buggy_lines.sh <project_id> <bug_id> <out_dir>"
#
# Examples:
#
# Case 1) -- output: line 2
# buggy        fixed
# 1            1
# 2            
#
# Case 2.1) -- output: line 2
# buggy        fixed
# 1            1
# 2            20
#
# Case 2.1) -- output: line 2
# buggy        fixed
# 1            1
# 2            20
#              21
#              22
#
# Case 2.2) -- output: line 2
# buggy        fixed
# 1            1
#              10
#              11
# 2            2
#
#
# Requirements:
# - Bash 4+ needs to be installed
# - diff needs to be installed
# - the environment variable D4J_HOME needs to be set and must point to the
#   Defects4J installation that contains all minimized patches.
# - the environment variable SLOC_HOME needs to be set and must point to the
#   sloccount installation.
#
################################################################################

#
# Print error message and exit
#
die() {
    echo $1
    exit 1
}

# Check command-line arguments
[ $# == 3 ] || die "usage: $0 <project_id> <bug_id> <out_dir>"
PID=$1
BID=$2
OUT_DIR=$3

mkdir -p $OUT_DIR
OUT_FILE="$OUT_DIR/$PID-$BID.buggy.lines"

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"

# Check whether SLOC_HOME is set
[ "$SLOC_HOME" != "" ] || die "SLOC_HOME is not set!"

# Put the defects4j command on the PATH
PATH=$PATH:$D4J_HOME/framework/bin:$SLOC_HOME

# Temporary directory, used to checkout the buggy and fixed version
TMP="/tmp/get_buggy_lines_$$"
mkdir -p $TMP
# Temporary file, used to collect information about all removed and added lines
TMP_LINES="$TMP/all_buggy_lines"


#
# Determine all buggy lines, using the diff between the buggy and fixed version
#
# Checkout the fixed project version
work_dir="$TMP/$PID-$BID"
defects4j checkout -p$PID -v${BID}f -w$work_dir
# Determine and iterate over all modified classes (i.e., patched files)
src_dir=$(grep "d4j.dir.src.classes=" $work_dir/defects4j.build.properties | cut -f2 -d'=')
mod_classes=$(cat $D4J_HOME/framework/projects/$PID/modified_classes/$BID.src)
for class in $mod_classes; do
    file="$(echo $class | tr '.' '/').java";

    # Checkout the fixed project version
    defects4j checkout -p$PID -v${BID}f -w$work_dir
    cp $work_dir/$src_dir/$file "$TMP/fixed"

    # Checkout the buggy project version
    defects4j checkout -p$PID -v${BID}b -w$work_dir
    cp $work_dir/$src_dir/$file "$TMP/buggy"

    # Diff between buggy and fixed -- only show line numbers for removed and
    # added lines in the buggy version
    diff \
        --unchanged-line-format='' \
        --old-line-format="$file#%dn#%l%c'\12'" \
        --new-group-format="$file#%df#FAULT_OF_OMISSION%c'\12'" \
        "$TMP/buggy" "$TMP/fixed" >> "$TMP_LINES"
done
# Print all removed lines to output file
grep --text -v "FAULT_OF_OMISSION" "$TMP_LINES" > "$OUT_FILE"

# Check which added lines need to be added to the output file
for entry in $(grep --text 'FAULT_OF_OMISSION' "$TMP_LINES"); do
    # Determine whether file#line already exists in output file -> if so, skip
    line=$(echo $entry | cut -f1,2 -d'#')
    grep -q "$line" "$OUT_FILE" || echo "$entry" >> "$OUT_FILE"
done


#
# Compute total sloc for all bug-related classes on the buggy version
#
defects4j checkout -p$PID -v${BID}b -w$work_dir
# Set of all bug-related classes
rel_classes=$(cat $D4J_HOME/framework/projects/$PID/loaded_classes/$BID.src)
# Temporary directory that holds all bug-related classes -- used to compute the
# overall number of lines of code
DIR_SRC="$TMP/loc"
mkdir -p $DIR_SRC
[ -f $OUT_DIR/sloc.csv ] || echo "project_id,bug_id,sloc,sloc_total" > $OUT_DIR/sloc.csv
CNT=1
for class in $rel_classes; do
    src_file="$(echo $class | tr '.' '/').java";
    to_file="$(echo $src_file | tr '/' '-')";

    # Checkout the buggy project version
    [ -f  $work_dir/$src_dir/$src_file ] && cp $work_dir/$src_dir/$src_file "$DIR_SRC/$to_file"
    (( CNT += 1 ))
done
# Run sloccount and report total sloc
sloc=$(sloccount $DIR_SRC | grep "java=\d*" | cut -f1 -d' ')
sloc_total=$(sloccount $work_dir/$src_dir | grep "java=\d*" | cut -f1 -d' ')
echo "$PID,$BID,$sloc,$sloc_total" >> $OUT_DIR/sloc.csv

rm -rf $TMP
