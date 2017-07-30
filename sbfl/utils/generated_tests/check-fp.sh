#!/usr/bin/env bash

#
# Very simple script to support manual analysis of false positives.  The script
# extracts all root causes from the logs of the triggering tests (evosuite and
# randoop), adds the root cause of the developer-written test suite (for
# comparison), and writes the unique lines to a log file.
#
# The scripts expects:
# - For evosuite and randoop, an archive with all triggering tests:
#     * evosuite-triggers.tar.gz
#     * randoop-triggers.tar.gz
# - D4J_HOME must be exported and point to the root directory of a Defects4J installation.
#
# The scripts writes the log files to: ./check-fp-logs

[ -z $1 ] && echo "usage: $0 project_id" && exit 1
pid=$1

[ -z $D4J_HOME ] && echo "D4J_HOME variable needs to be set (root directory of Defects4J)" && exit 1
pid=$1


DIR=check-fp-logs
mkdir -p $DIR

for tool in evosuite randoop; do
    # Unpack all triggers if necessary
    [ -d "$tool-triggers" ] || tar -xzf "$tool-triggers.tar.gz"
    # Write all root causes to log file
    grep -A3 "\---" $tool-triggers/$pid-*.log > $DIR/$tool-$pid.check.log
    >.tmp
    # Determine unique failures
    for file in `find $tool-triggers -name "$pid-*.log"`; do
        bug=`echo $file | cut -f1 -d'.' | cut -f2 -d'/'`
        bid=`echo $bug | cut -f2 -d'-' | tr -d 'b'`

        # Get root cause(s) from developer-written test suite
        orig_msg=`awk '/---/{getline; print}' $D4J_HOME/framework/projects/$pid/trigger_tests/$bid | sort -u | tr '\n' '|'`
        # Get all unique root causes from current log file
        awk '/---/{getline; print}' $file | sort -u > .msg
        if [ -s .msg ]; then
            while read line; do
                echo "$bug -- $line@## ORIG: ## $orig_msg@" >> .tmp
            done < .msg
        fi
        rm .msg
    done
    sort -u .tmp | tr '@' '\n' > $DIR/$tool-$pid.check.uniq && rm .tmp
done
