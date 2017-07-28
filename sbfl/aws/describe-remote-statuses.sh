#!/bin/bash

HERE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P) || { echo "unable to cd into $(dirname "${BASH_SOURCE[0]}")" >&2; exit 1; }
source "$HERE/utils.sh"

USAGE="$0 HOSTS_FILE"
if [ "$#" != 1 ]; then
  die "usage: $USAGE"
fi

HOSTS_FILE="$1"

ALL_INSTANCE_INFO=$(aws ec2 describe-instances)

while read HOST; do
  echo ''
  echo '################################################################'
  echo "## $HOST"
  echo '################################################################'

  if ! grep -q "$HOST" <<<"$ALL_INSTANCE_INFO"; then
    echo 'not alive'
    continue
  fi

  scp-to-instance "$HOST" "$HERE/describe-status.sh" "~/describe-status.sh"
  ssh-to-instance "$HOST" <<< '~/describe-status.sh'
done < "$HOSTS_FILE"
