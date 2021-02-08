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

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.Condition;
import io.fabric8.kubernetes.api.model.ConditionBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.kie.baaas.ccp.api.Decision;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.api.DecisionVersionStatus;
import org.kie.baaas.ccp.api.Phase;
import org.kie.baaas.ccp.api.ResourceUtils;
import org.kie.baaas.ccp.api.Webhook;
import org.kie.baaas.ccp.api.WebhookBuilder;
import org.kie.baaas.ccp.client.RemoteResourceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.baaas.ccp.api.DecisionVersionStatus.CONDITION_BUILD;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.CONDITION_CURRENT;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.CONDITION_READY;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.CONDITION_SERVICE;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.REASON_FAILED;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.REASON_SUCCESS;
import static org.kie.baaas.ccp.controller.DecisionController.DECISION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionRequestController.CUSTOMER_LABEL;

@ApplicationScoped
public class DecisionVersionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionVersionService.class);

    @Inject
    RemoteResourceClient resourceClient;

    @Inject
    KubernetesClient kubernetesClient;

    public void setBuildCompleted(DecisionVersion version, String imageRef) {
        version.getStatus().setImageRef(imageRef);
        setBuildStatus(version, Boolean.TRUE, REASON_SUCCESS, "");
    }

    public DecisionVersion setBuildStatus(DecisionVersion version, Boolean status, String reason, String message) {
        DecisionVersion currentVersion = initializeStatus(version);
        Condition current = currentVersion.getStatus().getCondition(CONDITION_BUILD);
        Condition expected = new ConditionBuilder()
                .withType(CONDITION_BUILD)
                .withMessage(message)
                .withReason(reason)
                .withStatus(ResourceUtils.capitalize(status))
                .build();
        if (current != null) {
            expected.setLastTransitionTime(current.getLastTransitionTime());
        }
        if (!expected.equals(current)) {
            expected.setLastTransitionTime(ResourceUtils.now());
            currentVersion.getStatus().setCondition(CONDITION_BUILD, expected);
            currentVersion = kubernetesClient.customResources(DecisionVersion.class)
                    .inNamespace(currentVersion.getMetadata().getNamespace())
                    .updateStatus(currentVersion);
            notify(CONDITION_BUILD, currentVersion);
        }
        return currentVersion;
    }

    public DecisionVersion setPipelineRef(DecisionVersion version, String pipelineRef) {
        DecisionVersion currentVersion = initializeStatus(version);
        currentVersion.getStatus().setPipelineRef(pipelineRef);
        return kubernetesClient.customResources(DecisionVersion.class)
                .inNamespace(currentVersion.getMetadata().getNamespace())
                .updateStatus(currentVersion);
    }

    public DecisionVersion setCurrentStatus(DecisionVersion version, Boolean isCurrent) {
        DecisionVersion currentVersion = initializeStatus(version);
        Condition current = currentVersion.getStatus().getCondition(CONDITION_CURRENT);
        Condition expected = new ConditionBuilder()
                .withType(CONDITION_CURRENT)
                .withStatus(ResourceUtils.capitalize(isCurrent))
                .build();
        if (current != null) {
            expected.setLastTransitionTime(current.getLastTransitionTime());
        }
        if (!expected.equals(current)) {
            expected.setLastTransitionTime(ResourceUtils.now());
            currentVersion.getStatus().setCondition(CONDITION_CURRENT, expected);
            currentVersion = kubernetesClient.customResources(DecisionVersion.class)
                    .inNamespace(currentVersion.getMetadata().getNamespace())
                    .updateStatus(currentVersion);
            notify(CONDITION_CURRENT, currentVersion);
        }
        return currentVersion;
    }

    public DecisionVersion setReadyStatus(DecisionVersion version, URI endpoint) {
        DecisionVersion currentVersion = kubernetesClient.customResources(DecisionVersion.class)
                .inNamespace(version.getMetadata().getNamespace())
                .withName(version.getMetadata().getName())
                .get();
        Condition current = currentVersion.getStatus().getCondition(CONDITION_READY);
        Condition expected = new ConditionBuilder()
                .withType(CONDITION_READY)
                .withReason(REASON_SUCCESS)
                .withStatus(ResourceUtils.capitalize(Boolean.TRUE))
                .build();
        if (current != null) {
            expected.setLastTransitionTime(current.getLastTransitionTime());
        }
        if (!expected.equals(current)) {
            expected.setLastTransitionTime(new Date().toString());
            currentVersion.getStatus().setCondition(CONDITION_READY, expected);
            DecisionVersion updatedVersion = kubernetesClient.customResources(DecisionVersion.class)
                    .inNamespace(currentVersion.getMetadata().getNamespace())
                    .updateStatus(currentVersion);
            setDecisionEndpoint(updatedVersion, endpoint);
            notify(CONDITION_READY, version);
        }
        return currentVersion;
    }

    public DecisionVersion setKogitoSvcRef(DecisionVersion version, String kogitoServiceRef) {
        version.getStatus().setKogitoServiceRef(kogitoServiceRef);
        return kubernetesClient.customResources(DecisionVersion.class)
                .inNamespace(version.getMetadata().getNamespace())
                .updateStatus(version);
    }

    public DecisionVersion setServiceStatus(DecisionVersion version, Boolean status, String reason, String message) {
        DecisionVersion currentVersion = kubernetesClient.customResources(DecisionVersion.class)
                .inNamespace(version.getMetadata().getNamespace())
                .withName(version.getMetadata().getName())
                .get();
        Condition current = currentVersion.getStatus().getCondition(CONDITION_SERVICE);
        Condition expected = new ConditionBuilder()
                .withType(CONDITION_SERVICE)
                .withMessage(message)
                .withReason(reason)
                .withStatus(ResourceUtils.capitalize(status))
                .build();
        if (current != null) {
            expected.setLastTransitionTime(current.getLastTransitionTime());
        }
        if (!expected.equals(current)) {
            expected.setLastTransitionTime(new Date().toString());
            currentVersion.getStatus().setCondition(CONDITION_SERVICE, expected);
            currentVersion = kubernetesClient.customResources(DecisionVersion.class)
                    .inNamespace(version.getMetadata().getNamespace())
                    .updateStatus(currentVersion);
            notify(CONDITION_SERVICE, currentVersion);
        }
        return currentVersion;
    }

    private void setDecisionEndpoint(DecisionVersion version, URI endpoint) {
        Decision decision = getDecision(version);
        if (decision != null) {
            decision.getStatus().setEndpoint(endpoint);
            kubernetesClient.customResources(Decision.class).inNamespace(decision.getMetadata().getNamespace()).updateStatus(decision);
        }
    }

    private Collection<URI> getWebhooks(Decision decision) {
        if (decision == null) {
            return Collections.emptyList();
        }
        return decision.getSpec().getWebhooks();
    }

    private Decision getDecision(DecisionVersion version) {
        return kubernetesClient.customResources(Decision.class)
                .inNamespace(version.getMetadata().getNamespace())
                .withName(version.getMetadata().getLabels().get(DECISION_LABEL))
                .get();
    }

    private void notify(String conditionType, DecisionVersion version) {
        CompletableFuture.runAsync(() -> {
            Condition condition = version.getStatus().getCondition(conditionType);
            if (!CONDITION_READY.equals(conditionType) && !REASON_FAILED.equals(condition.getReason())) {
                return;
            }
            WebhookBuilder webhookBuilder = new WebhookBuilder().withCustomer(version.getMetadata().getLabels().get(CUSTOMER_LABEL))
                    .withDecision(version.getMetadata().getLabels().get(DECISION_LABEL))
                    .withVersion(version.getSpec().getVersion())
                    .withNamespace(version.getMetadata().getNamespace())
                    .withVersionResource(version.getMetadata().getName())
                    .withAt(ResourceUtils.now())
                    .withMessage(condition.getMessage());
            Decision decision = getDecision(version);
            Collection<URI> webhooks = getWebhooks(decision);
            if (CONDITION_READY.equals(conditionType)) {
                Phase phase = Phase.CURRENT;
                if (!Objects.equals(
                        decision.getSpec().getDefinition().getVersion(),
                        version.getSpec().getVersion())) {
                    phase = Phase.READY;
                }
                webhookBuilder.withPhase(phase).withEndpoint(decision.getStatus().getEndpoint());
            } else if (REASON_FAILED.equals(condition.getReason())) {
                webhookBuilder.withPhase(Phase.FAILED);
            }
            Webhook webhook = webhookBuilder.build();
            webhooks.forEach(u -> resourceClient.notify(webhook, u));
        });
    }


    private DecisionVersion initializeStatus(DecisionVersion version) {
        if (version.getStatus() == null) {
            version.setStatus(new DecisionVersionStatus().setReady(Boolean.FALSE));
        }
        return kubernetesClient.customResources(DecisionVersion.class)
                .inNamespace(version.getMetadata().getNamespace())
                .updateStatus(version);
    }

}
