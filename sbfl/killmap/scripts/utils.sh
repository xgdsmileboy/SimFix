#!/bin/bash

# Defines a bunch of little helper functions to glue together D4J and Killmap.
# You probably won't have to use this yourself -- it's mostly just scaffolding
# for `generate-matrix.sh`.
#
# The only function I can think of that you might find useful yourself is
#   d4j-checkout-and-prepare PROJECT BUG DIR
# which will check out a D4J project into the named directory, compile it
# with mutants, and do any other setup-stuff that needs to happen for matrix
# generation.
#
# If you do use this yourself, source it, don't just run it, so you have
# access to the functions it defines.
#
# Usage:
#   source utils.sh
#

KILLMAP_SCRIPTS_HOME=$(dirname "${BASH_SOURCE[0]}")
KILLMAP_SCRIPTS_HOME=$(readlink --canonicalize "$KILLMAP_SCRIPTS_HOME")

KILLMAP_HOME=$(readlink --canonicalize "$KILLMAP_SCRIPTS_HOME/..")

if [ ! "$DEFECTS4J_HOME" ]; then
  echo 'Error: DEFECTS4J_HOME not set' >&2
  return 1
fi

source "$KILLMAP_SCRIPTS_HOME/ensure-java-1.7.sh" || return 1

export PATH="$PATH:$DEFECTS4J_HOME/framework/bin"
export PATH="$PATH:$DEFECTS4J_HOME/framework/util"
export PATH="$DEFECTS4J_HOME/major/bin:$PATH"
export PYTHONPATH="$PYTHONPATH:$FL_DATA_HOME/utils"


user-is-asking-for-help() {
  # Exits with status 0 if any argument is -h or --help, else 1.
  for arg in "$@"; do
    if [ "$arg" = "-h" -o "$arg" = "--help" ]; then
      return 0;
    fi
  done
  return 1
}

d4j-create-mml() {
  # Creates the MML file telling Major how to mutate the given project/bug.
  local USAGE='d4j-create-mml PROJECT BUG DEST'
  if user-is-asking-for-help "$@"; then
    echo "Usage: $USAGE"
    echo 'Prints the name of the newly created compiled MML file (e.g. 1.mml.bin).'
    return 0
  fi
  if [ "$#" != 3 ]; then
    echo "Usage: USAGE"
    return 1
  fi

  local PROJECT=$1
  local BUG=$2
  local DEST=$3

  local TEMPDIR
  TEMPDIR=$(mktemp -d "/tmp/mml-$PROJECT-XXXX") &&
  "$DEFECTS4J_HOME/framework/util/create_mml.pl" -p "$PROJECT" -b "$BUG" -o "$TEMPDIR" -c "$DEFECTS4J_HOME/framework/projects/$PROJECT/loaded_classes/$BUG.src" &&
  mv "$TEMPDIR/$BUG.mml.bin" "$DEST" &&
  rm -rf "$TEMPDIR"
}

d4j-mutate() {
  # Compiles the D4J project in the named directory, with mutants.
  # At the moment, the mutated classes live in ".classes_mutated"
  # underneath the given directory.
  local USAGE='d4j-mutate PROJECT BUG DIR'
  if user-is-asking-for-help "$@"; then
    echo "Usage: $USAGE"
    echo 'Compiles the Defects4J project in the current directory with mutants.'
    return 0
  fi
  if [ "$#" != 3 ]; then
    echo "Usage: $USAGE" >&2
    return 1
  fi

  local PROJECT=$1
  local BUG=$2
  local DIR=$(readlink --canonicalize "$3")

  local MML
  export MML=$(mktemp "$DIR/.mml.bin.XXXX") &&
  d4j-create-mml "$PROJECT" "$BUG" "$MML" &&
  (cd "$DIR" && # ant sometimes has trouble finding things unless we cd in
   PATH="$PATH:$DEFECTS4J_HOME/major/bin" \
    ant -Dd4j.home="$DEFECTS4J_HOME" \
        -Dbasedir="$(pwd)" \
        -f "$DEFECTS4J_HOME/framework/projects/defects4j.build.xml" \
        mutate) &&
  rm -rf "$MML"
}

d4j-prepare() {
  # Does all necessary setup to get a freshly-checked-out D4J project ready
  # for matrix-generation.
  local USAGE='d4j-prepare PROJECT BUG DIR'
  if user-is-asking-for-help "$@"; then
    echo "Usage: $USAGE"
    echo 'Compiles the Defects4J project in the current directory with mutants.'
    return 0
  fi
  if [ "$#" != 3 ]; then
    echo "Usage: $USAGE" >&2
    return 1
  fi

  local PROJECT=$1
  local BUG=$2
  local DIR=$3

  (cd "$DIR" &&
   defects4j export -p cp.test -o d4j-cp.test.txt &&
   defects4j export -p tests.trigger -o triggering-tests.txt &&
   defects4j export -p tests.relevant -o relevant-test-classes.txt) \
  || return 1
  d4j-mutate "$PROJECT" "$BUG" "$DIR"
  (cd "$DIR" && defects4j compile) || return 1
}

filter-dumb-lines-printed-by-tests() {
  # Once in a while, when the matrix-generator runs a test, something gets
  # printed to stdout somehow. This function filters out any lines that
  # don't look like matrix-generator output.
  egrep -a '^[a-zA-Z0-9_$.]+#[a-zA-Z0-9_]+,[0-9]+,[0-9]+,'
}

#############################################
## ACTUALLY USEFUL FUNCTIONS
#############################################


d4j-checkout-and-prepare() {
  # Check out a D4J project and get it ready for matrix-generation.
  local USAGE='d4j-checkout-and-prepare PROJECT BUG DIR'
  if user-is-asking-for-help "$@"; then
    echo "Usage: $USAGE"
    echo 'Checks out the given project version, then prepares it for matrix generation.'
    return 0
  fi
  if [ "$#" != 3 ]; then
    echo "Usage: $USAGE" >&2
    return 1
  fi
  local PROJECT=$1
  local BUG=$2
  local DIR=$3

  export GRADLE_USER_HOME="$DIR/.gradle-local-home"
  defects4j checkout -p "$PROJECT" -v "${BUG}b" -w "$DIR" &&
  d4j-prepare "$PROJECT" "$BUG" "$DIR"
}

d4j-generate-matrix-here() {
  # Run Killmap on the D4J project in the current directory.
  # Prints the matrix to stdout.
  # Prints debugging/progress/timing information to stderr.
  local USAGE="d4j-generate-matrix-here [--help] KILLMAP_ARGS ..."
  if user-is-asking-for-help "$@"; then
    echo 'Generates the test-outcome matrix for the current Defects4J project,'
    echo 'using PARTIAL as a cache to avoid re-running tests from previous iterations.'
    return 0
  fi

  export TZ='America/Los_Angeles'
  export KILLMAP_CLASSPATH=".classes_mutated:$DEFECTS4J_HOME/major/lib/junit-4.11.jar:$(cat d4j-cp.test.txt | tr -d '\n'):$DEFECTS4J_HOME/major/config/config.jar:$KILLMAP_HOME/bin:$CLASSPATH"
  time java -cp "$KILLMAP_CLASSPATH" killmap.Main "$@" \
    | filter-dumb-lines-printed-by-tests
}
