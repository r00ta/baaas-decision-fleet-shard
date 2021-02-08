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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import io.javaoperatorsdk.operator.api.config.ControllerConfiguration;
import org.kie.baaas.ccp.api.Decision;
import org.kie.baaas.ccp.api.DecisionStatus;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.api.DecisionVersionBuilder;
import org.kie.baaas.ccp.service.DecisionVersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.baaas.ccp.controller.DecisionRequestController.CUSTOMER_LABEL;

@Controller(namespaces = ControllerConfiguration.WATCH_ALL_NAMESPACES_MARKER)
@ApplicationScoped
public class DecisionController implements ResourceController<Decision> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionController.class);
    public static final String DECISION_LABEL = "org.kie.baaas/decision";
    public static final String DECISION_NAMESPACE_LABEL = "org.kie.baaas/decisionnamespace";

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    DecisionVersionService versionService;

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
                        .withOwnerReferences(decision.getOwnerReference())
                        .build())
                .withSpec(decision.getSpec().getDefinition())
                .build();
        List<DecisionVersion> versions = kubernetesClient.customResources(DecisionVersion.class)
                .inNamespace(namespace)
                .withLabel(DECISION_LABEL, decision.getMetadata().getName())
                .list()
                .getItems();

        if (versions.stream().noneMatch(v -> expected.getMetadata().getName().equals(v.getMetadata().getName()))) {
            DecisionVersion current = kubernetesClient.customResources(DecisionVersion.class)
                    .inNamespace(expected.getMetadata().getNamespace())
                    .create(expected);
            versions.add(current);
        }
        if (decision.getStatus() == null) {
            decision.setStatus(new DecisionStatus());
        }
        decision.getStatus().setVersionId(expected.getSpec().getVersion());
        versions.forEach(v -> setCurrentVersion(decision, v));
        return UpdateControl.updateStatusSubResource(decision);
    }

    private void setCurrentVersion(Decision decision, DecisionVersion version) {
        boolean isCurrent = decision != null
                && decision.getSpec().getDefinition().getVersion().equals(version.getSpec().getVersion());
        versionService.setCurrentStatus(version, isCurrent);
    }

}
