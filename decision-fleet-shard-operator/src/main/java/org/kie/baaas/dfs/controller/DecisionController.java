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

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.baaas.dfs.api.Decision;
import org.kie.baaas.dfs.api.DecisionRequest;
import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.api.DecisionVersionBuilder;
import org.kie.baaas.dfs.api.Phase;
import org.kie.baaas.dfs.client.RemoteResourceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import io.javaoperatorsdk.operator.api.config.ControllerConfiguration;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;

import static io.fabric8.kubernetes.client.utils.KubernetesResourceUtil.getNamespace;
import static org.kie.baaas.dfs.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_REQUEST_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.OPERATOR_NAME;

@Controller(namespaces = ControllerConfiguration.WATCH_ALL_NAMESPACES_MARKER)
@ApplicationScoped
public class DecisionController implements ResourceController<Decision> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionController.class);

    @Inject
    KubernetesClient client;

    @Inject
    RemoteResourceClient resourceClient;

    @Override
    public void init(EventSourceManager eventSourceManager) {
        eventSourceManager.registerEventSource("decision-version-event-source", DecisionVersionEventSource.createAndRegisterWatch(client));
    }

    public DeleteControl deleteResource(Decision decision, Context<Decision> context) {
        LOGGER.info("Deleted Decision: {} in namespace {}", decision.getMetadata().getName(), decision.getMetadata().getNamespace());
        String requestName = decision.getMetadata().getLabels().get(DECISION_REQUEST_LABEL);
        LOGGER.info("Deleting DecisionRequest: {} in namespace {}", requestName, client.getNamespace());
        client.customResources(DecisionRequest.class).inNamespace(client.getNamespace()).withName(requestName).delete();
        return DeleteControl.DEFAULT_DELETE;
    }

    public UpdateControl<Decision> createOrUpdateResource(Decision decision, Context<Decision> context) {
        LOGGER.info("Create or update Decision: {} in namespace {}", decision.getMetadata().getName(), decision.getMetadata().getNamespace());
        return createOrUpdateDecisionVersion(decision);
    }

    private UpdateControl<Decision> createOrUpdateDecisionVersion(Decision decision) {
        String namespace = getNamespace(decision);
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
            version = client.customResources(DecisionVersion.class)
                    .inNamespace(namespace)
                    .withName(expected.getMetadata().getName())
                    .get();
        } catch (KubernetesClientException e) {
            LOGGER.error("Unable to retrieve DecisionVersion {}", expected.getMetadata().getName(), e);
            throw e;
        }

        if (version == null || !Objects.equals(expected.getSpec(), version.getSpec())) {
            version = client.customResources(DecisionVersion.class)
                    .inNamespace(namespace)
                    .createOrReplace(expected);
        }

        if (Boolean.parseBoolean(version.getStatus().isReady()) && version.getStatus().getKogitoServiceRef() != null) {
            // TODO: create or update the "current" route for the decision. See https://issues.redhat.com/browse/BAAAS-212
            // createOrUpdate route with version.getStatus().getKogitoServiceRef());
            // For the time being, let's return the version endpoint contained in the decisionVersion CRD.
            if (version.getStatus().getEndpoint() != null) {
                decision.getStatus().setEndpoint(version.getStatus().getEndpoint());
                resourceClient.notify(decision, version.getMetadata().getName(), null, Phase.CURRENT);
            }
            return UpdateControl.updateStatusSubResource(decision);
        }
        return UpdateControl.noUpdate();
    }
}
