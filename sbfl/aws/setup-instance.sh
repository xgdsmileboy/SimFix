#!/bin/bash

HERE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P) || { echo "unable to cd into $(dirname "${BASH_SOURCE[0]}")" >&2; exit 1; }

source "$HERE/utils.sh"

HOST=$(bash "$HERE/launch-new-spot-instance.sh" "$HERE/setup-launch-spec.json") || die 'unable to launch spot instance'

scp-to-instance "$HOST" "$HERE/setup-instance-payload.sh" '~/setup-self.sh'
ssh-to-instance "$HOST" <<< 'chmod +x ./setup-self.sh; ./setup-self.sh'

echo ''
echo '##############################################'
echo 'Configuring image so it can access your S3 bucket.'
echo ''
ssh-to-instance "$HOST" aws configure

INSTANCE_ID=$(dns-to-instance-id "$HOST") || die "unable to get instance-id for $HOST"
IMAGE_ID=$(aws ec2 create-image --instance-id "$INSTANCE_ID" --name "fl-killmap-generation-$(date '+%Y-%m-%d-%H-%M-%S')" | python3 "$HERE/jsonextract.py" ImageId) || die 'failed to create AMI'
aws ec2 wait image-available --image-id "$IMAGE_ID" || die 'failed waiting for image to become available(!?!?)'
aws ec2 terminate-instances --instance-ids "$INSTANCE_ID" || die "failed to terminate $INSTANCE_ID(!?!?)"

sed "s ami-[^\"]* $IMAGE_ID " < "$HERE/setup-launch-spec.json" > "$HERE/generation-launch-spec.json"
