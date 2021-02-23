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
import java.util.Map;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import io.fabric8.kubernetes.api.model.ConditionBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.UpdateControl;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.kie.baaas.ccp.api.Decision;
import org.kie.baaas.ccp.api.DecisionBuilder;
import org.kie.baaas.ccp.api.DecisionSpec;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.api.DecisionVersionBuilder;
import org.kie.baaas.ccp.api.DecisionVersionSpec;
import org.kie.baaas.ccp.api.DecisionVersionStatus;
import org.kie.baaas.ccp.api.ResourceUtils;
import org.kie.baaas.ccp.service.JsonResourceUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.CONDITION_BUILD;
import static org.kie.baaas.ccp.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.OPERATOR_NAME;
import static org.kie.baaas.ccp.service.KogitoService.KOGITO_RUNTIME_CONTEXT;

@QuarkusTest
class DecisionControllerTest extends AbstractControllerTest {

    @Inject
    DecisionController decisionController;

    @Test
    void testCreateNewVersion() {
        //Given
        Decision decision = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision")
                        .withNamespace(NAMESPACE)
                        .addToLabels(CUSTOMER_LABEL, "foo")
                        .build())
                .withSpec(new DecisionSpec()
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();

        //When
        UpdateControl<Decision> updateControl = decisionController.createOrUpdateResource(decision, null);

        //Then
        assertFalse(updateControl.isUpdateStatusSubResource());
        DecisionVersion version = client.customResources(DecisionVersion.class).inNamespace(NAMESPACE).withName("some-decision-1").get();
        assertThat(version.getMetadata().getName(), is("some-decision-1"));
        Map<String, String> expectedLabels = Map.of(DECISION_LABEL, decision.getMetadata().getName(), MANAGED_BY_LABEL, OPERATOR_NAME, CUSTOMER_LABEL, "foo");
        expectedLabels.forEach((key, value) -> assertThat(version.getMetadata().getLabels(), hasEntry(key, value)));
        assertThat(version.getMetadata().getOwnerReferences().size(), is(1));
        assertThat(version.getMetadata().getOwnerReferences().get(0).getName(), is(decision.getMetadata().getName()));
        assertThat(version.getSpec(), is(decision.getSpec().getDefinition()));
        assertThat(version.getStatus().isReady(), is("False"));
    }

    @Test
    void testReplaceVersion() {
        //Given
        Decision decision = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision")
                        .withNamespace(NAMESPACE)
                        .addToLabels(CUSTOMER_LABEL, "foo")
                        .build())
                .withSpec(new DecisionSpec()
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();
        DecisionVersion previous = new DecisionVersionBuilder().withMetadata(new ObjectMetaBuilder()
                .withName("some-decision-1")
                .withNamespace(NAMESPACE).build()).withStatus(new DecisionVersionStatus())
                .withStatus(new DecisionVersionStatus().setReady(Boolean.TRUE))
                .build();
        client.customResources(DecisionVersion.class).inNamespace(NAMESPACE).create(previous);

        //When
        UpdateControl<Decision> updateControl = decisionController.createOrUpdateResource(decision, null);

        //Then
        assertFalse(updateControl.isUpdateStatusSubResource());
        DecisionVersion version = client.customResources(DecisionVersion.class).inNamespace(NAMESPACE).withName("some-decision-1").get();
        assertThat(version.getMetadata().getName(), is("some-decision-1"));
        Map<String, String> expectedLabels = Map.of(DECISION_LABEL, decision.getMetadata().getName(), MANAGED_BY_LABEL, OPERATOR_NAME, CUSTOMER_LABEL, "foo");
        expectedLabels.forEach((key, value) -> assertThat(version.getMetadata().getLabels(), hasEntry(key, value)));
        assertThat(version.getMetadata().getOwnerReferences().size(), is(1));
        assertThat(version.getMetadata().getOwnerReferences().get(0).getName(), is(decision.getMetadata().getName()));
        assertThat(version.getSpec(), is(decision.getSpec().getDefinition()));
        assertThat(version.getStatus().isReady(), is("False"));
    }

    @Test
    void testChangeToMissingVersion() {
        //Given
        Decision decision = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision")
                        .withNamespace(NAMESPACE)
                        .addToLabels(CUSTOMER_LABEL, "foo")
                        .build())
                .withSpec(new DecisionSpec()
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();
        client.customResources(Decision.class).inNamespace(NAMESPACE).create(decision);
        DecisionVersion previous = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision-1")
                        .withNamespace(NAMESPACE)
                        .addToLabels(DECISION_LABEL, decision.getMetadata().getName())
                        .addToLabels(CUSTOMER_LABEL, decision.getMetadata().getLabels().get(CUSTOMER_LABEL))
                        .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                        .withOwnerReferences(decision.getOwnerReference())
                        .build())
                .withStatus(new DecisionVersionStatus())
                .withSpec(decision.getSpec().getDefinition())
                .build();
        previous = client.customResources(DecisionVersion.class).inNamespace(NAMESPACE).create(previous);
        previous.getStatus().setReady(Boolean.TRUE)
                .setCondition(CONDITION_BUILD, new ConditionBuilder().withType(CONDITION_BUILD).withStatus(ResourceUtils.capitalize(Boolean.TRUE)).build())
                .setImageRef("quay.io/baaas/foo-some-decision:1");
        client.customResources(DecisionVersion.class).inNamespace(NAMESPACE).updateStatus(previous);

        //When
        UpdateControl<Decision> updateControl = decisionController.createOrUpdateResource(decision, null);

        //Then
        assertFalse(updateControl.isUpdateStatusSubResource());
        DecisionVersion version = client.customResources(DecisionVersion.class).inNamespace(NAMESPACE).withName("some-decision-1").get();
        JsonObject kogitoSvc = Json.createObjectBuilder(client.customResource(KOGITO_RUNTIME_CONTEXT).list(NAMESPACE)).build().getJsonArray("items").getJsonObject(0);
        assertNotNull(kogitoSvc);
        assertThat(version.getMetadata().getName(), is("some-decision-1"));
        Map<String, String> expectedLabels = Map.of(DECISION_LABEL, decision.getMetadata().getName(), MANAGED_BY_LABEL, OPERATOR_NAME, CUSTOMER_LABEL, "foo");
        expectedLabels.forEach((key, value) -> assertThat(version.getMetadata().getLabels(), hasEntry(key, value)));
        assertThat(version.getMetadata().getOwnerReferences().size(), is(1));
        assertThat(version.getMetadata().getOwnerReferences().get(0).getName(), is(decision.getMetadata().getName()));
        assertThat(version.getSpec(), is(decision.getSpec().getDefinition()));
        assertThat(version.getStatus().isReady(), is("True"));
        assertThat(version.getStatus().getKogitoServiceRef(), is(JsonResourceUtils.getName(kogitoSvc)));
    }
}
