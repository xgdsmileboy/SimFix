#!/bin/bash

# Ensures that the `java` on your PATH is version 1.7, or returns exit status 1.
# Prepends "$JAVA_HOME/bin" to your PATH if necessary.
# You should source this script, not just run it, so it can modify your PATH.
#
# Usage:
#   source ensure-java-1.7.sh
#

is-java-1.7() {
  test "$("$1" -version 2>&1 | egrep 'version.*[^.0-9]1.7')"
}

if ! is-java-1.7 java; then
  if is-java-1.7 "$JAVA_HOME/bin/java"; then
    export PATH="$JAVA_HOME/bin:$PATH"
  else
    echo 'Error: neither java nor $JAVA_HOME/bin/java looks like version 1.7' >&2
    return 1
  fi
fi
