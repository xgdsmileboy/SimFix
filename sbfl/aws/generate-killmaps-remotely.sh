#!/bin/bash
#
# Usage:
#
#     generate-killmaps-remotely.sh [--OPTIONS ...] \
#       ec2-52-37-185-103.us-west-2.compute.amazonaws.com \
#       s3://some-bucket/some-subdir
#       targets.csv
#
# will SSH into the given host and tell it to run
#   `generate-killmaps-and-put-on-s3.sh` on the given `targets.csv`,
#   putting the results in `s3://some-bucket/some-subdir`.
#   (Both the script and the CSV file are copied over to the remote machine.)
#
# Any options starting with `--` will be passed through to Killmap.
#

HERE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P) || { echo "unable to cd into $(dirname "${BASH_SOURCE[0]}")" >&2; exit 1; }
source "$HERE/utils.sh"

OPTIONS=()
while [[ "$1" = --* ]]; do
  OPTIONS+=($1)
  shift
done

USAGE="$0 [--OPTIONS ...] HOST DESTINATION TARGETS.csv"
if [ "$#" != 3 ]; then
  die "usage: $USAGE"
fi

HOST="$1"
DESTINATION="$2"
TARGETS_CSV="$3"

echo 'Sending script and targets (via SCP)...' >&2
scp-to-instance "$HOST" \
  "$HERE/generate-killmaps-and-put-on-s3.sh" \
  "~/generate-killmaps-and-put-on-s3.sh" \
  || die 'Failed to send script over SCP.'
scp-to-instance "$HOST" "$TARGETS_CSV" "~/targets.csv" \
  || die 'Failed to send targets over SCP.'

echo 'Starting remote process...' >&2
ssh-to-instance "$HOST" <<< '
    nohup ~/generate-killmaps-and-put-on-s3.sh '"${OPTIONS[*]}"' '"$DESTINATION"' ~/targets.csv >~/generate-killmaps-remotely-$$.stdout.txt 2>~/generate-killmaps-remotely-$$.stderr.txt &
  ' || die 'Failed to fire-and-forget killmap over SSH.'

echo 'Done!' >&2
