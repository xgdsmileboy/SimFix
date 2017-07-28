#!/bin/bash
HERE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P) || { echo "unable to cd into $(dirname "${BASH_SOURCE[0]}")" >&2; exit 1; }
KEY_FILE="$HERE/fault-localization.pem"
die() { echo $'\e[0;31m'"$*"$'\e[0m' >&2; exit 1; }

scp-to-instance() {
  local HOST SRC_PATH DST_PATH RET
  HOST="$1"
  SRC_PATH="$2"
  DST_PATH="$3"
  ssh-keygen -R "$HOST" >/dev/null 2>&1
  scp -r -o 'StrictHostKeyChecking no' -i "$KEY_FILE" "$SRC_PATH" "ec2-user@$HOST:$DST_PATH"
  RET=$?
  ssh-keygen -R "$HOST" >/dev/null 2>&1
  return $RET
}

ssh-to-instance() {
  local HOST RET
  HOST="$1"; shift
  ssh-keygen -R "$HOST" >/dev/null 2>&1
  ssh -t -o 'StrictHostKeyChecking no' -i "$KEY_FILE" "ec2-user@$HOST" "$@"
  RET=$?
  ssh-keygen -R "$HOST" >/dev/null 2>&1
  return $RET
}

instance-id-to-dns() {
  aws ec2 describe-instances --instance-ids "$1" | python3 "$HERE/jsonextract.py" Reservations 0 Instances 0 PublicDnsName
}

dns-to-instance-id() {
  aws ec2 describe-instances --filters "Name=dns-name,Values=$1" | python3 jsonextract.py Reservations 0 Instances 0 InstanceId
}
