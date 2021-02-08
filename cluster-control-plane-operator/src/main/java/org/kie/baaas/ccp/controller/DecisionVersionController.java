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

package org.kie.baaas.ccp.controller;

import java.io.IOException;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import io.javaoperatorsdk.operator.api.config.ControllerConfiguration;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.service.DecisionVersionService;
import org.kie.baaas.ccp.service.KogitoService;
import org.kie.baaas.ccp.service.PipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.baaas.ccp.api.DecisionVersionStatus.CONDITION_BUILD;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.CONDITION_CURRENT;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.REASON_FAILED;
import static org.kie.baaas.ccp.controller.DecisionController.DECISION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionController.DECISION_NAMESPACE_LABEL;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getSpec;
import static org.kie.baaas.ccp.service.KogitoService.KOGITO_RUNTIME_CONTEXT;
import static org.kie.baaas.ccp.service.PipelineService.PIPELINE_RUN_CONTEXT;

@Controller(namespaces = ControllerConfiguration.WATCH_ALL_NAMESPACES_MARKER)
@ApplicationScoped
public class DecisionVersionController implements ResourceController<DecisionVersion> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionVersionController.class);

    public static final String DECISION_VERSION_LABEL = "org.kie.baaas/decisionversion";

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    DecisionVersionService versionService;

    @Inject
    PipelineService pipelineService;

    @Inject
    KogitoService kogitoService;

    @Override
    public void init(EventSourceManager eventSourceManager) {
        pipelineService.watchRuns();
        kogitoService.watchRuntimes();
    }

    public DeleteControl deleteResource(DecisionVersion version, Context<DecisionVersion> context) {
        LOGGER.info("Create or update DecisionRevision: {} in namespace {}", version.getMetadata().getName(), version.getMetadata().getNamespace());
        try {
            kubernetesClient.customResource(PIPELINE_RUN_CONTEXT).delete(kubernetesClient.getNamespace(), PipelineService.getPipelineRunName(version));
        } catch (KubernetesClientException e) {
            if (e.getCode() == 404) {
                LOGGER.debug("PipelineRun was already deleted. Ignoring");
            } else {
                LOGGER.error("Unable to clean up PipelineRun for Version: " + version.getMetadata().getName(), e);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to clean up PipelineRun for Version: " + version.getMetadata().getName(), e);
        }
        return DeleteControl.DEFAULT_DELETE;
    }

    public UpdateControl<DecisionVersion> createOrUpdateResource(DecisionVersion version, Context<DecisionVersion> context) {
        LOGGER.info("Create or update DecisionRevision: {} in namespace {}", version.getMetadata().getName(), version.getMetadata().getNamespace());
        createPipelineRun(version);
        createKogitoService(version);
        return UpdateControl.noUpdate();
    }

    private void createPipelineRun(DecisionVersion version) {
        try {
            JsonObject expected = PipelineService.buildPipelineRun(kubernetesClient.getNamespace(), version);
            JsonObject pipelineRuns = Json.createObjectBuilder(kubernetesClient.customResource(PIPELINE_RUN_CONTEXT)
                    .list(kubernetesClient.getNamespace(), Map.of(
                            DECISION_VERSION_LABEL, version.getMetadata().getName(),
                            DECISION_NAMESPACE_LABEL, version.getMetadata().getNamespace())))
                    .build();
            if (!pipelineRuns.getJsonArray("items").isEmpty()) {
                LOGGER.debug("PipelineRun exists for this decisionVersion. Skipping...");
                return;
            }
            LOGGER.debug("PipelineRun doesn't exist for this decisionVersion. Create it.");
            kubernetesClient.customResource(PIPELINE_RUN_CONTEXT)
                    .create(kubernetesClient.getNamespace(), expected.toString());
            versionService.setPipelineRef(version, expected.getJsonObject("metadata").getString("name"));
        } catch (KubernetesClientException | IOException e) {
            LOGGER.warn("Unable to process Pipeline Run", e);
            versionService.setBuildStatus(version, Boolean.FALSE, REASON_FAILED, e.getMessage());
        }
    }

    private void createKogitoService(DecisionVersion version) {
        if (!isBuilt(version) || !Boolean.parseBoolean(version.getStatus().getCondition(CONDITION_CURRENT).getStatus())) {
            return;
        }
        LOGGER.info("Creating Kogito Runtime for version {}", version.getMetadata().getName());
        JsonObject expected = KogitoService.buildService(version);
        String name = expected.getJsonObject("metadata").getString("name");
        JsonObject current = null;
        try {
            current = Json.createObjectBuilder(kubernetesClient.customResource(KOGITO_RUNTIME_CONTEXT)
                    .get(version.getMetadata().getNamespace(), version.getMetadata().getLabels().get(DECISION_LABEL)))
                    .build();
        } catch (KubernetesClientException e) {
            LOGGER.debug("KogitoRuntime {} does not exist. Creating...", name);
        }
        if (needsUpdate(expected, current)) {
            try {
                kubernetesClient.customResource(KOGITO_RUNTIME_CONTEXT).createOrReplace(version.getMetadata().getNamespace(), expected.toString());
                versionService.setKogitoSvcRef(version, name);
            } catch (IOException e) {
                LOGGER.warn("Unable to process KogitoService", e);
                versionService.setServiceStatus(version, Boolean.FALSE, REASON_FAILED, e.getMessage());
            }
        }
    }

    private boolean isBuilt(DecisionVersion version) {
        return version.getStatus() != null
                && version.getStatus().getCondition(CONDITION_BUILD) != null
                && Boolean.parseBoolean(version.getStatus().getCondition(CONDITION_BUILD).getStatus());
    }

    private boolean needsUpdate(JsonObject expected, JsonObject current) {
        if (current == null) {
            return true;
        }
        JsonObject expectedSpec = getSpec(expected);
        JsonObject currentSpec = getSpec(current);
        return !expectedSpec.getString("image").equals(currentSpec.getString("image"))
                || expectedSpec.getInt("replicas") != currentSpec.getInt("replicas");
    }
}
