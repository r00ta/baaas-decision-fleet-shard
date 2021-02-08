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
package org.kie.baaas.ccp.service;

import java.io.IOException;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.api.DecisionVersionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.baaas.ccp.controller.DecisionController.DECISION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionController.DECISION_NAMESPACE_LABEL;
import static org.kie.baaas.ccp.controller.DecisionRequestController.CUSTOMER_LABEL;
import static org.kie.baaas.ccp.controller.DecisionVersionController.DECISION_VERSION_LABEL;
import static org.kie.baaas.ccp.service.JsonResourceUtils.buildParam;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getCondition;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getConditions;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getLabels;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getName;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getNamespace;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getStatus;

@ApplicationScoped
public class PipelineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineService.class);

    private static final String PIPELINE_REF = "baaas-ccp-decision-build";
    private static final String VAR_POM_CONFIGMAP = "BUILD_INPUT_POM_XML_CONFIGMAP";
    private static final String VAR_DMN_LOCATION = "BUILD_INPUT_DMN_LOCATION";
    private static final String VAR_REGISTRY_LOCATION = "BUILD_OUTPUT_REGISTRY_PUSH_LOCATION";
    private static final String KAFKA_POM_XML_CONFIGMAP = "baaas-ccp-build-pom-kafka-xml";
    private static final String POM_XML_CONFIGMAP = "baaas-ccp-build-pom-xml";
    private static final String IMAGE_REF_TEMPLATE = "quay.io/%s/baaas-decision-builds:%s-%s-%s";

    public static final CustomResourceDefinitionContext PIPELINE_RUN_CONTEXT = new CustomResourceDefinitionContext
            .Builder()
            .withGroup("tekton.dev")
            .withVersion("v1beta1")
            .withName("pipelineruns.tekton.dev")
            .withKind("PipelineRun")
            .withPlural("pipelineruns")
            .withScope("Namespaced")
            .build();

    @Inject
    KubernetesClient client;

    @Inject
    DecisionVersionService versionService;

    public static String getPipelineRunName(DecisionVersion version) {
        return version.getMetadata().getLabels().get(CUSTOMER_LABEL) + "-" + version.getMetadata().getName();
    }

    public static JsonObject buildPipelineRun(String namespace, DecisionVersion version) {
        return Json.createObjectBuilder()
                .add("apiVersion", PIPELINE_RUN_CONTEXT.getGroup() + "/" + PIPELINE_RUN_CONTEXT.getVersion())
                .add("kind", PIPELINE_RUN_CONTEXT.getKind())
                .add("metadata", Json.createObjectBuilder()
                        .add("name", getPipelineRunName(version))
                        .add("namespace", namespace)
                        .add("labels", Json.createObjectBuilder()
                                .add(DECISION_VERSION_LABEL, version.getMetadata().getName())
                                .add(DECISION_LABEL, version.getMetadata().getLabels().get(DECISION_LABEL))
                                .add(CUSTOMER_LABEL, version.getMetadata().getLabels().get(CUSTOMER_LABEL))
                                .add(DECISION_NAMESPACE_LABEL, version.getMetadata().getNamespace())
                                .build())
                        .build())
                .add("spec", Json.createObjectBuilder()
                        .add("pipelineRef", Json.createObjectBuilder()
                                .add("name", PIPELINE_REF)
                                .build())
                        .add("params", Json.createArrayBuilder()
                                .add(buildParam(VAR_POM_CONFIGMAP, getConfigMapName(version.getSpec())))
                                .add(buildParam(VAR_DMN_LOCATION, version.getSpec().getSource().toString()))
                                .add(buildParam(VAR_REGISTRY_LOCATION, buildImageRef(version)))
                                .build())
                        .add("podTemplate", Json.createObjectBuilder()
                                .add("securityContext", Json.createObjectBuilder()
                                        .add("fsGroup", 1001)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public void watchRuns() {
        try {
            LOGGER.debug("Registering PipelineRun watcher");
            client.customResource(PIPELINE_RUN_CONTEXT).watch(client.getNamespace(), new Watcher<>() {
                @Override
                public void eventReceived(Action action, String resource) {
                    if (!action.equals(Action.ADDED) && !action.equals(Action.MODIFIED)) {
                        LOGGER.debug("Ignore PipelineRun action {}", action);
                        return;
                    }
                    try (JsonReader reader = Json.createReader(new StringReader(resource))) {
                        JsonObject run = getLatestRun(reader.readObject());
                        if (run == null) {
                            return;
                        }
                        LOGGER.debug("Updated PipelineRun {}", getName(run));
                        if (getConditions(run) == null) {
                            return;
                        }
                        JsonObject succeeded = getCondition(run, "Succeeded");
                        if (succeeded != null) {
                            JsonObject labels = getLabels(run);
                            DecisionVersion version = client.customResources(DecisionVersion.class)
                                    .inNamespace(labels.getString(DECISION_NAMESPACE_LABEL))
                                    .withName(labels.getString(DECISION_VERSION_LABEL))
                                    .get();
                            if (version != null) {
                                boolean status = Boolean.parseBoolean(succeeded.getString("status"));
                                if (status) {
                                    versionService.setBuildCompleted(version, PipelineService.buildImageRef(version));
                                } else {
                                    versionService.setBuildStatus(
                                            version,
                                            Boolean.FALSE,
                                            succeeded.getString("reason"),
                                            succeeded.getString("message")
                                    );
                                }
                            }
                        }
                    }
                }


                @Override
                public void onClose(WatcherException cause) {
                    // Do nothing
                }

            });
        } catch (IOException e) {
            LOGGER.error("Unable to watch PipelineRun objects", e);
        }

    }

    private JsonObject getLatestRun(JsonObject run) {
        Map<String, String> labels = new HashMap<>();
        JsonObject labelsObj = getLabels(run);
        labelsObj.keySet().forEach(k -> labels.put(k, labelsObj.getString(k)));
        Map<String, Object> runs = client.customResource(PIPELINE_RUN_CONTEXT).list(getNamespace(run), labels);
        Optional<JsonObject> recent = Json.createObjectBuilder(runs).build().getJsonArray("items")
                .stream()
                .map(JsonValue::asJsonObject)
                .max(Comparator.comparing(r -> PipelineService.getStartTime(r.asJsonObject())));
        return recent.orElse(null);
    }

    public static String getConfigMapName(DecisionVersionSpec spec) {
        if (spec.getKafka() != null) {
            return KAFKA_POM_XML_CONFIGMAP;
        }
        return POM_XML_CONFIGMAP;
    }

    public static String buildImageRef(DecisionVersion version) {
        return String.format(IMAGE_REF_TEMPLATE,
                //TODO: changeme
                "ruben",
                version.getMetadata().getNamespace(),
                version.getMetadata().getLabels().get(DECISION_LABEL),
                version.getSpec().getVersion());
    }

    public static ZonedDateTime getStartTime(JsonObject run) {
        JsonObject status = getStatus(run);
        if (status == null) {
            return null;
        }
        if (!status.containsKey("startTime")) {
            return null;
        }
        return ZonedDateTime.parse(status.getString("startTime"));
    }

}
