#!/bin/bash

shopt -s globstar

ALL_COMPLETED='yes'

while IFS=',' read PROJECT BUG TIMEOUT; do
  [ "$PROJECT $BUG $TIMEOUT" = "Project Bug Timeout" ] && continue
  VERSION="$PROJECT-$BUG"
  LOG="$HOME/killmaps/$PROJECT/$BUG/log.txt"
  TAR="$HOME/killmaps/$PROJECT/$BUG.tar.gz"
  COMPLETION_TIMESTAMP="$HOME/killmaps/$PROJECT/$BUG.completed"

  [ -f "$COMPLETION_TIMESTAMP" ] || ALL_COMPLETED='no'

  if [ -f "$COMPLETION_TIMESTAMP" ]; then
    echo "complete: $VERSION"
  elif [ -f "$TAR" ]; then
    echo "compressing/sending: $VERSION"
  elif [ -f "$LOG" ]; then
    if grep -q "Completed successfully" "$LOG"; then
      echo "complete log (but no tar file yet; huh): $VERSION"
    else
      echo "incomplete log: $VERSION"
    fi
    echo "  log last modified at $(stat --format "%y" "$LOG")"
    if grep -q 'starting test' "$LOG"; then
      echo "  last test started: $(grep "starting test" "$LOG" | tail -n 1)"
      echo "  last test's time estimate: $(grep "should take at most" "$LOG" | tail -n 1)"
    fi
    if egrep -q '^java\.lang\.' "$LOG"; then
      echo "  uncaught exception: $(egrep "^java\.lang\." "$LOG")"
    fi
  else
    echo "unstarted: $VERSION"
  fi
done < ~/targets.csv

if [ "$ALL_COMPLETED" = 'yes' ]; then
  echo 'All completed!'
fi
