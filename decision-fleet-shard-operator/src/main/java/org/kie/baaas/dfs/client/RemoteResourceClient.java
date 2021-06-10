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

package org.kie.baaas.dfs.client;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.kie.baaas.dfs.api.Decision;
import org.kie.baaas.dfs.api.DecisionRequest;
import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.api.Phase;
import org.kie.baaas.dfs.api.ResourceUtils;
import org.kie.baaas.dfs.api.Webhook;
import org.kie.baaas.dfs.api.WebhookBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.baaas.dfs.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_LABEL;

@ApplicationScoped
public class RemoteResourceClient {

    private Client client = ResteasyClientBuilder.newClient();

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteResourceClient.class);

    public void notify(DecisionRequest request, String message, Phase phase) {
        CompletableFuture.runAsync(() -> {
            Webhook webhook = new WebhookBuilder().withCustomer(request.getSpec().getCustomerId())
                    .withDecision(request.getSpec().getName())
                    .withAt(ResourceUtils.now())
                    .withMessage(message)
                    .withPhase(phase)
                    .build();
            notify(webhook, request.getSpec().getWebhooks());
        });
    }

    public void notify(Decision decision, DecisionVersion version, String message, Phase phase) {
        CompletableFuture.runAsync(() -> {
            Webhook webhook = new WebhookBuilder().withCustomer(decision.getMetadata().getLabels().get(CUSTOMER_LABEL))
                    .withDecision(decision.getMetadata().getName())
                    .withAt(ResourceUtils.now())
                    .withMessage(message)
                    .withNamespace(decision.getMetadata().getNamespace())
                    .withPhase(phase)
                    .withVersionResource(version.getMetadata().getName())
                    .withVersion(decision.getStatus().getVersionId())
                    .withEndpoint(version.getStatus().getEndpoint())
                    .withCurrentEndpoint(decision.getStatus().getEndpoint())
                    .withVersionEndpoint(version.getStatus().getEndpoint())
                    .build();
            notify(webhook, decision.getSpec().getWebhooks());
        });
    }

    public void notify(DecisionVersion version, Collection<URI> webhooks, String message, Phase phase) {
        CompletableFuture.runAsync(() -> {
            Webhook webhook = new WebhookBuilder().withCustomer(version.getMetadata().getLabels().get(CUSTOMER_LABEL))
                    .withDecision(version.getMetadata().getLabels().get(DECISION_LABEL))
                    .withAt(ResourceUtils.now())
                    .withMessage(message)
                    .withVersion(version.getSpec().getVersion())
                    .withEndpoint(version.getStatus().getEndpoint())
                    .withVersionEndpoint(version.getStatus().getEndpoint())
                    .withNamespace(version.getMetadata().getNamespace())
                    .withVersionResource(version.getMetadata().getName())
                    .withPhase(phase)
                    .build();
            notify(webhook, webhooks);
        });
    }

    private void notify(Webhook event, Collection<URI> endpoints) {
        if (endpoints == null) {
            return;
        }
        endpoints.forEach(e -> {
            Response response = client.target(e).request(MediaType.APPLICATION_JSON).post(Entity.json(event));
            if (response.getStatus() < Response.Status.BAD_REQUEST.getStatusCode()) {
                LOGGER.debug("Successfully emitted webhook to URI: {} with payload {}", e, Entity.json(event));
                return;
            }
            LOGGER.warn("Unable to emit webhook to URI: {} with payload {}. Received: {}", e, Entity.json(event), response.getStatus());
        });
    }
}
