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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.ConditionBuilder;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.UpdateControl;
import io.quarkus.test.junit.QuarkusTest;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.kie.baaas.ccp.api.AdmissionStatus;
import org.kie.baaas.ccp.api.Decision;
import org.kie.baaas.ccp.api.DecisionBuilder;
import org.kie.baaas.ccp.api.DecisionConstants;
import org.kie.baaas.ccp.api.DecisionRequest;
import org.kie.baaas.ccp.api.DecisionRequestBuilder;
import org.kie.baaas.ccp.api.DecisionRequestSpec;
import org.kie.baaas.ccp.api.DecisionSpec;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.api.DecisionVersionBuilder;
import org.kie.baaas.ccp.api.DecisionVersionSpec;
import org.kie.baaas.ccp.api.DecisionVersionStatus;
import org.kie.baaas.ccp.api.Phase;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.CONDITION_BUILD;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.REASON_FAILED;
import static org.kie.baaas.ccp.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_REQUEST_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.OPERATOR_NAME;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@QuarkusTest
class DecisionRequestControllerTest extends AbstractControllerTest {

    @Inject
    DecisionRequestController decisionRequestController;

    @Test
    void testInvalidRequestValidationError() {
        //Given
        DecisionRequest request = new DecisionRequestBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-request")
                        .withNamespace(CONTROLLER_NS)
                        .build())
                .withSpec(new DecisionRequestSpec()
                        .setName("some-decision")
                        .setWebhooks(List.of(URI.create("somewebhook")))
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();
        assertThat(client.namespaces().withName(CUSTOMER_NS).get(), nullValue());

        //When
        UpdateControl<DecisionRequest> updateControl = decisionRequestController.createOrUpdateResource(request, null);

        //Then
        assertThat(client.namespaces().withName(CUSTOMER_NS).get(), nullValue());

        assertThat(updateControl.isUpdateStatusSubResource(), is(true));
        DecisionRequest updatedRequest = updateControl.getCustomResource();
        assertThat(updatedRequest.getStatus().getState(), is(AdmissionStatus.REJECTED));
        assertThat(updatedRequest.getStatus().getVersionRef(), nullValue());
        assertThat(updatedRequest.getStatus().getReason(), is(DecisionConstants.VALIDATION_ERROR));
        String expectedErrorMsg = "Invalid spec: customerId must not be blank";
        assertThat(updatedRequest.getStatus().getMessage(), is(expectedErrorMsg));

        assertThat(client.customResources(Decision.class).inNamespace(CUSTOMER_NS).list().getItems(), empty());
        Mockito.verify(remoteResourceClient, times(1)).notify(request, expectedErrorMsg, Phase.FAILED);
    }

    @Test
    void testRequestBuildFailed() {
        //Given
        DecisionRequest request = new DecisionRequestBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-request")
                        .withNamespace(CONTROLLER_NS)
                        .build())
                .withSpec(new DecisionRequestSpec()
                        .setCustomerId(CUSTOMER)
                        .setName("some-decision")
                        .setWebhooks(List.of(URI.create("somewebhook")))
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();
        assertThat(client.namespaces().withName(CUSTOMER_NS).get(), nullValue());
        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision-1")
                        .addToLabels(DECISION_LABEL, request.getSpec().getName())
                        .build())
                .withSpec(request.getSpec().getDefinition())
                .withStatus(new DecisionVersionStatus()
                        .setCondition(CONDITION_BUILD, new ConditionBuilder()
                                .withType(CONDITION_BUILD)
                                .withStatus(REASON_FAILED).build()))
                .build();
        client.customResources(DecisionVersion.class).inNamespace(CUSTOMER_NS).create(version);

        //When
        UpdateControl<DecisionRequest> updateControl = decisionRequestController.createOrUpdateResource(request, null);

        //Then
        assertThat(updateControl.isUpdateStatusSubResource(), is(true));
        DecisionRequest updatedRequest = updateControl.getCustomResource();
        assertThat(updatedRequest.getStatus().getState(), is(AdmissionStatus.REJECTED));
        assertThat(updatedRequest.getStatus().getVersionRef(), nullValue());
        assertThat(updatedRequest.getStatus().getReason(), is(DecisionConstants.VERSION_BUILD_FAILED));
        String expectedErrorMsg = "Requested DecisionVersion build failed";
        assertThat(updatedRequest.getStatus().getMessage(), is(expectedErrorMsg));

        assertThat(client.customResources(Decision.class).inNamespace(CUSTOMER_NS).list().getItems(), empty());
        Mockito.verify(remoteResourceClient, times(1)).notify(request, expectedErrorMsg, Phase.FAILED);
    }

    @Test
    void testRequestDuplicatedVersionError() {
        //Given
        DecisionRequest request = new DecisionRequestBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-request")
                        .withNamespace(CONTROLLER_NS)
                        .build())
                .withSpec(new DecisionRequestSpec()
                        .setCustomerId(CUSTOMER)
                        .setName("some-decision")
                        .setWebhooks(List.of(URI.create("somewebhook")))
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();
        assertThat(client.namespaces().withName(CUSTOMER_NS).get(), nullValue());
        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision-1")
                        .addToLabels(DECISION_LABEL, request.getSpec().getName())
                        .build())
                .withSpec(new DecisionVersionSpec().setVersion("1").setSource(URI.create("othersource")))
                .build();
        client.customResources(DecisionVersion.class).inNamespace(CUSTOMER_NS).create(version);

        //When
        UpdateControl<DecisionRequest> updateControl = decisionRequestController.createOrUpdateResource(request, null);

        //Then
        assertThat(updateControl.isUpdateStatusSubResource(), is(true));
        DecisionRequest updatedRequest = updateControl.getCustomResource();
        assertThat(updatedRequest.getStatus().getState(), is(AdmissionStatus.REJECTED));
        assertThat(updatedRequest.getStatus().getVersionRef(), nullValue());
        assertThat(updatedRequest.getStatus().getReason(), is(DecisionConstants.DUPLICATED_VERSION));
        String expectedErrorMsg = "The provided version already exists with a different spec";
        assertThat(updatedRequest.getStatus().getMessage(), is(expectedErrorMsg));

        assertThat(client.customResources(Decision.class).inNamespace(CUSTOMER_NS).list().getItems(), empty());
        Mockito.verify(remoteResourceClient, times(1)).notify(request, expectedErrorMsg, Phase.FAILED);
    }

    @Test
    void testInvalidRequestInvalidNamespace() {
        //Given
        DecisionRequest request = new DecisionRequestBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-request")
                        .withNamespace("controller-namespace")
                        .build())
                .withSpec(new DecisionRequestSpec()
                        .setCustomerId("Traffic-Violation")
                        .setName("some-decision")
                        .setWebhooks(List.of(URI.create("somewebhook")))
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();
        assertThat(client.namespaces().withName(CUSTOMER_NS).get(), nullValue());

        //When
        UpdateControl<DecisionRequest> updateControl = decisionRequestController.createOrUpdateResource(request, null);

        //Then
        assertThat(client.namespaces().withName(CUSTOMER_NS).get(), nullValue());

        assertThat(updateControl.isUpdateStatusSubResource(), is(true));
        DecisionRequest updatedRequest = updateControl.getCustomResource();
        assertThat(updatedRequest.getStatus().getState(), is(AdmissionStatus.REJECTED));
        assertThat(updatedRequest.getStatus().getVersionRef(), nullValue());
        assertThat(updatedRequest.getStatus().getReason(), is(DecisionConstants.VALIDATION_ERROR));
        String expectedErrorMsg = "Invalid target namespace: baaas-Traffic-Violation";
        assertThat(updatedRequest.getStatus().getMessage(), is(expectedErrorMsg));

        assertThat(client.customResources(Decision.class).inNamespace(CUSTOMER_NS).list().getItems(), empty());
        Mockito.verify(remoteResourceClient, times(1)).notify(request, expectedErrorMsg, Phase.FAILED);
    }

    @Test
    void testCreateNewRequest() {
        //Given
        DecisionRequest request = new DecisionRequestBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-request")
                        .withNamespace("controller-namespace")
                        .build())
                .withSpec(new DecisionRequestSpec()
                        .setCustomerId(CUSTOMER)
                        .setName("some-decision")
                        .setWebhooks(List.of(URI.create("somewebhook")))
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();
        assertThat(client.namespaces().withName(CUSTOMER_NS).get(), nullValue());

        //When
        UpdateControl<DecisionRequest> updateControl = decisionRequestController.createOrUpdateResource(request, null);

        //Then
        assertThat(client.namespaces().withName(CUSTOMER_NS).get(), notNullValue());

        assertThat(updateControl.isUpdateStatusSubResource(), is(true));
        DecisionRequest updatedRequest = updateControl.getCustomResource();
        assertThat(updatedRequest.getStatus().getState(), is(AdmissionStatus.SUCCESS));
        assertThat(updatedRequest.getStatus().getMessage(), nullValue());
        assertThat(updatedRequest.getStatus().getReason(), nullValue());
        assertThat(updatedRequest.getStatus().getVersionRef().getName(), is(request.getSpec().getName()));
        assertThat(updatedRequest.getStatus().getVersionRef().getNamespace(), is(CUSTOMER_NS));
        assertThat(updatedRequest.getStatus().getVersionRef().getVersion(), is(request.getSpec().getDefinition().getVersion()));
        Decision decision = client.customResources(Decision.class).inNamespace(CUSTOMER_NS).withName("some-decision").get();
        assertThat(decision.getMetadata().getName(), is("some-decision"));
        Map<String, String> expectedLabels = Map.of(
                MANAGED_BY_LABEL, OPERATOR_NAME,
                CUSTOMER_LABEL, request.getSpec().getCustomerId(),
                DECISION_REQUEST_LABEL, request.getMetadata().getName());
        expectedLabels.forEach((key, value) -> assertThat(decision.getMetadata().getLabels(), hasEntry(key, value)));
        assertThat(decision.getMetadata().getOwnerReferences(), empty());
        assertThat(decision.getSpec().getDefinition(), is(request.getSpec().getDefinition()));
        assertThat(decision.getSpec().getWebhooks(), is(request.getSpec().getWebhooks()));
    }

    @Test
    void testUnchangedRequest() throws InterruptedException {
        //Given
        DecisionRequest request = new DecisionRequestBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-request")
                        .withNamespace("controller-namespace")
                        .build())
                .withSpec(new DecisionRequestSpec()
                        .setCustomerId(CUSTOMER)
                        .setName("some-decision")
                        .setWebhooks(List.of(URI.create("somewebhook")))
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();
        assertThat(client.namespaces().withName(CUSTOMER_NS).get(), nullValue());
        Decision decision = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(request.getSpec().getName())
                        .withNamespace(CUSTOMER_NS)
                        .build())
                .withSpec(new DecisionSpec()
                        .setDefinition(request.getSpec().getDefinition())
                        .setWebhooks(request.getSpec().getWebhooks()))
                .build();
        client.namespaces().create(new NamespaceBuilder().withMetadata(new ObjectMetaBuilder().withName(CUSTOMER_NS).build()).build());
        client.customResources(Decision.class).inNamespace(CUSTOMER_NS).create(decision);

        //When
        UpdateControl<DecisionRequest> updateControl = decisionRequestController.createOrUpdateResource(request, null);

        //Then
        RecordedRequest lastRequest = server.getLastRequest();
        assertThat(lastRequest.getMethod(), is("GET"));
        assertThat(lastRequest.getPath(), is("/apis/operator.baaas/v1alpha1/namespaces/baaas-test/decisions/some-decision"));
        assertThat(client.namespaces().withName(CUSTOMER_NS).get(), notNullValue());

        assertThat(updateControl.isUpdateStatusSubResource(), is(true));
        DecisionRequest updatedRequest = updateControl.getCustomResource();
        assertThat(updatedRequest.getStatus().getState(), is(AdmissionStatus.SUCCESS));
        assertThat(updatedRequest.getStatus().getMessage(), nullValue());
        assertThat(updatedRequest.getStatus().getReason(), nullValue());
        assertThat(updatedRequest.getStatus().getVersionRef().getName(), is(request.getSpec().getName()));
        assertThat(updatedRequest.getStatus().getVersionRef().getNamespace(), is(CUSTOMER_NS));
        assertThat(updatedRequest.getStatus().getVersionRef().getVersion(), is(request.getSpec().getDefinition().getVersion()));
        Decision current = client.customResources(Decision.class).inNamespace(CUSTOMER_NS).withName("some-decision").get();
        assertThat(current.getMetadata().getName(), is("some-decision"));
        assertThat(current.getMetadata().getOwnerReferences(), empty());
        assertThat(current.getSpec().getDefinition(), is(request.getSpec().getDefinition()));
        assertThat(current.getSpec().getWebhooks(), is(request.getSpec().getWebhooks()));
    }
}
