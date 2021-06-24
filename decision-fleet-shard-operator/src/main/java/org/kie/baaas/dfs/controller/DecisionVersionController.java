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
package org.kie.baaas.dfs.controller;

import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.service.DecisionVersionService;
import org.kie.baaas.dfs.service.KogitoService;
import org.kie.baaas.dfs.service.PipelineService;
import org.kie.baaas.dfs.service.networking.NetworkingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import io.javaoperatorsdk.operator.api.config.ControllerConfiguration;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;

import static org.kie.baaas.dfs.service.KogitoService.KOGITO_RUNTIME_CONTEXT;
import static org.kie.baaas.dfs.service.PipelineService.PIPELINE_RUN_CONTEXT;

@Controller(namespaces = ControllerConfiguration.WATCH_ALL_NAMESPACES_MARKER)
@ApplicationScoped
public class DecisionVersionController implements ResourceController<DecisionVersion> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionVersionController.class);

    private final ReentrantLock lock = new ReentrantLock();

    private GenericResourceEventSource kogitoRuntimeEventSource;

    private GenericResourceEventSource pipelineRunEventSource;

    private AbstractEventSource networkingEventSource;

    private EventSourceManager eventSourceManager;

    @Inject
    KubernetesClient client;

    @Inject
    DecisionVersionService versionService;

    @Inject
    PipelineService pipelineService;

    @Inject
    KogitoService kogitoService;

    @Inject
    NetworkingService networkingService;

    @Override
    public void init(EventSourceManager eventSourceManager) {
        lock.lock();
        try {
            this.eventSourceManager = eventSourceManager;
            this.kogitoRuntimeEventSource = GenericResourceEventSource.createAndRegisterWatch(client, KOGITO_RUNTIME_CONTEXT);
            eventSourceManager.registerEventSource("pipeline-run-event-source", this.kogitoRuntimeEventSource);
            this.pipelineRunEventSource = GenericResourceEventSource.createAndRegisterWatch(client, PIPELINE_RUN_CONTEXT);
            eventSourceManager.registerEventSource("kogito-runtime-event-source", this.pipelineRunEventSource);
            this.networkingEventSource = networkingService.createAndRegisterWatchNetworkingResource();
            eventSourceManager.registerEventSource("kogito-networking-event-source", this.networkingEventSource);
        } finally {
            lock.unlock();
        }
    }

    public DeleteControl deleteResource(DecisionVersion version, Context<DecisionVersion> context) {
        LOGGER.info("Delete DecisionVersion: {} in namespace {}", version.getMetadata().getName(), version.getMetadata().getNamespace());
        pipelineService.delete(version);
        networkingService.delete(version.getMetadata().getName(), version.getMetadata().getNamespace());
        eventSourceManager.deRegisterCustomResourceFromEventSource(getEventSourceName(version), version.getMetadata().getUid());
        return DeleteControl.DEFAULT_DELETE;
    }

    public UpdateControl<DecisionVersion> createOrUpdateResource(DecisionVersion version, Context<DecisionVersion> context) {
        LOGGER.info("Create or update DecisionVersion: {} in namespace {}", version.getMetadata().getName(), version.getMetadata().getNamespace());
        if (!eventSourceManager.getRegisteredEventSources().containsKey(getEventSourceName(version))) {
            eventSourceManager.registerEventSource(getEventSourceName(version), DecisionEventSource.createAndRegisterWatch(client, version));
        }
        pipelineService.createOrUpdate(version);
        kogitoService.createOrUpdate(version);
        return versionService.updateStatus(version);
    }

    private static String getEventSourceName(DecisionVersion version) {
        return "decision-event-source-" + version.getMetadata().getName();
    }
}
