/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.baaas.dfs.service;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.kie.baaas.dfs.api.Decision;
import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.model.NetworkResource;
import org.kie.baaas.dfs.service.networking.NetworkingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

import static org.kie.baaas.dfs.api.DecisionConstants.CLIENTID_KEY;
import static org.kie.baaas.dfs.api.DecisionConstants.CLIENTSECRET_KEY;
import static org.kie.baaas.dfs.api.DecisionVersionStatus.CONDITION_BUILD;
import static org.kie.baaas.dfs.api.DecisionVersionStatus.REASON_FAILED;
import static org.kie.baaas.dfs.controller.DecisionLabels.BAAAS_RESOURCE_KOGITO_SERVICE;
import static org.kie.baaas.dfs.controller.DecisionLabels.BAAAS_RESOURCE_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.OPERATOR_NAME;
import static org.kie.baaas.dfs.service.JsonResourceUtils.buildEnvValue;
import static org.kie.baaas.dfs.service.JsonResourceUtils.buildEnvValueFromSecret;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getConditionStatus;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getName;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getOwnerUid;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getSpec;
import static org.kie.baaas.dfs.service.JsonResourceUtils.toJson;

@ApplicationScoped
public class KogitoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KogitoService.class);

    static final int REPLICAS = 1;

    static final String BAAAS_DASHBOARD_AUTH_SECRET = "baaas-dfs-kafka-credentials";
    static final String BAAAS_DASHBOARD_BOOTSTRAP_SERVERS = "BAAAS_DASHBOARD_BOOTSTRAP_SERVERS";
    static final String BAAAS_DASHBOARD_CLIENTID = "BAAAS_DASHBOARD_CLIENTID";
    static final String BAAAS_DASHBOARD_CLIENTSECRET = "BAAAS_DASHBOARD_CLIENTSECRET";

    static final String BAAAS_KAFKA_BOOTSTRAP_SERVERS = "BAAAS_KAFKA_BOOTSTRAP_SERVERS";
    static final String BAAAS_KAFKA_CLIENTID = "BAAAS_KAFKA_CLIENTID";
    static final String BAAAS_KAFKA_CLIENTSECRET = "BAAAS_KAFKA_CLIENTSECRET";

    static final String BAAAS_KAFKA_INCOMING_TOPIC = "BAAAS_KAFKA_INCOMING_TOPIC";
    static final String BAAAS_KAFKA_OUTGOING_TOPIC = "BAAAS_KAFKA_OUTGOING_TOPIC";

    static final String BOOTSTRAP_SERVERS_KEY = "bootstrapservers";

    public static final CustomResourceDefinitionContext KOGITO_RUNTIME_CONTEXT = new CustomResourceDefinitionContext.Builder()
            .withGroup("app.kiegroup.org")
            .withVersion("v1beta1")
            .withName("kogitoruntimes.app.kiegroup.org")
            .withKind("KogitoRuntime")
            .withPlural("kogitoruntimes")
            .withScope("Namespaced")
            .build();

    @Inject
    DecisionVersionService versionService;

    @Inject
    NetworkingService networkingService;

    @Inject
    KubernetesClient client;

    public static String getServiceName(DecisionVersion version) {
        return version.getMetadata().getName();
    }

    public static JsonObject build(DecisionVersion version) {
        JsonObjectBuilder specBuilder = Json.createObjectBuilder()
                .add("image", version.getStatus().getImageRef())
                .add("replicas", REPLICAS);
        JsonArrayBuilder envBuilder = Json.createArrayBuilder()
                .add(buildEnvValueFromSecret(
                        BAAAS_DASHBOARD_BOOTSTRAP_SERVERS,
                        BOOTSTRAP_SERVERS_KEY,
                        BAAAS_DASHBOARD_AUTH_SECRET))
                .add(buildEnvValueFromSecret(
                        BAAAS_DASHBOARD_CLIENTID,
                        CLIENTID_KEY,
                        BAAAS_DASHBOARD_AUTH_SECRET))
                .add(buildEnvValueFromSecret(
                        BAAAS_DASHBOARD_CLIENTSECRET,
                        CLIENTSECRET_KEY,
                        BAAAS_DASHBOARD_AUTH_SECRET));
        if (version.getSpec().getKafka() != null) {
            envBuilder
                    .add(buildEnvValueFromSecret(
                            BAAAS_KAFKA_CLIENTID,
                            CLIENTID_KEY,
                            version.getSpec().getKafka().getSecretName()))
                    .add(buildEnvValueFromSecret(
                            BAAAS_KAFKA_CLIENTSECRET,
                            CLIENTSECRET_KEY,
                            version.getSpec().getKafka().getSecretName()))
                    .add(buildEnvValue(
                            BAAAS_KAFKA_BOOTSTRAP_SERVERS,
                            version.getSpec().getKafka().getBootstrapServers()));
            if (version.getSpec().getKafka().getInputTopic() != null) {
                envBuilder.add(buildEnvValue(
                        BAAAS_KAFKA_INCOMING_TOPIC,
                        version.getSpec().getKafka().getInputTopic()));
            }
            if (version.getSpec().getKafka().getOutputTopic() != null) {
                envBuilder.add(buildEnvValue(
                        BAAAS_KAFKA_OUTGOING_TOPIC,
                        version.getSpec().getKafka().getOutputTopic()));
            }
        }
        specBuilder.add("env", envBuilder.build());
        //Kogito Operator requires to own the KogitoRuntime resource.
        version.getMetadata().getOwnerReferences().get(0).setController(false);
        JsonArrayBuilder ownerRefs = Json.createArrayBuilder().add(toJson(version.getOwnerReference()));
        return Json.createObjectBuilder()
                .add("apiVersion", KOGITO_RUNTIME_CONTEXT.getGroup() + "/" + KOGITO_RUNTIME_CONTEXT.getVersion())
                .add("kind", KOGITO_RUNTIME_CONTEXT.getKind())
                .add("metadata", Json.createObjectBuilder()
                        .add("name", getServiceName(version))
                        .add("namespace", version.getMetadata().getNamespace())
                        .add("labels", Json.createObjectBuilder()
                                .add(BAAAS_RESOURCE_LABEL, BAAAS_RESOURCE_KOGITO_SERVICE)
                                .add(DECISION_LABEL, version.getMetadata().getLabels().get(DECISION_LABEL))
                                .add(CUSTOMER_LABEL, version.getMetadata().getLabels().get(CUSTOMER_LABEL))
                                .add(MANAGED_BY_LABEL, OPERATOR_NAME)
                                .build())
                        .add("ownerReferences", ownerRefs)
                        .build())
                .add("spec", specBuilder.build())
                .build();
    }

    public void createOrUpdate(DecisionVersion version) {
        if (!isBuilt(version) || !isCurrent(version)) {
            return;
        }
        LOGGER.info("Creating or Updating Kogito Runtime for DecisionVersion {}", version.getMetadata().getName());
        JsonObject expected = build(version);
        createOrUpdateDashboardAuthSecret(version.getMetadata().getNamespace());
        String name = getName(expected);
        JsonObject current = null;
        try {
            current = Json.createObjectBuilder(client.customResource(KOGITO_RUNTIME_CONTEXT)
                    .get(version.getMetadata().getNamespace(), version.getMetadata().getName()))
                    .build();
        } catch (KubernetesClientException e) {
            LOGGER.debug("KogitoRuntime {} does not exist. Creating...", name);
        }
        //TODO KOGITO-4536 - Only createOrReplace when needsUpdate
        if (current == null) {
            try {
                current = Json.createObjectBuilder(client.customResource(KOGITO_RUNTIME_CONTEXT)
                        .createOrReplace(version.getMetadata().getNamespace(), expected.toString()))
                        .build();
            } catch (IOException e) {
                LOGGER.warn("Unable to process KogitoService for DecisionVersion {}", version.getMetadata().getName(), e);
                versionService.setServiceStatus(version, Boolean.FALSE, REASON_FAILED, e.getMessage());
            }
        } else if (needsUpdate(expected, current)) {
            try {
                client.customResource(KOGITO_RUNTIME_CONTEXT).createOrReplace(version.getMetadata().getNamespace(), expected.toString());
                LOGGER.info("Deleting KogitoRuntime {}. See KOGITO-4536", getName(expected));
                client.customResource(KOGITO_RUNTIME_CONTEXT).delete(version.getMetadata().getNamespace(), getName(expected));
                version.getStatus().setKogitoServiceRef(null);
                version.getStatus().setReady(Boolean.FALSE);
                versionService.setServiceStatus(version, Boolean.FALSE, "KogitoRuntimeRedeploy", "re-creating KogitoRuntime");
                return;
            } catch (IOException e) {
                LOGGER.warn("Unable to delete KogitoService", e);
                versionService.setServiceStatus(version, Boolean.FALSE, REASON_FAILED, e.getMessage());
            }
        }
        //END KOGITO-4536
        version.getStatus().setKogitoServiceRef(name);
        boolean provisioning = getConditionStatus(current, "Provisioning");
        boolean deployed = getConditionStatus(current, "Deployed");
        String reason = provisioning ? "Provisioning" : "Unknown";
        Boolean status = Boolean.FALSE;
        String message = "";
        if (deployed) {
            try {
                JsonObject kogitoRuntime = Json.createObjectBuilder(client.customResource(KOGITO_RUNTIME_CONTEXT)
                        .get(version.getMetadata().getNamespace(), version.getStatus().getKogitoServiceRef())).build();
                if (Objects.equals(version.getMetadata().getUid(), JsonResourceUtils.getOwnerUid(kogitoRuntime))) {
                    reason = "Deployed";
                    status = Boolean.TRUE;
                    versionService.setReadyStatus(version);
                    NetworkResource networkResource = networkingService.getOrCreateVersionEndpoint(version, version.getOwnerReference());
                    if (networkResource == null) {
                        LOGGER.info("DecisionVersion {} is not ready because of its networking resource.", version.getMetadata().getName());
                        return;
                    }

                    version.getStatus().setEndpoint(URI.create(networkResource.getEndpoint()));
                }
            } catch (KubernetesClientException e) {
                LOGGER.warn("Unable to retrieve KogitoRuntime {} for DecisionVersion {}",
                        version.getStatus().getKogitoServiceRef(),
                        version.getMetadata().getName(), e);
            }
        }
        versionService.setServiceStatus(version, status, reason, message);
    }

    private boolean needsUpdate(JsonObject expected, JsonObject current) {
        JsonObject expectedSpec = getSpec(expected);
        JsonObject currentSpec = getSpec(current);
        if (!Objects.equals(expectedSpec.getString("image"), currentSpec.getString("image"))
                || !Objects.equals(expectedSpec.getInt("replicas"), currentSpec.getInt("replicas"))
                || !Objects.equals(getOwnerUid(expected), getOwnerUid(current))) {
            return true;
        }
        if (expectedSpec.containsKey("env")) {
            if (!currentSpec.containsKey("env")) {
                return true;
            }
            return !expectedSpec.getJsonArray("env").stream()
                    .map(v -> v.asJsonObject())
                    .allMatch(e -> currentSpec.getJsonArray("env").stream()
                            .map(JsonValue::asJsonObject)
                            .anyMatch(c -> Objects.equals(e, c)));
        }
        return false;
    }

    private boolean isBuilt(DecisionVersion version) {
        return version.getStatus() != null
                && version.getStatus().getCondition(CONDITION_BUILD) != null
                && Boolean.parseBoolean(version.getStatus().getCondition(CONDITION_BUILD).getStatus());
    }

    private boolean isCurrent(DecisionVersion version) {
        Decision decision = client.customResources(Decision.class)
                .inNamespace(version.getMetadata().getNamespace())
                .withName(version.getMetadata().getLabels().get(DECISION_LABEL))
                .get();
        return decision != null
                && Objects.equals(decision.getSpec().getDefinition().getVersion(), version.getSpec().getVersion());
    }

    private void createOrUpdateDashboardAuthSecret(String namespace) {
        Secret current = client.secrets()
                .inNamespace(namespace)
                .withName(BAAAS_DASHBOARD_AUTH_SECRET)
                .get();
        // TODO: Replace how credentials are retrieved from a secure vault. For the demo will be a pre-provisioned secret.
        Secret vault = client.secrets()
                .inNamespace(client.getNamespace())
                .withName(BAAAS_DASHBOARD_AUTH_SECRET)
                .get();
        if (vault == null) {
            LOGGER.error("Missing required kafka-auth secret {} in {}", BAAAS_DASHBOARD_AUTH_SECRET, client.getNamespace());
            return;
        }
        if (current == null || !Objects.equals(current.getData(), vault.getData())) {
            Secret expected = new SecretBuilder()
                    .withMetadata(new ObjectMetaBuilder()
                            .withNamespace(namespace)
                            .withName(BAAAS_DASHBOARD_AUTH_SECRET)
                            .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                            .build())
                    .withData(vault.getData())
                    .build();
            LOGGER.debug("Create or replace kafka-auth secret {} in {}", expected.getMetadata().getName(), expected.getMetadata().getNamespace());
            client.secrets().inNamespace(namespace).createOrReplace(expected);
        }
    }

}
