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

kubectl delete cm baaas-ccp-build-application-kafka-props baaas-ccp-build-application-props \
  baaas-ccp-build-pom-kafka-xml baaas-ccp-build-pom-xml baaas-ccp-build-dockerfile || true

kubectl delete pipeline,task baaas-ccp-decision-build || true

kubectl delete secrets baaas-ccp-build-aws-credentials baaas-ccp-build-registry-push-token baaas-ccp-kafka-credentials || true



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
