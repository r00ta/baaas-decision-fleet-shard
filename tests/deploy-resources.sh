#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
# set -o xtrace

# Set magic variables
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
__root="$(cd "$(dirname "${__dir}")" && pwd)"
__target="${__root}/target"
__gitremote="$(git remote get-url origin)"
__infra_repo="${__gitremote/baaas-decision-fleet-shard/baaas-infra}"
__infra_path="${__target}/baaas-infra"
__pipeline_resources="${__infra_path}/base/dfs/resources/build-pipeline/"
__credentials="${__dir}/user-credentials.sh"
__pull_secret="${__dir}/user-pull-secret.yaml"

deploy_crds () {
  kubectl apply -f "${__root}"/crd || true
}

create_cms () {
  kubectl create cm baaas-dfs-build-application-kafka-props --from-file=application.properties="${__pipeline_resources}"/application-kafka.properties || true
  kubectl create cm baaas-dfs-build-application-props --from-file="${__pipeline_resources}"/application.properties || true
  kubectl create cm baaas-dfs-build-pom-kafka-xml --from-file=pom.xml="${__pipeline_resources}"/pom-kafka.xml || true
  kubectl create cm baaas-dfs-build-pom-xml --from-file="${__pipeline_resources}"/pom.xml || true
  kubectl create cm baaas-dfs-build-dockerfile --from-file="${__pipeline_resources}"/Dockerfile || true
}

create_pipeline () {
  kubectl apply -f "${__pipeline_resources}"/pipeline.yml || true
  kubectl apply -f "${__pipeline_resources}"/pvc.yml || true
}

create_user_credentials () {
  # from user-credentials.sh file
  kubectl create secret generic baaas-dfs-build-aws-credentials \
    --from-literal=AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID}" \
    --from-literal=AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION}" \
    --from-literal=AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY}" || true

  kubectl create secret generic baaas-dfs-kafka-credentials \
    --from-literal=bootstrapservers=${KAFKA_SERVER} \
    --from-literal=clientid=${KAFKA_CLIENT_ID} \
    --from-literal=clientsecret=${KAFKA_CLIENT_SECRET} || true
}

create_pull_secret () {
  if [ -f "${__pull_secret}" ]
  then
    kubectl apply -f "${__pull_secret}" || true
    secret_name="$(grep name "${__pull_secret}" | awk '{print$2}')"
    kubectl patch serviceaccount default -p "{\"imagePullSecrets\": [{\"name\": \"${secret_name}\"}]}" || true
  else
    echo "Pull secret file not found. Assuming CRC installation."
  fi
}

if [ ! -f "${__credentials}" ]
then
  echo ERROR missing "${__credentials}" file
  exit 1
fi
source "${__credentials}"
if [ ! -d "${__infra_path}" ]
then
  mkdir "${__target}"
  git clone "${__infra_repo}" "${__infra_path}"
fi

deploy_crds
create_cms
create_pull_secret
create_pipeline
create_user_credentials

