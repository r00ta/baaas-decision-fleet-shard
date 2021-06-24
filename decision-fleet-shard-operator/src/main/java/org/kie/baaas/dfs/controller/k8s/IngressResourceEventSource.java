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
package org.kie.baaas.dfs.controller.k8s;

import java.util.List;

import org.kie.baaas.dfs.model.NetworkResourceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;

import static org.kie.baaas.dfs.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.OPERATOR_NAME;

public class IngressResourceEventSource extends AbstractEventSource implements Watcher<Ingress> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngressResourceEventSource.class);

    private final KubernetesClient client;

    public static IngressResourceEventSource createAndRegisterWatch(KubernetesClient client) {
        IngressResourceEventSource eventSource = new IngressResourceEventSource(client);
        eventSource.registerWatch();
        return eventSource;
    }

    private IngressResourceEventSource(KubernetesClient client) {
        this.client = client;
    }

    private void registerWatch() {
        client.network().v1().ingresses().inAnyNamespace().withLabel(MANAGED_BY_LABEL, OPERATOR_NAME).watch(this);
    }

    @Override
    public void eventReceived(Action action, Ingress ingress) {
        if (eventHandler == null) {
            LOGGER.warn("Ignoring action {} for resource ingress. EventHandler has not yet been initialized.", action);
            return;
        }

        LOGGER.info(
                "Event received for action: {}, {}: {}",
                action.name(),
                "Ingress",
                ingress.getMetadata().getName());

        if (action == Action.ERROR) {
            LOGGER.warn(
                    "Skipping {} event for {} uid: {}, version: {}",
                    action,
                    "Ingress",
                    ingress.getMetadata().getUid(),
                    ingress.getMetadata().getResourceVersion());
            return;
        }

        List<OwnerReference> ownerReferences = ingress.getMetadata().getOwnerReferences();
        if (!ownerReferences.isEmpty()) {
            String ownerUid = ownerReferences.get(0).getUid();
            LOGGER.debug("Handling event for {} uid: {}, ownerUid: {}, version: {}",
                    "Ingress",
                    ingress.getMetadata().getUid(),
                    ownerUid,
                    ingress.getMetadata().getResourceVersion());
            LOGGER.debug(ingress.toString());
            eventHandler.handleEvent(new NetworkResourceEvent(action, ownerUid, this));
        } else {
            LOGGER.warn("Unable to retrieve Owner UID. Ignoring event {} {}/{}", ingress.getMetadata().getNamespace(),
                    ingress.getKind(), ingress.getMetadata().getName());
        }
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
