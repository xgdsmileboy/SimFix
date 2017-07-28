#!/bin/bash

HERE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P) || { echo "unable to cd into $(dirname "${BASH_SOURCE[0]}")" >&2; exit 1; }
source "$HERE/utils.sh"

USAGE="$0 LAUNCH_SPEC"
[ "$1" ] || die "usage: $USAGE"
LAUNCH_SPEC="$1"

SPOT_REQUEST_INFO=$(aws ec2 request-spot-instances \
  --spot-price 0.10 \
  --instance-count 1 \
  --type 'one-time' \
  --launch-specification "file://$LAUNCH_SPEC")
SPOT_REQUEST_ID=$(python3 "$HERE/jsonextract.py" SpotInstanceRequests 0 SpotInstanceRequestId <<<"$SPOT_REQUEST_INFO")

echo "requested spot instance; request ID is $SPOT_REQUEST_ID" >&2

aws ec2 wait spot-instance-request-fulfilled --spot-instance-request-ids "$SPOT_REQUEST_ID" >&2 ||
  die 'spot instance was not fulfilled'

INSTANCE_ID=$(aws ec2 describe-spot-instance-requests --spot-instance-request-ids "$SPOT_REQUEST_ID" | python3 "$HERE/jsonextract.py" SpotInstanceRequests 0 InstanceId) ||
  die 'unable to get spot-instance info'

HOST=$(instance-id-to-dns "$INSTANCE_ID") ||
  die "unable to extract host from instance info for $INSTANCE_ID"

echo "request $SPOT_REQUEST_ID was fulfilled; instance ID is $INSTANCE_ID, domain name is $HOST" >&2

aws ec2 wait instance-status-ok --instance-ids "$INSTANCE_ID" >&2 ||
  die 'instance just is not coming up'

echo "$HOST"
