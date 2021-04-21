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

import org.kie.baaas.ccp.api.Decision;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.model.DecisionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;

import static io.javaoperatorsdk.operator.processing.KubernetesResourceUtils.getUID;
import static io.javaoperatorsdk.operator.processing.KubernetesResourceUtils.getVersion;
import static org.kie.baaas.ccp.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.OPERATOR_NAME;

/**
 * EventSource that DecisionVersions will register to track Decision changes that might affect the owned Version.
 */
public class DecisionEventSource extends AbstractEventSource implements Watcher<Decision> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionEventSource.class);

    private final KubernetesClient client;

    private final DecisionVersion version;

    public static DecisionEventSource createAndRegisterWatch(KubernetesClient client, DecisionVersion version) {
        DecisionEventSource eventSource = new DecisionEventSource(client, version);
        eventSource.registerWatch();
        return eventSource;
    }

    private DecisionEventSource(KubernetesClient client, DecisionVersion version) {
        this.client = client;
        this.version = version;
    }

    private void registerWatch() {
        client.customResources(Decision.class)
                .inAnyNamespace()
                .withLabel(MANAGED_BY_LABEL, OPERATOR_NAME)
                .withLabel(CUSTOMER_LABEL, version.getMetadata().getLabels().get(CUSTOMER_LABEL))
                .withField("metadata.name", version.getMetadata().getLabels().get(DECISION_LABEL))
                .watch(this);
    }

    @Override
    public void eventReceived(Action action, Decision resource) {
        if (eventHandler == null) {
            LOGGER.warn("Ignoring action {} for resource {}. EventHandler has not yet been initialized.", action, resource);
            return;
        }
        LOGGER.info(
                "Event received for action: {}, Decision: {}",
                action.name(),
                resource.getMetadata().getName());
        if (action == Action.ERROR) {
            LOGGER.warn(
                    "Skipping {} event for custom resource uid: {}, version: {}",
                    action,
                    getUID(resource),
                    getVersion(resource));
            return;
        }
        eventHandler.handleEvent(new DecisionEvent(action, version.getMetadata().getUid(), resource, this));
    }

    @Override
    public void onClose(WatcherException e) {
        if (e == null) {
            return;
        }
        if (e.isHttpGone()) {
            LOGGER.warn("Received error for watch, will try to reconnect.", e);
            registerWatch();
        } else {
            // Note that this should not happen normally, since fabric8 client handles reconnect.
            // In case it tries to reconnect this method is not called.
            LOGGER.error("Unexpected error happened with watch. Will exit.", e);
            System.exit(1);
        }
    }
}
