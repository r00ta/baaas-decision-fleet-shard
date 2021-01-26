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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import org.kie.baaas.api.Decision;
import org.kie.baaas.api.DecisionRevision;
import org.kie.baaas.api.DecisionRevisionBuilder;
import org.kie.baaas.api.DecisionRevisionSpec;
import org.kie.baaas.api.DecisionRevisionStatusBuilder;
import org.kie.baaas.api.DecisionStatusBuilder;
import org.kie.baaas.api.Phase;
import org.kie.baaas.ccp.service.DecisionRevisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@ApplicationScoped
public class DecisionController implements ResourceController<Decision> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionController.class);
    public static final String DECISION_LABEL = "org.kie.baaas.decision";

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    DecisionRevisionService decisionRevisionService;

    public DeleteControl deleteResource(Decision decision, Context<Decision> context) {
        LOGGER.info("Create or update Decision: {} in namespace {}", decision.getMetadata().getName(), decision.getMetadata().getNamespace());
        return DeleteControl.DEFAULT_DELETE;
    }

    public UpdateControl<Decision> createOrUpdateResource(Decision decision, Context<Decision> context) {
        LOGGER.info("Create or update Decision: {} in namespace {}", decision.getMetadata().getName(), decision.getMetadata().getNamespace());
        try {
            return createOrUpdateRevision(decision);
        } catch (KubernetesClientException e) {
            LOGGER.error("Unable to create or update DecisionRevision", e);
            return UpdateControl.noUpdate();
        }
    }

    private UpdateControl<Decision> createOrUpdateRevision(Decision decision) {
        DecisionRevision latest = decisionRevisionService.getLatest(decision.getMetadata().getNamespace(), decision.getMetadata().getName());
        Long id = 1L;
        if(latest != null) {
            id = latest.getSpec().getId();
        }
        DecisionRevision expected = new DecisionRevisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(decision.getMetadata().getName() + "-" + id)
                        .withNamespace(decision.getMetadata().getNamespace())
                        .addToLabels(DECISION_LABEL, decision.getMetadata().getName())
                        .withOwnerReferences(decision.getOwnerReference())
                        .build())
                .withSpec(DecisionRevisionSpec.build(id, decision.getMetadata().getName(), decision.getSpec()))
                .build();
        if (latest == null || !expected.getSpec().equals(latest.getSpec())) {
            if (latest != null) {
                expected.getMetadata().setName(decision.getMetadata().getName() + "-" + ++id);
                expected.getSpec().setId(id);
                replaceCurrentRevision(latest);
            }
            kubernetesClient.customResources(DecisionRevision.class)
                    .inNamespace(decision.getMetadata().getNamespace())
                    .create(expected);
            return updateDecisionStatus(decision, expected);
        }
        return UpdateControl.noUpdate();
    }

    private void replaceCurrentRevision(DecisionRevision current) {
        current.setStatus(new DecisionRevisionStatusBuilder().withPhase(Phase.REPLACED).build());
        kubernetesClient.customResources(DecisionRevision.class)
                .inNamespace(current.getMetadata().getNamespace())
                .withName(current.getMetadata().getName())
                .updateStatus(current);
    }

    private UpdateControl<Decision> updateDecisionStatus(Decision decision, DecisionRevision revision) {
        decision.setStatus(new DecisionStatusBuilder()
                .withRevisionId(revision.getSpec().getId())
                .withRevisionName(revision.getMetadata().getName())
                .build());
        return UpdateControl.updateStatusSubResource(decision);
    }
}
