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
package org.kie.baaas.dfs.controller.openshift;

import java.util.List;

import org.kie.baaas.dfs.model.NetworkResourceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;

import static org.kie.baaas.dfs.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.dfs.service.JsonResourceUtils.isManagedByController;

public class OpenshiftResourceEventSource extends AbstractEventSource implements Watcher<Route> {

    private static final Logger LOGGER = LoggerFactory.getLogger(org.kie.baaas.dfs.controller.k8s.IngressResourceEventSource.class);

    private final OpenShiftClient client;

    public static OpenshiftResourceEventSource createAndRegisterWatch(OpenShiftClient client) {
        OpenshiftResourceEventSource eventSource = new OpenshiftResourceEventSource(client);
        eventSource.registerWatch();
        return eventSource;
    }

    private OpenshiftResourceEventSource(OpenShiftClient client) {
        this.client = client;
    }

    private void registerWatch() {
        client.routes().inAnyNamespace().watch(this);
    }

    @Override
    public void eventReceived(Action action, Route route) {
        if (eventHandler == null) {
            LOGGER.warn("Ignoring action {} for resource ingress. EventHandler has not yet been initialized.", action);
            return;
        }

        LOGGER.info(
                "Event received for action: {}, {}: {}",
                action.name(),
                "Ingress",
                route.getMetadata().getName());

        if (action == Action.ERROR) {
            LOGGER.warn(
                    "Skipping {} event for {} uid: {}, version: {}",
                    action,
                    "Route",
                    route.getMetadata().getUid(),
                    route.getMetadata().getResourceVersion());
            return;
        }

        if (!isManagedByController(route.getMetadata().getLabels().getOrDefault(MANAGED_BY_LABEL, null))) {
            LOGGER.info("Ignoring event for not owned resource route uid: {}", route.getMetadata().getUid());
            return;
        }

        List<OwnerReference> ownerReferences = route.getMetadata().getOwnerReferences();
        if (!ownerReferences.isEmpty()) {
            String ownerUid = ownerReferences.get(0).getUid();
            LOGGER.debug("Handling event for {} uid: {}, ownerUid: {}, version: {}",
                    "Route",
                    route.getMetadata().getUid(),
                    ownerUid,
                    route.getMetadata().getResourceVersion());
            eventHandler.handleEvent(new NetworkResourceEvent(action, ownerUid, this));
        } else {
            LOGGER.warn("Unable to retrieve Owner UID. Ignoring event {} {}/{}", route.getMetadata().getNamespace(),
                    route.getKind(), route.getMetadata().getName());
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
