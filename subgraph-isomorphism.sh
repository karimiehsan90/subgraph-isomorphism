#!/usr/bin/env bash

set -o errexit

source subgraph-isomorphism-env.sh

test() {
  mvn test
}

build() {
  mvn clean package -DskipTests
  for module in "${DOCKERIZE_MODULES[@]}"; do
    docker build \
      -t "subgraph-isomorphism/${module}" \
      -f "${module}/Dockerfile" \
      "${module}"
  done
}

deploy() {
  scp map-reduce/target/map-reduce-1.0-SNAPSHOT-jar-with-dependencies.jar admin@195.248.242.194:subgraph-isomorphism.jar

}

setup-vm() {
  ./provisioning/vagrant/setup-vm.sh
}

run-playbook() {
  inventory=${1:-local}
  playbook=${2:-main}
  ansible_args=("")
  if [ $# -gt 2 ]; then
    ansible_args=("${@:3}")
  fi
  docker run -i \
    --net host \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --rm \
    subgraph-isomorphism/provisioning \
      "${inventory}" \
      "${playbook}" \
      "${ansible_args[@]}"
}

parse-args() {
  METHOD=${1}
}

main() {
  parse-args "${@}"
  shift
  ${METHOD} "${@}"
}

main "${@}"
