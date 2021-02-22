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

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.model.DecisionVersionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.javaoperatorsdk.operator.processing.KubernetesResourceUtils.getUID;
import static io.javaoperatorsdk.operator.processing.KubernetesResourceUtils.getVersion;
import static org.kie.baaas.ccp.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.OPERATOR_NAME;

public class DecisionVersionEventSource extends AbstractEventSource implements Watcher<DecisionVersion> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionVersionEventSource.class);

    private final KubernetesClient client;

    public static DecisionVersionEventSource createAndRegisterWatch(KubernetesClient client) {
        DecisionVersionEventSource eventSource = new DecisionVersionEventSource(client);
        eventSource.registerWatch();
        return eventSource;
    }

    private DecisionVersionEventSource(KubernetesClient client) {
        this.client = client;
    }

    private void registerWatch() {
        client.customResources(DecisionVersion.class)
                .inAnyNamespace()
                .withLabel(MANAGED_BY_LABEL, OPERATOR_NAME)
                .watch(this);
    }

    @Override
    public void eventReceived(Action action, DecisionVersion resource) {
        if(eventHandler == null) {
            LOGGER.warn("Ignoring action {} for resource {}. EventHandler has not yet been initialized.", action, resource);
            return;
        }
        LOGGER.info(
                "Event received for action: {}, DecisionVersion: {} (ready={})",
                action.name(),
                resource.getMetadata().getName(),
                resource.getStatus().isReady());

        if (action == Action.ERROR) {
            LOGGER.warn(
                    "Skipping {} event for custom resource uid: {}, version: {}",
                    action,
                    getUID(resource),
                    getVersion(resource));
            return;
        }
        eventHandler.handleEvent(new DecisionVersionEvent(action, resource, this));
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
