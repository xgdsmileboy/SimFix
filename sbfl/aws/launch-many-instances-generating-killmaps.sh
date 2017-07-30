#!/bin/bash

HERE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P) || { echo "unable to cd into $(dirname "${BASH_SOURCE[0]}")" >&2; exit 1; }

die() { echo $'\e[0;31m'"$*"$'\e[0m' >&2; exit 1; }
USAGE="$0 DESTINATION TARGET_FILE [TARGET_FILE ...]"

OPTIONS=()
while [[ "$1" = --* ]]; do
  OPTIONS+=($1)
  shift
done

[ "$#" -ge 2 ] || die "usage: $USAGE"
DESTINATION="$1"
shift

launch() {
  HOST=$("$HERE/launch-new-spot-instance.sh" "$HERE/generation-launch-spec.json")
  "$HERE/generate-killmaps-remotely.sh" "${OPTIONS[@]}" "$HOST" "$DESTINATION" "$1"
  echo "$TARGET_FILE,$HOST"
}

echo 'TargetFile,Host'
for TARGET_FILE in "$@"; do
  launch "$TARGET_FILE" &
  sleep 10 # requests crash sometimes; I think it's from firing too many at once
done

wait
