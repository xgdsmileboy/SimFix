#!/usr/bin/env bash
#
################################################################################
# This script populates Defects4J's database with killable mutants.
# - Mutants are generated for all classes modified by the fix for the real fault.
# - Only mutants that are killed by any test of the developer-written test suite
#   are added to the database.
#
# Usage:
# populate_d4j <PID> <VID_START> <VID_END>
#
# Positional parameters:
# - <PID>        Defects4J's project ID: Chart, Closure, Lang, Math, or Time.
# - <VID_START>  First Defects4J version ID to consider (inclusive).
# - <VID_END>    Last Defects4J version ID to consider (inclusive).
#
# Examples:
# - All project versions in Lang:
#   populate_d4j Lang 1 65
#
# - A subset of all project versions in Math:
#   populate_d4j Math 1 10
#
# Environment variables:
# - D4J_HOME     Needs to be set and must point to the Defects4J installation that is to
#                be populated.
# - D4J_TMP_DIR  Set the temporary directory for this script (optional).
#                The default is: /tmp/populate_d4j_<process_id_of_this_script>.
#
# Requirements:
# The following command-line tools need to be installed:
# - diffstat
# - sed
#
# TODO: Determine triggering tests for each killable mutant.
# TODO: Sample mutants (uniformly, triggering test only, fixed methods only)?
################################################################################

#
# Print error message and exit
#
die() {
    echo $1
    exit 1
}

#
# Print usage message and exit
#
usage() {
    die "usage: $0 <PID> <VID_START> <VID_END>"
}

# Check arguments and set PID and VID range
[ $# -eq 3 ] || usage
PID=$1
VID_START=$2
VID_END=$3
# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"

# Set temporary directory used to checkout the project versions
DIR_TMP="${D4J_TMP_DIR:-"/tmp/populate_d4j_$$"}/$PID-$VID_START-$VID_END"
# Clean and create the temporary directory, if necessary
rm -rf $DIR_TMP
mkdir -p $DIR_TMP

# Defects4J's top-level build file.
D4J_BUILD_XML=$D4J_HOME/framework/projects/defects4j.build.xml
# Defects4J directories for given project id (PID)
DIR_REL_TESTS="$D4J_HOME/framework/projects/$PID/relevant_tests"
DIR_MOD_CLASSES="$D4J_HOME/framework/projects/$PID/modified_classes"
DIR_LOADED_CLASSES="$D4J_HOME/framework/projects/$PID/loaded_classes"
DIR_TRIGGERS="$D4J_HOME/framework/projects/$PID/trigger_tests"
DIR_PATCHES="$D4J_HOME/framework/projects/$PID/patches"
COMMIT_DB="$D4J_HOME/framework/projects/$PID/commit-db"

# Add Defects4J executables to the PATH
export PATH="$D4J_HOME/major/bin:$D4J_HOME/framework/bin:$D4J_HOME/framework/util:$PATH"

# Iterate over the requested project versions [VID_START,VID_END] for the given project id (PID)
for vid in $(seq $VID_START $VID_END); do
    # Skip all existing entries
    grep -q "^${vid}00001," $COMMIT_DB && echo "Skip $vid" && continue

    # Obtain and compile the fixed project version
    defects4j checkout -p $PID -v ${vid}f -w $DIR_TMP || die "Checkout failed"
    defects4j compile -w $DIR_TMP || die "Compilation failed"

    # Create mml file (mutant definitions) for modified classes
    create_mml.pl -p $PID -o $DIR_TMP -b $vid -c $DIR_MOD_CLASSES/${vid}.src || die "MML generation failed"
    # Set mml file, enable mutant export, and run Major to generate mutants
    export MML="$DIR_TMP/${vid}.mml.bin"
    export MAJOR_OPT="-J-Dmajor.export.mutants=true -J-Dmajor.strict.flow=true"
    cd $DIR_TMP
    ant -f $D4J_BUILD_XML -Dbasedir=$DIR_TMP -Dd4j.home=$D4J_HOME \
            mutate || die "Mutant generation failed"
    # Perform a full mutation analysis to obtain the set of killable mutants
    ant -f $D4J_BUILD_XML -Dbasedir=$DIR_TMP -Dd4j.home=$D4J_HOME -Dd4j.relevant.tests.only=true \
            -Dmajor.kill.log=$DIR_TMP/kill.log -Dmajor.exclude=$DIR_TMP/exclude.txt \
            mutation.test || die "Mutation analysis failed"
    # Save the mutation logs -- for debugging purposes and in case we need the mapping in the future
    cp $DIR_TMP/mutants.log $DIR_TRIGGERS/${vid}.mutants.log
    cp $DIR_TMP/kill.log $DIR_TRIGGERS/${vid}.kill.log

    # Determine the src directory (relative to working directory),
    # all ids of killable mutants, and the commit hashes of the real fault.
    dir_src=$(defects4j export -p dir.src.classes -w $DIR_TMP)
    mut_ids=$(grep -v "LIVE" $DIR_TMP/kill.log | cut -f1 -d',')
    # The commit-db requires a commit hash for the buggy and fixed version.
    # Since we don't store the mutants in the version control system, we don't
    # have a commit hash for the buggy version of mutants. However, we don't
    # need the hash of the buggy version, so we just re-use the one from the
    # real fault.
    hash_fixed=$(grep "^${vid}," $COMMIT_DB | cut -f3 -d',')
    hash_buggy=$(grep "^${vid}," $COMMIT_DB | cut -f2 -d',')

    # Write header of the csv file that maps bug ID to mutant ID
    # Note that bug IDs are consecutively numbered, each referencing the mutant ID of a
    # killable mutant.
    echo "BugID,MutantID" > $DIR_TRIGGERS/${vid}.mutants.map.csv

    # Numbering scheme for mutants (artificial bugs):
    # bug_id = <vid> * 100000 + <mutant_id>
    #
    # We allow 99999 killable mutants per version id (vid).
    bug_id="${vid}00001"
    for mut_id in $mut_ids; do
        # Checkout the unmutated source files (revert previous mutations)
        cd $DIR_TMP && git checkout $dir_src
        # Copy the mutated source code file
        cp -R $DIR_TMP/mutants/$mut_id/* $DIR_TMP/$dir_src
        #
        # For debugging purpose only:
        # Compile the source code of each mutant to make sure it was correctly exported by Major.
        # cd $DIR_TMP && git diff -- $dir_src > $DIR_PATCHES/${bug_id}.src.patch && defects4j compile
        #
        # Compute the diff and store the patch
        cd $DIR_TMP && git diff -- $dir_src > $DIR_PATCHES/${bug_id}.src.patch
        # Create an entry with computed bug_id in commit-db
        echo "${bug_id},${hash_buggy},${hash_fixed}" >> $COMMIT_DB
        # Determine the modified file from the patch, using diffstat, and extract the
        # class name.
        mod_class=$(diffstat -l -p1 $DIR_PATCHES/${bug_id}.src.patch \
            | sed -e "s|\.java||g" \
            | sed -e "s|${dir_src}||g" \
            | sed -e "s|^/*||g" \
            | tr '/' '.')
        cd $DIR_MOD_CLASSES && echo $mod_class > ${bug_id}.src
        # Prepare a trigger-test file and add a comment of the mutant id
        echo "# Mutant id: $mut_id" > $DIR_TRIGGERS/$bug_id
        # Add mapping from bug ID to mutant ID
        echo "$bug_id,$mut_id" >> $DIR_TRIGGERS/${vid}.mutants.map.csv

        # Create relevant-tests and loaded_classes links
        ln -s ${vid}      $DIR_REL_TESTS/${bug_id}
        ln -s ${vid}.src  $DIR_LOADED_CLASSES/${bug_id}.src
        ln -s ${vid}.test $DIR_LOADED_CLASSES/${bug_id}.test

        # Increment the bug_id for the next mutant of the same version id
        bug_id=$(($bug_id + 1))
    done
done
