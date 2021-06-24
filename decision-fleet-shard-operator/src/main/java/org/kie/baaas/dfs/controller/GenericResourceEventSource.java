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

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.kie.baaas.dfs.model.GenericResourceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;

import static org.kie.baaas.dfs.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.OPERATOR_NAME;
import static org.kie.baaas.dfs.controller.DecisionLabels.OWNER_UID_LABEL;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getLabel;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getName;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getNamespace;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getOwnerUid;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getResourceVersion;
import static org.kie.baaas.dfs.service.JsonResourceUtils.getUID;
import static org.kie.baaas.dfs.service.JsonResourceUtils.isManagedByController;

public class GenericResourceEventSource extends AbstractEventSource implements Watcher<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericResourceEventSource.class);

    private final KubernetesClient client;
    private final CustomResourceDefinitionContext context;

    public static GenericResourceEventSource createAndRegisterWatch(KubernetesClient client, CustomResourceDefinitionContext context) {
        GenericResourceEventSource eventSource = new GenericResourceEventSource(client, context);
        eventSource.registerWatch();
        return eventSource;
    }

    private GenericResourceEventSource(KubernetesClient client, CustomResourceDefinitionContext context) {
        this.client = client;
        this.context = context;
    }

    private void registerWatch() {
        try {
            client.customResource(context).watch(null, null, Map.of(MANAGED_BY_LABEL, OPERATOR_NAME), (String) null, this);
        } catch (IOException e) {
            LOGGER.error("Unable to register watcher for {}", context.getName(), e);
        }

    }

    @Override
    public void eventReceived(Action action, String resource) {
        if (eventHandler == null) {
            LOGGER.warn("Ignoring action {} for resource {}. EventHandler has not yet been initialized.", action, resource);
            return;
        }
        try (JsonReader reader = Json.createReader(new StringReader(resource))) {
            JsonObject object = reader.readObject();
            LOGGER.info(
                    "Event received for action: {}, {}: {}",
                    action.name(),
                    context.getName(),
                    getName(object));

            if (action == Action.ERROR) {
                LOGGER.warn(
                        "Skipping {} event for {} uid: {}, version: {}",
                        action,
                        context.getName(),
                        getUID(object),
                        getResourceVersion(object));
                return;
            }
            if (!isManagedByController(object)) {
                LOGGER.info("Ignoring event for not owned resource {} uid: {}", context.getName(), getUID(object));
                return;
            }
            String ownerUid = getOwnerUid(object);
            if (Objects.equals(client.getNamespace(), getNamespace(object))) {
                ownerUid = getLabel(object, OWNER_UID_LABEL);
            }
            if (ownerUid == null) {
                LOGGER.warn("Unable to retrieve Owner UID. Ignoring event {} {}/{}", getNamespace(object), object.getString("kind"), getName(object));
            } else {
                LOGGER.debug("Handling event for {} uid: {}, ownerUid: {}, version: {}",
                        context.getName(),
                        getUID(object),
                        ownerUid,
                        getResourceVersion(object));
                eventHandler.handleEvent(new GenericResourceEvent(action, ownerUid, object, this));
            }
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
