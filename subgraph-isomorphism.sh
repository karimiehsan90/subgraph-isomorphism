#!/usr/bin/env bash

set -o errexit

source subgraph-isomorphism-env.sh

build() {
  for module in "${DOCKERIZE_MODULES[@]}"; do
    docker build \
      -t "subgraph-isomorphism/${module}" \
      -f "${module}/Dockerfile" \
      "${module}"
  done
}

setup-vm() {
  ./provisioning/vagrant/setup-vm.sh
}

parse-args() {
  METHOD=${1}
  shift
}

main() {
  parse-args "${@}"
  ${METHOD}
}

main "${@}"
