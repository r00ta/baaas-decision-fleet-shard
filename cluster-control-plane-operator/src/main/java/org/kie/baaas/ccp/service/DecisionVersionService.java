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

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.Condition;
import io.fabric8.kubernetes.api.model.ConditionBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.UpdateControl;
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
import static org.kie.baaas.ccp.api.DecisionVersionStatus.REASON_PENDING;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.REASON_RUNNING;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.REASON_SUCCESS;
import static org.kie.baaas.ccp.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_LABEL;

@ApplicationScoped
public class DecisionVersionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionVersionService.class);

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    RemoteResourceClient resourceClient;

    public void setBuildCompleted(DecisionVersion version, String imageRef) {
        setBuildStatus(version, Boolean.TRUE, REASON_SUCCESS, "");
        if (!Objects.equals(version.getStatus().getImageRef(), imageRef)) {
            version.getStatus().setImageRef(imageRef);
        }
    }

    public void setBuildStatus(DecisionVersion version, Boolean status, String reason, String message) {
        setStatusCondition(version, new ConditionBuilder()
                .withType(CONDITION_BUILD)
                .withMessage(message)
                .withReason(reason)
                .withStatus(ResourceUtils.capitalize(status))
                .build());
    }

    public void setServiceStatus(DecisionVersion version, Boolean status, String reason, String message) {
        setStatusCondition(version, new ConditionBuilder()
                .withType(CONDITION_SERVICE)
                .withMessage(message)
                .withReason(reason)
                .withStatus(ResourceUtils.capitalize(status))
                .build());
    }

    public void setReadyStatus(DecisionVersion version) {
        setStatusCondition(version, new ConditionBuilder()
                .withType(CONDITION_READY)
                .withReason(REASON_SUCCESS)
                .withStatus(ResourceUtils.capitalize(Boolean.TRUE))
                .build());
    }

    private void setStatusCondition(DecisionVersion version, Condition condition) {
        if (version.getStatus() == null) {
            return;
        }
        Condition current = version.getStatus().getCondition(condition.getType());
        if (current != null) {
            condition.setLastTransitionTime(current.getLastTransitionTime());
        }
        if (!condition.equals(current)) {
            condition.setLastTransitionTime(new Date().toString());
            version.getStatus().setCondition(condition.getType(), condition);
            LOGGER.debug("Set status condition for {} to {}", version.getMetadata().getName(), condition);
        }
    }

    public UpdateControl<DecisionVersion> updateStatus(DecisionVersion version) {
        if (version.getStatus().getCondition(CONDITION_READY) == null) {
            version.getStatus().setReady(Boolean.FALSE);
        }
        DecisionVersion current = kubernetesClient.customResources(DecisionVersion.class)
                .inNamespace(version.getMetadata().getNamespace())
                .withName(version.getMetadata().getName())
                .get();
        if (current == null) {
            return UpdateControl.noUpdate();
        }
        DecisionVersionStatus currentStatus = current.getStatus();
        if (Objects.equals(currentStatus, version.getStatus())) {
            return UpdateControl.noUpdate();
        }
        Decision decision = kubernetesClient.customResources(Decision.class)
                .inNamespace(version.getMetadata().getNamespace())
                .withName(version.getMetadata().getLabels().get(DECISION_LABEL))
                .get();
        version.getStatus().getConditionValues()
                .stream()
                .filter(c -> currentStatus == null || !Objects.equals(c, currentStatus.getCondition(c.getType())))
                .filter(c -> !REASON_PENDING.equals(c.getReason()) && !REASON_RUNNING.equals(c.getReason()))
                .forEach(c -> notify(c, version, decision));
        current.setStatus(version.getStatus());
        return UpdateControl.updateStatusSubResource(current);

    }

    private void notify(Condition condition, DecisionVersion version, Decision decision) {
        if (decision == null || decision.getSpec().getWebhooks() == null || decision.getSpec().getWebhooks().isEmpty()) {
            return;
        }
        if (CONDITION_CURRENT.equals(condition.getType()) || (CONDITION_READY.equals(condition.getType()) && !Boolean.parseBoolean(condition.getStatus()))) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            WebhookBuilder webhookBuilder = new WebhookBuilder().withCustomer(version.getMetadata().getLabels().get(CUSTOMER_LABEL))
                    .withDecision(version.getMetadata().getLabels().get(DECISION_LABEL))
                    .withVersion(version.getSpec().getVersion())
                    .withNamespace(version.getMetadata().getNamespace())
                    .withVersionResource(version.getMetadata().getName())
                    .withAt(ResourceUtils.now())
                    .withMessage(condition.getMessage());

            if (CONDITION_READY.equals(condition.getType())) {
                Phase phase = Phase.READY;
                if (Boolean.parseBoolean(version.getStatus().getCondition(CONDITION_CURRENT).getStatus())) {
                    phase = Phase.CURRENT;
                }
                webhookBuilder.withPhase(phase).withEndpoint(decision.getStatus().getEndpoint());
            } else if (REASON_FAILED.equals(condition.getReason())) {
                webhookBuilder.withPhase(Phase.FAILED);
            }
            Webhook webhook = webhookBuilder.build();
            decision.getSpec().getWebhooks().forEach(u -> resourceClient.notify(webhook, u));
        });
    }

}
