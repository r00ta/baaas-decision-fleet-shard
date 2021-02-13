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
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

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
import org.kie.baaas.ccp.api.ResourceUtils;
import org.kie.baaas.ccp.service.DecisionVersionService;
import org.kie.baaas.ccp.service.KogitoService;
import org.kie.baaas.ccp.service.PipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.baaas.ccp.api.DecisionVersionStatus.REASON_FAILED;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_NAMESPACE_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_VERSION_LABEL;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getCondition;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getName;
import static org.kie.baaas.ccp.service.KogitoService.KOGITO_RUNTIME_CONTEXT;
import static org.kie.baaas.ccp.service.PipelineService.PIPELINE_MESSAGE;
import static org.kie.baaas.ccp.service.PipelineService.PIPELINE_REASON;
import static org.kie.baaas.ccp.service.PipelineService.PIPELINE_RUN_CONTEXT;
import static org.kie.baaas.ccp.service.PipelineService.PIPELINE_SUCCEEDED;

@Controller(namespaces = ControllerConfiguration.WATCH_ALL_NAMESPACES_MARKER)
@ApplicationScoped
public class DecisionVersionController implements ResourceController<DecisionVersion> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionVersionController.class);

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    DecisionVersionService versionService;

    @Inject
    PipelineService pipelineService;

    @Inject
    KogitoService kogitoService;

    private GenericResourceEventSource kogitoRuntimeEventSource;

    private GenericResourceEventSource pipelineRunEventSource;

    @Override
    public void init(EventSourceManager eventSourceManager) {
        this.kogitoRuntimeEventSource = GenericResourceEventSource.createAndRegisterWatch(kubernetesClient, KOGITO_RUNTIME_CONTEXT);
        this.pipelineRunEventSource = GenericResourceEventSource.createAndRegisterWatch(kubernetesClient, PIPELINE_RUN_CONTEXT);
        eventSourceManager.registerEventSource("pipeline-run-event-source", this.pipelineRunEventSource);
        eventSourceManager.registerEventSource("kogito-runtime-event-source", this.kogitoRuntimeEventSource);
    }

    public DeleteControl deleteResource(DecisionVersion version, Context<DecisionVersion> context) {
        LOGGER.info("Create or update DecisionVersion: {} in namespace {}", version.getMetadata().getName(), version.getMetadata().getNamespace());
        try {
            kubernetesClient.customResource(PIPELINE_RUN_CONTEXT).delete(kubernetesClient.getNamespace(), PipelineService.getPipelineRunName(version));
        } catch (KubernetesClientException e) {
            if (e.getCode() == 404) {
                LOGGER.debug("PipelineRun was already deleted. Ignoring");
            } else {
                LOGGER.error("Unable to clean up PipelineRun for Version: {}", version.getMetadata().getName(), e);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to clean up PipelineRun for Version: {}", version.getMetadata().getName(), e);
        }
        return DeleteControl.DEFAULT_DELETE;
    }

    public UpdateControl<DecisionVersion> createOrUpdateResource(DecisionVersion version, Context<DecisionVersion> context) {
        LOGGER.info("Create or update DecisionVersion: {} in namespace {}", version.getMetadata().getName(), version.getMetadata().getNamespace());
        createOrUpdatePipelineRun(version);
        kogitoService.createOrUpdateService(version);
        return versionService.updateStatus(version);
    }

    private void createOrUpdatePipelineRun(DecisionVersion version) {
        try {
            JsonObject expected = PipelineService.buildPipelineRun(kubernetesClient.getNamespace(), version);
            JsonObject pipelineRuns = Json.createObjectBuilder(kubernetesClient.customResource(PIPELINE_RUN_CONTEXT)
                    .list(kubernetesClient.getNamespace(), Map.of(
                            DECISION_VERSION_LABEL, version.getMetadata().getName(),
                            DECISION_NAMESPACE_LABEL, version.getMetadata().getNamespace())))
                    .build();
            JsonArray items = pipelineRuns.getJsonArray("items");
            JsonObject run = null;
            if (!items.isEmpty()) {
                LOGGER.debug("PipelineRun exists for this decisionVersion. Skipping...");
                Optional<JsonValue> recentBuild = items.stream().max(Comparator.comparing(v -> ResourceUtils.fromInstant(v.asJsonObject().getJsonObject("status").getString("startTime"))));
                if (recentBuild.isPresent()) {
                    run = recentBuild.get().asJsonObject();
                }
            }
            if (run == null) {
                LOGGER.debug("PipelineRun doesn't exist for this decisionVersion. Create it.");
                run = Json.createObjectBuilder(kubernetesClient
                        .customResource(PIPELINE_RUN_CONTEXT)
                        .create(kubernetesClient.getNamespace(), expected.toString()))
                        .build();
            }
            updateBuildStatus(version, run);
        } catch (KubernetesClientException | IOException e) {
            LOGGER.warn("Unable to process Pipeline Run", e);
            versionService.setBuildStatus(version, Boolean.FALSE, REASON_FAILED, e.getMessage());
        }
    }

    public void updateBuildStatus(DecisionVersion version, JsonObject pipelineRun) {
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
                    succeeded.getString(PIPELINE_MESSAGE)
            );
        }
    }

}
