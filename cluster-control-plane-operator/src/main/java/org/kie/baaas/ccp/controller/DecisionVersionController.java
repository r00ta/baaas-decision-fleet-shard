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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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

import static org.kie.baaas.ccp.service.KogitoService.KOGITO_RUNTIME_CONTEXT;
import static org.kie.baaas.ccp.service.PipelineService.PIPELINE_RUN_CONTEXT;

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
        pipelineService.createOrUpdatePipelineRun(version);
        kogitoService.createOrUpdateService(version);
        return versionService.updateStatus(version);
    }
}
