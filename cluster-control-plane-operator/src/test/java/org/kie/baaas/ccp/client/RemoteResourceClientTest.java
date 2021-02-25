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
package org.kie.baaas.ccp.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.VerificationException;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.baaas.ccp.api.Decision;
import org.kie.baaas.ccp.api.DecisionBuilder;
import org.kie.baaas.ccp.api.DecisionRequest;
import org.kie.baaas.ccp.api.DecisionRequestBuilder;
import org.kie.baaas.ccp.api.DecisionRequestSpec;
import org.kie.baaas.ccp.api.DecisionSpec;
import org.kie.baaas.ccp.api.DecisionStatus;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.api.DecisionVersionBuilder;
import org.kie.baaas.ccp.api.DecisionVersionSpec;
import org.kie.baaas.ccp.api.Phase;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.baaas.ccp.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_LABEL;
import static org.wildfly.common.Assert.assertFalse;

@QuarkusTest
class RemoteResourceClientTest {

    @Inject
    RemoteResourceClient client;

    private static WireMockServer mockServer = new WireMockServer(wireMockConfig().dynamicPort());

    @BeforeAll
    static void start() {
        mockServer.start();
    }

    @AfterAll
    static void stop() {
        mockServer.stop();
    }

    @BeforeEach
    void resetMock() {
        mockServer.resetAll();
    }

    @Test
    void testNotifyDecisionRequestNoWebhooks() throws InterruptedException, ExecutionException, TimeoutException {
        DecisionRequest request = new DecisionRequestBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("req1").build())
                .withSpec(new DecisionRequestSpec().setName("decision-name"))
                .build();
        client.notify(request, "some message", Phase.FAILED);
        CompletableFuture<Boolean> verification = CompletableFuture.supplyAsync(() -> {
            while (true) {
                try {
                    return !mockServer.findAllUnmatchedRequests().isEmpty();
                } catch (VerificationException e) {
                    //Ignore and retry
                }
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    return Boolean.FALSE;
                }
            }
        });
        assertFalse(verification.get(2, TimeUnit.SECONDS));
    }

    @Test
    void testNotifyBadRequest() throws InterruptedException, ExecutionException, TimeoutException {
        Collection<URI> webhooks = new ArrayList<>();
        List<String> callbacks = List.of("/callback1");
        mockServer.stubFor(post(callbacks.get(0)).willReturn(aResponse().withStatus(404)));
        webhooks.add(URI.create("http://localhost:" + mockServer.port() + callbacks.get(0)));

        DecisionRequest request = new DecisionRequestBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("req1").build())
                .withSpec(new DecisionRequestSpec()
                        .setCustomerId("thecustomer")
                        .setName("decision-name")
                        .setWebhooks(webhooks))
                .build();
        client.notify(request, "some message", Phase.FAILED);
        CompletableFuture<Boolean> verification = CompletableFuture.supplyAsync(() -> {
                    while (true) {
                        try {
                            callbacks.forEach(s -> mockServer.verify(postRequestedFor(urlEqualTo(s))
                                    .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON))
                                    .withRequestBody(matchingJsonPath("$.decision", containing(request.getSpec().getName())))
                                    .withRequestBody(matchingJsonPath("$.customer", containing(request.getSpec().getCustomerId())))
                                    .withRequestBody(matchingJsonPath("$.message", containing("some message")))
                                    .withRequestBody(matchingJsonPath("$.phase", containing(Phase.FAILED.name())))
                                    .withRequestBody(matchingJsonPath("$.namespace", absent()))
                                    .withRequestBody(matchingJsonPath("$.endpoint", absent()))
                                    .withRequestBody(matchingJsonPath("$.version", absent()))
                                    .withRequestBody(matchingJsonPath("$.version_resource", absent()))
                            ));
                            return Boolean.TRUE;
                        } catch (VerificationException e) {
                            //Ignore and retry
                        }
                        try {
                            Thread.sleep(200L);
                        } catch (InterruptedException e) {
                            return Boolean.FALSE;
                        }
                    }
                }
        );
        assertTrue(verification.get(20, TimeUnit.SECONDS));
        assertThat(mockServer.findAllUnmatchedRequests(), empty());
    }

    @Test
    void testNotifyDecisionRequestWebhooks() throws InterruptedException, ExecutionException, TimeoutException {
        Collection<URI> webhooks = new ArrayList<>();
        List<String> callbacks = List.of("/callback1", "/callback2", "/callback3");
        callbacks.forEach(s -> {
            mockServer.stubFor(post(s).willReturn(aResponse().withStatus(200)));
            webhooks.add(URI.create("http://localhost:" + mockServer.port() + s));
        });
        DecisionRequest request = new DecisionRequestBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("req1").build())
                .withSpec(new DecisionRequestSpec()
                        .setCustomerId("thecustomer")
                        .setName("decision-name")
                        .setWebhooks(webhooks))
                .build();
        client.notify(request, "some message", Phase.FAILED);
        CompletableFuture<Boolean> verification = CompletableFuture.supplyAsync(() -> {
                    while (true) {
                        try {
                            callbacks.forEach(s -> mockServer.verify(postRequestedFor(urlEqualTo(s))
                                    .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON))
                                    .withRequestBody(matchingJsonPath("$.decision", containing(request.getSpec().getName())))
                                    .withRequestBody(matchingJsonPath("$.customer", containing(request.getSpec().getCustomerId())))
                                    .withRequestBody(matchingJsonPath("$.message", containing("some message")))
                                    .withRequestBody(matchingJsonPath("$.phase", containing(Phase.FAILED.name())))
                                    .withRequestBody(matchingJsonPath("$.namespace", absent()))
                                    .withRequestBody(matchingJsonPath("$.endpoint", absent()))
                                    .withRequestBody(matchingJsonPath("$.version", absent()))
                                    .withRequestBody(matchingJsonPath("$.version_resource", absent()))
                            ));
                            return Boolean.TRUE;
                        } catch (VerificationException e) {
                            //Ignore and retry
                        }
                        try {
                            Thread.sleep(200L);
                        } catch (InterruptedException e) {
                            return Boolean.FALSE;
                        }
                    }
                }
        );
        assertTrue(verification.get(20, TimeUnit.SECONDS));
        assertThat(mockServer.findAllUnmatchedRequests(), empty());
    }

    @Test
    void testNotifyDecisionWebhooks() throws InterruptedException, ExecutionException, TimeoutException {
        Collection<URI> webhooks = new ArrayList<>();
        List<String> callbacks = List.of("/callback1", "/callback2", "/callback3");
        callbacks.forEach(s -> {
            mockServer.stubFor(post(s).willReturn(aResponse().withStatus(200)));
            webhooks.add(URI.create("http://localhost:" + mockServer.port() + s));
        });
        Decision decision = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("mydecision")
                        .withNamespace("baaas-thecustomer")
                        .addToLabels(CUSTOMER_LABEL, "thecustomer").build())
                .withSpec(new DecisionSpec()
                        .setDefinition(new DecisionVersionSpec().setVersion("1").setSource(URI.create("some-source")))
                        .setWebhooks(webhooks))
                .withStatus(new DecisionStatus()
                        .setEndpoint(URI.create("http://mydecision.example.com"))
                        .setVersionId("1"))
                .build();
        client.notify(decision, "mydecision-1", "some message", Phase.CURRENT);
        CompletableFuture<Boolean> verification = CompletableFuture.supplyAsync(() -> {
                    while (true) {
                        try {
                            callbacks.forEach(s -> mockServer.verify(postRequestedFor(urlEqualTo(s))
                                    .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON))
                                    .withRequestBody(matchingJsonPath("$.decision", containing(decision.getMetadata().getName())))
                                    .withRequestBody(matchingJsonPath("$.message", containing("some message")))
                                    .withRequestBody(matchingJsonPath("$.customer", containing(decision.getMetadata().getLabels().get(CUSTOMER_LABEL))))
                                    .withRequestBody(matchingJsonPath("$.phase", containing(Phase.CURRENT.name())))
                                    .withRequestBody(matchingJsonPath("$.namespace", containing(decision.getMetadata().getNamespace())))
                                    .withRequestBody(matchingJsonPath("$.endpoint", containing(decision.getStatus().getEndpoint().toString())))
                                    .withRequestBody(matchingJsonPath("$.version", containing(decision.getSpec().getDefinition().getVersion())))
                                    .withRequestBody(matchingJsonPath("$.version_resource", containing("mydecision-1")))
                            ));
                            return Boolean.TRUE;
                        } catch (VerificationException e) {
                            //Ignore and retry
                        }
                        try {
                            Thread.sleep(200L);
                        } catch (InterruptedException e) {
                            return Boolean.FALSE;
                        }
                    }
                }
        );
        assertTrue(verification.get(20, TimeUnit.SECONDS));
        assertThat(mockServer.findAllUnmatchedRequests(), empty());
    }

    @Test
    void testNotifyDecisionVersionWebhooks() throws InterruptedException, ExecutionException, TimeoutException {
        Collection<URI> webhooks = new ArrayList<>();
        List<String> callbacks = List.of("/callback1", "/callback2", "/callback3");
        callbacks.forEach(s -> {
            mockServer.stubFor(post(s).willReturn(aResponse().withStatus(200)));
            webhooks.add(URI.create("http://localhost:" + mockServer.port() + s));
        });
        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("mydecision-1")
                        .withNamespace("baaas-thecustomer")
                        .addToLabels(CUSTOMER_LABEL, "thecustomer")
                        .addToLabels(DECISION_LABEL, "mydecision")
                        .build())

                .withSpec(new DecisionVersionSpec().setVersion("1").setSource(URI.create("some-source")))
                .build();
        client.notify(version, webhooks, "some message", Phase.FAILED);
        CompletableFuture<Boolean> verification = CompletableFuture.supplyAsync(() -> {
                    while (true) {
                        try {
                            callbacks.forEach(s -> mockServer.verify(postRequestedFor(urlEqualTo(s))
                                    .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON))
                                    .withRequestBody(matchingJsonPath("$.decision", containing(version.getMetadata().getLabels().get(DECISION_LABEL))))
                                    .withRequestBody(matchingJsonPath("$.message", containing("some message")))
                                    .withRequestBody(matchingJsonPath("$.customer", containing(version.getMetadata().getLabels().get(CUSTOMER_LABEL))))
                                    .withRequestBody(matchingJsonPath("$.phase", containing(Phase.FAILED.name())))
                                    .withRequestBody(matchingJsonPath("$.namespace", containing(version.getMetadata().getNamespace())))
                                    .withRequestBody(matchingJsonPath("$.endpoint", absent()))
                                    .withRequestBody(matchingJsonPath("$.version", containing(version.getSpec().getVersion())))
                                    .withRequestBody(matchingJsonPath("$.version_resource", containing(version.getMetadata().getName())))
                            ));
                            return Boolean.TRUE;
                        } catch (VerificationException e) {
                            //Ignore and retry
                        }
                        try {
                            Thread.sleep(200L);
                        } catch (InterruptedException e) {
                            return Boolean.FALSE;
                        }
                    }
                }
        );
        assertTrue(verification.get(20, TimeUnit.SECONDS));
        assertThat(mockServer.findAllUnmatchedRequests(), empty());
    }
}
