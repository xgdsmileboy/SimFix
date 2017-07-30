#!/usr/bin/env bash
#
################################################################################
# This script categorizes all faults in Defects4J, using the bug-fixing patches
# and the following categories:
# 1) Single-line fault:
#    - the fault fix is a single insertion, deletion, or modification (modification as
#      reported by diffstat, and insertions/deletion of curlies ignored).
#
# 2) Fault of omission
#    - the fault fix only inserts code.
#
# 3) Fault in non-executable code
#    - the fault fix applies only to non-executable code -- that is, to a
#      declaration rather than an executable statement. Only changing the position of a
#      curly also belongs to this category.
#
# The absolute numbers and ratios for all categories are written to STDOUT (as LaTex macros).
#
# The categorization for each fault is logged to "categories.csv", if debugging is enabled (DEBUG=1).
#
# Usage:
# ./categorize_patches.sh
#
# Requirements:
# - Bash 4+ needs to be installed
# - diffstat needs to be installed
# - the environment variable D4J_HOME needs to be set and must point to the Defects4J
#   installation that contains all minimized patches.
#
# TODO: The faults in non-executable code are currently hard-coded.
################################################################################

# If DEBUG is enabled, log categories to LOG file
DEBUG=0
LOG="./categories.csv"
[ $DEBUG == 1 ] && echo "PID,BID,Category" > $LOG

# The set of faults in non-executable code -- determined by manually inspecting the diffs.
declare -A nonexec_faults=(
    ["Lang-23"]=1
    ["Lang-25"]=1
    ["Lang-29"]=1
    ["Lang-30"]=1
    ["Lang-53"]=1
    ["Lang-56"]=1
    ["Math-12"]=1
    ["Math-30"]=1
    ["Math-57"]=1
    ["Math-79"]=1
    ["Mockito-8"]=1
)
# List of faults that involve only non-executable code
LIST_FAULTS_ONLY_NON_EXEC="Lang-23, Lang-25, Lang-29, Lang-30, Lang-53, Lang-56, Math-12, Math-30, Math-57, Math-79, Mockito-8"

# List of faults that involve some non-executable code
NUM_FAULTS_SOME_NON_EXEC=21
LIST_FAULTS_SOME_NON_EXEC="Lang-4, Lang-8, Lang-23, Lang-25, Lang-29, Lang-30, Lang-53, Lang-56, Math-12, Math-30, Math-57, Math-74, Math-79, Math-95, Math-104, Mockito-10, Mockito-16, Mockito-19, Mockito-21, Mockito-25, Mockito-30"

# Counters for all categories and total number of faults
NUM_FAULTS_SINGLE_LINE=0
NUM_FAULTS_OMISSION=0
NUM_FAULTS_ONLY_NON_EXEC=0
NUM_FAULTS=0

#
# Print error message and exit
#
die() {
    echo $1
    exit 1
}

#
# Log fault and category to file
#
log() {
    [ $DEBUG -eq 1 ] && echo $1,$2,$3 >> $LOG
}

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"

# Iterate over all projects in Defects4J
for pid in Chart Closure Lang Math Mockito Time; do
    dir_project="$D4J_HOME/framework/projects/$pid"
    dir_patches="$dir_project/patches"
    # Determine the number of real bugs for this project
    num_bugs=$(egrep "^[0-9][0-9]?[0-9]?," $dir_project/commit-db | wc -l)
    # Iterate over all real bugs for this project
    for bid in $(seq 1 $num_bugs); do
        ((NUM_FAULTS++))
        # Obtain the number of insertions, deletions, and modifications for all patched
        # files, using diffstat.
        num_ins=0
        num_del=0
        num_mod=0

        # diffstat options:
        # -m -> merge results (e.g., multiple hunks) per file.
        # -R -> swap old and new file in patch (i.e., consider buggy -> fixed).
        # -t -> produce csv output.
        #
        # csv output of diffstat:
        # INSERTED,DELETED,MODIFIED,FILENAME
        #
        # get csv output and ignore header of csv output
        diff_csv=$(diffstat -m -R -t "$dir_patches/$bid.src.patch" | grep -v "^INSERTED,")
        for file in $diff_csv; do
            ins=$(echo $file | cut -f1 -d',')
            del=$(echo $file | cut -f2 -d',')
            mod=$(echo $file | cut -f3 -d',')
            ((num_ins+=$ins))
            ((num_del+=$del))
            ((num_mod+=$mod))
        done
        # Count all deletions and additions in diff
        minus=$(grep -E "^-\s+" "$dir_patches/$bid.src.patch" | wc -l)
        plus=$(grep -E "^\+\s+" "$dir_patches/$bid.src.patch" | wc -l)
        curlies=0
        # Count all curlies in deletions and additions
        ((curlies+=$(grep -E "^[-+]\s+}\s*$" "$dir_patches/$bid.src.patch" | wc -l)))
        ((curlies+=$(grep -E "^[-+]\s+}\s*else\s*[{]?\s*$" "$dir_patches/$bid.src.patch" | wc -l)))
        ((curlies+=$(grep -E "^[-+]\s+{\s*$" "$dir_patches/$bid.src.patch" | wc -l)))
        
        # Check for category 1)
        if [ $((num_ins + num_del +num_mod)) -eq 1 ]; then
            log $pid $bid 1
            ((NUM_FAULTS_SINGLE_LINE++))
        elif [ $((plus + minus)) -eq $((curlies + 1)) ]; then
            log $pid $bid 1
            ((NUM_FAULTS_SINGLE_LINE++))
        fi
        # Check for category 2)
        if [ $(($num_del + $num_mod)) -eq 0 ]; then
            log $pid $bid 2
            ((NUM_FAULTS_OMISSION++))
        fi
        # Check for category 3)
        if [ "${nonexec_faults["$pid-$bid"]}" == "1" ]; then
            log $pid $bid 3
            ((NUM_FAULTS_ONLY_NON_EXEC++))
        fi
    done
done

# Print results as LaTex macros
NUM_FAULTS_MULTI_LINE=$((NUM_FAULTS - NUM_FAULTS_SINGLE_LINE))

ratio=$(perl -e "printf(\"%.0f\", ($NUM_FAULTS_SINGLE_LINE / $NUM_FAULTS * 100))")
echo "\\def\\nRealFaultsWithSingleLineBugfix{$NUM_FAULTS_SINGLE_LINE\\xspace}"
echo "\\def\\fractionOfRealFaultsWithSingleLineBugfix{$ratio\\%\\xspace}"

ratio=$(perl -e "printf(\"%.0f\", ($NUM_FAULTS_MULTI_LINE / $NUM_FAULTS * 100))")
echo "\\def\\nRealFaultsWithMultiLineBugfix{$NUM_FAULTS_MULTI_LINE\\xspace}"
echo "\\def\\fractionOfRealFaultsWithMultiLineBugfix{$ratio\\%\\xspace}"

ratio=$(perl -e "printf(\"%.0f\", ($NUM_FAULTS_OMISSION / $NUM_FAULTS * 100))")
echo "\\def\\nRealFaultsWithOmission{$NUM_FAULTS_OMISSION\\xspace}"
echo "\\def\\fractionOfRealFaultsWithOmission{$ratio\\%\\xspace}"

ratio=$(perl -e "printf(\"%.0f\", ($NUM_FAULTS_ONLY_NON_EXEC / $NUM_FAULTS * 100))")
echo "\\def\\faultsWhereAllFaultyLinesAreNonExecutable{$LIST_FAULTS_ONLY_NON_EXEC\\xspace}"
echo "\\def\\nRealFaultsWhereAllFaultyLinesAreNonExecutable{$NUM_FAULTS_ONLY_NON_EXEC\\xspace}"
echo "\\def\\fractionOfRealFaultsWhereAllFaultyLinesAreNonExecutable{$ratio\\%\\xspace}"

ratio=$(perl -e "printf(\"%.0f\", ($NUM_FAULTS_SOME_NON_EXEC / $NUM_FAULTS * 100))")
echo "\\def\\faultsWhereSomeFaultyLinesAreNonExecutable{$LIST_FAULTS_SOME_NON_EXEC\\xspace}"
echo "\\def\\nRealFaultsWhereSomeFaultyLinesAreNonExecutable{$NUM_FAULTS_SOME_NON_EXEC\\xspace}"
echo "\\def\\fractionOfRealFaultsWhereSomeFaultyLinesAreNonExecutable{$ratio\\%\\xspace}"
