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
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.api.DecisionVersionSpec;
import org.kie.baaas.dfs.api.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

import static org.kie.baaas.dfs.api.DecisionVersionStatus.REASON_FAILED;
import static org.kie.baaas.dfs.controller.DecisionLabels.BAAAS_RESOURCE_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.BAAAS_RESOURCE_PIPELINE_RUN;
import static org.kie.baaas.dfs.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_NAMESPACE_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_VERSION_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.OPERATOR_NAME;
import static org.kie.baaas.dfs.controller.DecisionLabels.OWNER_UID_LABEL;
import static org.kie.baaas.dfs.service.JsonResourceUtils.buildEnvValue;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getCondition;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getName;

@ApplicationScoped
public class PipelineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineService.class);

    public static final String PIPELINE_REF = "baaas-dfs-decision-build";
    public static final String VAR_PROPS_CONFIGMAP = "BUILD_INPUT_APP_PROPS_CONFIGMAP";
    public static final String VAR_POM_CONFIGMAP = "BUILD_INPUT_POM_XML_CONFIGMAP";
    public static final String VAR_DMN_LOCATION = "BUILD_INPUT_DMN_LOCATION";
    public static final String VAR_REGISTRY_LOCATION = "BUILD_OUTPUT_REGISTRY_PUSH_LOCATION";
    private static final String KAFKA_POM_XML_CONFIGMAP = "baaas-dfs-build-pom-kafka-xml";
    private static final String POM_XML_CONFIGMAP = "baaas-dfs-build-pom-xml";
    private static final String KAFKA_APP_PROPS_CONFIGMAP = "baaas-dfs-build-application-kafka-props";
    private static final String APP_PROPS_CONFIGMAP = "baaas-dfs-build-application-props";
    private static final String IMAGE_REF_TEMPLATE = "image-registry.openshift-image-registry.svc:5000/%s/%s:%s";

    public static final CustomResourceDefinitionContext PIPELINE_RUN_CONTEXT = new CustomResourceDefinitionContext.Builder()
            .withGroup("tekton.dev")
            .withVersion("v1beta1")
            .withName("pipelineruns.tekton.dev")
            .withKind("PipelineRun")
            .withPlural("pipelineruns")
            .withScope("Namespaced")
            .build();

    private static final String PIPELINE_REASON = "reason";
    private static final String PIPELINE_MESSAGE = "message";
    private static final String PIPELINE_SUCCEEDED = "Succeeded";

    @Inject
    DecisionVersionService versionService;

    @Inject
    KubernetesClient client;

    public void createOrUpdate(DecisionVersion version) {
        try {
            JsonObject expected = PipelineService.build(client.getNamespace(), version);
            JsonObject pipelineRuns = Json.createObjectBuilder(client.customResource(PIPELINE_RUN_CONTEXT)
                    .list(client.getNamespace(), Map.of(
                            DECISION_VERSION_LABEL, version.getMetadata().getName(),
                            DECISION_NAMESPACE_LABEL, version.getMetadata().getNamespace())))
                    .build();
            JsonArray items = pipelineRuns.getJsonArray("items");
            JsonObject run = null;
            if (!items.isEmpty()) {
                LOGGER.debug("PipelineRun exists for this decisionVersion {}. Skipping...", version.getMetadata().getName());
                Optional<JsonValue> recentBuild = items.stream().max(Comparator.comparing(v -> ResourceUtils.fromInstant(v.asJsonObject().getJsonObject("status").getString("startTime"))));
                if (recentBuild.isPresent()) {
                    run = recentBuild.get().asJsonObject();
                }
            }
            if (run == null) {
                LOGGER.debug("PipelineRun doesn't exist for this decisionVersion {}. Create it.", version.getMetadata().getName());
                run = Json.createObjectBuilder(client
                        .customResource(PIPELINE_RUN_CONTEXT)
                        .create(client.getNamespace(), expected.toString()))
                        .build();
            }
            updateBuildStatus(version, run);
        } catch (KubernetesClientException | IOException e) {
            LOGGER.warn("Unable to process Pipeline Run for DecisionVersion {}", version.getMetadata().getName(), e);
            versionService.setBuildStatus(version, Boolean.FALSE, REASON_FAILED, e.getMessage());
        }
    }

    public void delete(DecisionVersion version) {
        try {
            if (!client.customResource(PIPELINE_RUN_CONTEXT).list(client.getNamespace(), Map.of(OWNER_UID_LABEL, version.getMetadata().getUid())).isEmpty()) {
                LOGGER.debug("Cleaning up PipelineRun with name {} for DecisionVersion {}", getPipelineRunName(version), version.getMetadata().getName());
                client.customResource(PIPELINE_RUN_CONTEXT).delete(client.getNamespace(), getPipelineRunName(version));
            } else {
                LOGGER.debug("Missing PipelineRun with name {} for DecisionVersion {}. Ignoring.", getPipelineRunName(version), version.getMetadata().getName());
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to clean up PipelineRun with name {} for DecisionVersion {}", getPipelineRunName(version), version.getMetadata().getName(), e);
        }
    }

    private void updateBuildStatus(DecisionVersion version, JsonObject pipelineRun) {
        version.getStatus().setPipelineRef(getName(pipelineRun));
        JsonObject succeeded = getCondition(pipelineRun, PIPELINE_SUCCEEDED);
        if (succeeded == null) {
            return;
        }
        String reason = succeeded.getString(PIPELINE_REASON);
        if (PIPELINE_SUCCEEDED.equals(reason)) {
            versionService.setBuildCompleted(version, PipelineService.buildImageRef(version));
        } else {
            versionService.setBuildStatus(
                    version,
                    Boolean.FALSE,
                    succeeded.getString(PIPELINE_REASON),
                    succeeded.getString(PIPELINE_MESSAGE));
        }
    }

    private static JsonObject build(String namespace, DecisionVersion version) {
        return Json.createObjectBuilder()
                .add("apiVersion", PIPELINE_RUN_CONTEXT.getGroup() + "/" + PIPELINE_RUN_CONTEXT.getVersion())
                .add("kind", PIPELINE_RUN_CONTEXT.getKind())
                .add("metadata", Json.createObjectBuilder()
                        .add("name", getPipelineRunName(version))
                        .add("namespace", namespace)
                        .add("labels", Json.createObjectBuilder()
                                .add(BAAAS_RESOURCE_LABEL, BAAAS_RESOURCE_PIPELINE_RUN)
                                .add(DECISION_VERSION_LABEL, version.getMetadata().getName())
                                .add(DECISION_LABEL, version.getMetadata().getLabels().get(DECISION_LABEL))
                                .add(CUSTOMER_LABEL, version.getMetadata().getLabels().get(CUSTOMER_LABEL))
                                .add(DECISION_NAMESPACE_LABEL, version.getMetadata().getNamespace())
                                .add(OWNER_UID_LABEL, version.getMetadata().getUid())
                                .add(MANAGED_BY_LABEL, OPERATOR_NAME)
                                .build())
                        .build())
                .add("spec", Json.createObjectBuilder()
                        .add("pipelineRef", Json.createObjectBuilder()
                                .add("name", PIPELINE_REF)
                                .build())
                        .add("params", Json.createArrayBuilder()
                                .add(buildEnvValue(VAR_POM_CONFIGMAP, getPomConfigMapName(version.getSpec())))
                                .add(buildEnvValue(VAR_PROPS_CONFIGMAP, getPropsConfigMapName(version.getSpec())))
                                .add(buildEnvValue(VAR_DMN_LOCATION, version.getSpec().getSource().toString()))
                                .add(buildEnvValue(VAR_REGISTRY_LOCATION, buildImageRef(version)))
                                .build())
                        .build())
                .build();
    }

    private static String getPipelineRunName(DecisionVersion version) {
        return version.getMetadata().getLabels().get(CUSTOMER_LABEL) + "-" + version.getMetadata().getName();
    }

    private static String getPomConfigMapName(DecisionVersionSpec spec) {
        if (spec.getKafka() != null) {
            return KAFKA_POM_XML_CONFIGMAP;
        }
        return POM_XML_CONFIGMAP;
    }

    private static String getPropsConfigMapName(DecisionVersionSpec spec) {
        if (spec.getKafka() != null) {
            return KAFKA_APP_PROPS_CONFIGMAP;
        }
        return APP_PROPS_CONFIGMAP;
    }

    private static String buildImageRef(DecisionVersion version) {
        return String.format(IMAGE_REF_TEMPLATE,
                version.getMetadata().getNamespace(),
                version.getMetadata().getLabels().get(DECISION_LABEL),
                version.getSpec().getVersion());
    }

}
