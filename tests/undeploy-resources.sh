#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
# set -o xtrace

# Set magic variables
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
__root="$(cd "$(dirname "${__dir}")" && pwd)"
__target="${__root}/target"
__pull_secret="${__dir}/user-pull-secret.yaml"

kubectl delete cm baaas-dfs-build-application-kafka-props baaas-dfs-build-application-props \
  baaas-dfs-build-pom-kafka-xml baaas-dfs-build-pom-xml baaas-dfs-build-dockerfile || true

kubectl delete pipeline,task baaas-dfs-decision-build || true

kubectl delete secrets baaas-dfs-build-aws-credentials baaas-dfs-kafka-credentials || true



kubectl delete -f "${__root}/crd" || true

if [ -f "${__pull_secret}" ]
then
  kubectl delete -f "${__pull_secret}" || true
  kubectl patch serviceaccount default -p '{"imagePullSecrets": [{"$patch": "remove", "path": "/imagePullSecrets"}]}'
else
  echo "Pull secret file not found. Assuming CRC installation."
fi

if [ "${1:-}" = "clean" ]
then
  echo Removing target folder
  rm -rf ${__target}
else
  echo Use \'undeploy_resources.sh clean\' to also remove the target folder
fi
