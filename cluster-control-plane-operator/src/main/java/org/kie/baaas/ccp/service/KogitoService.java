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
package org.kie.baaas.ccp.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.baaas.ccp.controller.DecisionController.DECISION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionRequestController.CUSTOMER_LABEL;
import static org.kie.baaas.ccp.controller.DecisionVersionController.DECISION_VERSION_LABEL;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getConditionStatus;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getConditions;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getLabel;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getName;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getNamespace;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getStatus;

@ApplicationScoped
public class KogitoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KogitoService.class);
    private static final int REPLICAS = 1;

    public static final CustomResourceDefinitionContext KOGITO_RUNTIME_CONTEXT = new CustomResourceDefinitionContext
            .Builder()
            .withGroup("app.kiegroup.org")
            .withVersion("v1beta1")
            .withName("kogitoruntimes.app.kiegroup.org")
            .withKind("KogitoRuntime")
            .withPlural("kogitoruntimes")
            .withScope("Namespaced")
            .build();

    @Inject
    KubernetesClient client;

    @Inject
    DecisionVersionService versionService;

    public static String getServiceName(DecisionVersion version) {
        return version.getMetadata().getLabels().get(DECISION_LABEL);
    }

    public static JsonObject buildService(DecisionVersion version) {
        return Json.createObjectBuilder()
                .add("apiVersion", KOGITO_RUNTIME_CONTEXT.getGroup() + "/" + KOGITO_RUNTIME_CONTEXT.getVersion())
                .add("kind", KOGITO_RUNTIME_CONTEXT.getKind())
                .add("metadata", Json.createObjectBuilder()
                        .add("name", getServiceName(version))
                        .add("namespace", version.getMetadata().getNamespace())
                        .add("labels", Json.createObjectBuilder()
                                .add(DECISION_VERSION_LABEL, version.getMetadata().getName())
                                .add(DECISION_LABEL, version.getMetadata().getLabels().get(DECISION_LABEL))
                                .add(CUSTOMER_LABEL, version.getMetadata().getLabels().get(CUSTOMER_LABEL))
                                .build())
                        .build())
                .add("spec", Json.createObjectBuilder()
                        .add("image", version.getStatus().getImageRef())
                        .add("replicas", REPLICAS)
                        .build())
                .build();
    }

    public void watchRuntimes() {
        try {
            LOGGER.debug("Registering KogitoRuntimes watcher");
            client.customResource(KOGITO_RUNTIME_CONTEXT).watch(new Watcher<>() {
                @Override
                public void eventReceived(Action action, String resource) {
                    if (!action.equals(Action.ADDED) && !action.equals(Action.MODIFIED)) {
                        LOGGER.debug("Ignore KogitoRuntime action {}", action);
                        return;
                    }
                    try (JsonReader reader = Json.createReader(new StringReader(resource))) {
                        JsonObject runtime = reader.readObject();
                        LOGGER.debug("Updated KogitoRuntime {}", getName(runtime));
                        JsonArray conditions = getConditions(runtime);
                        if (conditions == null || getLabel(runtime, DECISION_VERSION_LABEL) == null) {
                            return;
                        }
                        DecisionVersion version = client.customResources(DecisionVersion.class)
                                .inNamespace(getNamespace(runtime))
                                .withName(getLabel(runtime, DECISION_VERSION_LABEL))
                                .get();
                        if (version == null) {
                            return;
                        }
                        boolean provisioning = getConditionStatus(runtime, "Provisioning");
                        boolean deployed = getConditionStatus(runtime, "Deployed");
                        String reason = provisioning ? "Provisioning" : "Unknown";
                        Boolean status = Boolean.FALSE;
                        if (deployed) {
                            reason = "Deployed";
                            status = Boolean.TRUE;
                            version = versionService.setReadyStatus(version, URI.create(getStatus(runtime).getString("externalURI")));
                        }
                        versionService.setServiceStatus(version, status, reason, "");

                    }
                }

                @Override
                public void onClose(WatcherException cause) {
                    //Do nothing
                }

            });
        } catch (IOException e) {
            LOGGER.error("Unable to watch KogitoRuntime objects", e);
        }
    }

}
