#!/bin/bash
#
# Runs Killmap many times and puts the results on S3.
#
# Usage:
#
#     generate-killmaps-and-put-on-s3.sh [--suffix=SUFFIX] [--OPTIONS ...] s3://some-bucket/some-subdir targets.csv
#
# where `targets.csv` looks like
#
#     Project,Bug,Timeout
#     Lang,1,16h
#     Lang,30000,16h
#
# This will generate a killmap for Lang-1b, taking no more than 16 hours;
#   then for Lang-30000b, taking no more than 16 hours.
# (The timeouts are passed into the `timeout` utility.)
# The killmap (and associated log files) will be put on S3, in
#   s3://some-bucket/some-subdir/{project}/{bug}.tar.gz
#
# Other options starting with '--' will be passed through to Killmap.
#

die() { echo "$@" >&2; exit 1; }

SUFFIX=''

OPTIONS=()
while [[ "$1" = --* ]]; do
  if [[ "$1" = --suffix=* ]]; then
    SUFFIX=${1#--suffix=}; shift
  else
    OPTIONS+=($1); shift
  fi
done

USAGE="$0 [--suffix=SUFFIX] [--OPTIONS ...] DESTINATION TARGETS.csv"
if [ "$#" != 2 ]; then
  die "usage: $USAGE"
fi

DESTINATION="$1"
aws s3 ls "$DESTINATION" || die "unable to ls '$DESTINATION' on S3"
TARGETS_CSV="$2"

cd || exit

echo "TARGETS_CSV is $TARGETS_CSV with contents:"
cat "$TARGETS_CSV"
echo ''

while IFS=',' read PROJECT BUG TIMEOUT; do
  echo "read $PROJECT $BUG $TIMEOUT"
  if [ "$PROJECT $BUG $TIMEOUT" = 'Project Bug Timeout' ]; then
    continue
  fi

  PARTIAL_KILLMAP="/tmp/prior-killmap-$$.csv.gz"
  DIRNAME="killmaps$SUFFIX/$PROJECT/$BUG"
  echo "DIRNAME = $DIRNAME"
  if aws s3 cp "$DESTINATION/$DIRNAME.tar.gz" "$DIRNAME.tar.gz"; then
    tar xf "$DIRNAME.tar.gz"
    mv "$DIRNAME/killmap.csv.gz" "$PARTIAL_KILLMAP"
    rm -r "$DIRNAME"
  else
    gzip </dev/null >"$PARTIAL_KILLMAP"
  fi

  mkdir -p "$DIRNAME"

  echo "about to start $PROJECT-$BUG with timeout $TIMEOUT"
  timeout "$TIMEOUT" "$KILLMAP_HOME/scripts/generate-matrix.sh" "${OPTIONS[@]}" --partial-output <(zcat "$PARTIAL_KILLMAP") "$PROJECT" "$BUG" "/tmp/$PROJECT-$BUG$SUFFIX" "$DIRNAME/mutants.log" 2>"$DIRNAME/log.txt" | gzip >"$DIRNAME/killmap.csv.gz"
  echo 'done'
  tar -czf "$DIRNAME.tar.gz" "$DIRNAME"
  aws s3 cp "$DIRNAME.tar.gz" "$DESTINATION/$DIRNAME.tar.gz"
  touch "$DIRNAME.completed"
done < "$TARGETS_CSV"

sudo shutdown -h now
