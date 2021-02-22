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
import java.io.StringReader;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;
import org.kie.baaas.ccp.model.GenericResourceEvent;
import org.kie.baaas.ccp.service.JsonResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.baaas.ccp.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.OPERATOR_NAME;

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
                    JsonResourceUtils.getName(object));

            if (action == Action.ERROR) {
                LOGGER.warn(
                        "Skipping {} event for {} uid: {}, version: {}",
                        action,
                        context.getName(),
                        JsonResourceUtils.getUID(object),
                        JsonResourceUtils.getResourceVersion(object));
                return;
            }
            if (JsonResourceUtils.getOwnerUid(object) == null) {
                LOGGER.info("Ignoring event for not owned resource {} uid: {}", context.getName(), JsonResourceUtils.getUID(object));
                return;
            }
            LOGGER.debug("Handling event for {} uid: {}, ownerUid: {}, version: {}",
                    context.getName(),
                    JsonResourceUtils.getUID(object),
                    JsonResourceUtils.getOwnerUid(object),
                    JsonResourceUtils.getResourceVersion(object));
            eventHandler.handleEvent(new GenericResourceEvent(action, object, this));
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
