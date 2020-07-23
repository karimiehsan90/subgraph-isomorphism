#!/usr/bin/env bash

set -o errexit

inventory=${1}
playbook=${2}
shift
shift

ansible-playbook "playbooks/${playbook}.yml" -i "inventories/${inventory}" "${@}"
