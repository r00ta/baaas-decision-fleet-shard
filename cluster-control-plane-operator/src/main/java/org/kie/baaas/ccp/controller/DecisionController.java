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

import java.net.URI;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import io.javaoperatorsdk.operator.api.config.ControllerConfiguration;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;
import org.kie.baaas.ccp.api.Decision;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.api.DecisionVersionBuilder;
import org.kie.baaas.ccp.service.DecisionVersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.baaas.ccp.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.OPERATOR_NAME;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getConditionStatus;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getStatus;
import static org.kie.baaas.ccp.service.KogitoService.KOGITO_RUNTIME_CONTEXT;

@Controller(namespaces = ControllerConfiguration.WATCH_ALL_NAMESPACES_MARKER)
@ApplicationScoped
public class DecisionController implements ResourceController<Decision> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionController.class);

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    DecisionVersionService versionService;

    private DecisionVersionEventSource decisionVersionEventSource;

    @Override
    public void init(EventSourceManager eventSourceManager) {
        this.decisionVersionEventSource = DecisionVersionEventSource.createAndRegisterWatch(kubernetesClient);
        eventSourceManager.registerEventSource("decision-version-event-source", this.decisionVersionEventSource);
    }

    public DeleteControl deleteResource(Decision decision, Context<Decision> context) {
        LOGGER.info("Create or update Decision: {} in namespace {}", decision.getMetadata().getName(), decision.getMetadata().getNamespace());
        return DeleteControl.DEFAULT_DELETE;
    }

    public UpdateControl<Decision> createOrUpdateResource(Decision decision, Context<Decision> context) {
        LOGGER.info("Create or update Decision: {} in namespace {}", decision.getMetadata().getName(), decision.getMetadata().getNamespace());
        return createOrUpdateDecisionVersion(decision);
    }

    private UpdateControl<Decision> createOrUpdateDecisionVersion(Decision decision) {
        String namespace = KubernetesResourceUtil.getNamespace(decision);
        DecisionVersion expected = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(decision.getMetadata().getName() + "-" + decision.getSpec().getDefinition().getVersion())
                        .withNamespace(namespace)
                        .addToLabels(DECISION_LABEL, decision.getMetadata().getName())
                        .addToLabels(CUSTOMER_LABEL, decision.getMetadata().getLabels().get(CUSTOMER_LABEL))
                        .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                        .withOwnerReferences(decision.getOwnerReference())
                        .build())
                .withSpec(decision.getSpec().getDefinition())
                .build();

        DecisionVersion version;
        try {
            version = kubernetesClient.customResources(DecisionVersion.class)
                    .inNamespace(namespace)
                    .withName(expected.getMetadata().getName())
                    .get();
        } catch (KubernetesClientException e) {
            LOGGER.error("Unable to retrieve DecisionVersion {}", expected.getMetadata().getName(), e);
            throw e;
        }
        if (version == null || !Objects.equals(expected.getSpec(), version.getSpec())) {
            version = kubernetesClient.customResources(DecisionVersion.class)
                    .inNamespace(expected.getMetadata().getNamespace())
                    .createOrReplace(expected);
        }
        if (Boolean.parseBoolean(version.getStatus().isReady())) {
            String kogitoServiceRef = version.getStatus().getKogitoServiceRef();
            JsonObject kogitoSvc = Json.createObjectBuilder(kubernetesClient
                    .customResource(KOGITO_RUNTIME_CONTEXT)
                    .get(namespace, kogitoServiceRef))
                    .build();
            boolean deployed = getConditionStatus(kogitoSvc, "Deployed");
            if (deployed) {
                decision.getStatus().setEndpoint(URI.create(getStatus(kogitoSvc).getString("externalURI")));
                decision.getStatus().setVersionId(version.getSpec().getVersion());
                return UpdateControl.updateStatusSubResource(decision);
            }
        }
        return UpdateControl.noUpdate();
    }
}
