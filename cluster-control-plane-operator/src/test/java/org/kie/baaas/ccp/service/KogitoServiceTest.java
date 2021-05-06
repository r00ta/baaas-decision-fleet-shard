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

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.junit.jupiter.api.Test;
import org.kie.baaas.ccp.api.Decision;
import org.kie.baaas.ccp.api.DecisionBuilder;
import org.kie.baaas.ccp.api.DecisionSpec;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.api.DecisionVersionBuilder;
import org.kie.baaas.ccp.api.DecisionVersionSpec;
import org.kie.baaas.ccp.api.DecisionVersionStatus;
import org.kie.baaas.ccp.api.Kafka;
import org.kie.baaas.ccp.api.ResourceUtils;
import org.kie.baaas.ccp.controller.AbstractControllerTest;
import org.kie.baaas.ccp.model.KogitoRuntime;

import io.fabric8.kubernetes.api.model.Condition;
import io.fabric8.kubernetes.api.model.ConditionBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.kie.baaas.ccp.api.DecisionVersionStatus.CONDITION_BUILD;
import static org.kie.baaas.ccp.controller.DecisionLabels.BAAAS_RESOURCE_KOGITO_SERVICE;
import static org.kie.baaas.ccp.controller.DecisionLabels.BAAAS_RESOURCE_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.OPERATOR_NAME;
import static org.kie.baaas.ccp.service.KogitoService.BAAAS_DASHBOARD_AUTH_SECRET;
import static org.kie.baaas.ccp.service.KogitoService.BAAAS_DASHBOARD_BOOTSTRAP_SERVERS;
import static org.kie.baaas.ccp.service.KogitoService.BAAAS_DASHBOARD_CLIENTID;
import static org.kie.baaas.ccp.service.KogitoService.BAAAS_DASHBOARD_CLIENTSECRET;
import static org.kie.baaas.ccp.service.KogitoService.BAAAS_KAFKA_BOOTSTRAP_SERVERS;
import static org.kie.baaas.ccp.service.KogitoService.BAAAS_KAFKA_CLIENTID;
import static org.kie.baaas.ccp.service.KogitoService.BAAAS_KAFKA_CLIENTSECRET;
import static org.kie.baaas.ccp.service.KogitoService.BAAAS_KAFKA_INCOMING_TOPIC;
import static org.kie.baaas.ccp.service.KogitoService.BAAAS_KAFKA_OUTGOING_TOPIC;
import static org.kie.baaas.ccp.service.KogitoService.BOOTSTRAP_SERVERS_KEY;
import static org.kie.baaas.ccp.service.KogitoService.CLIENTID_KEY;
import static org.kie.baaas.ccp.service.KogitoService.CLIENTSECRET_KEY;
import static org.kie.baaas.ccp.service.KogitoService.KOGITO_RUNTIME_CONTEXT;
import static org.kie.baaas.ccp.service.KogitoService.REPLICAS;
import static org.kie.baaas.ccp.service.KogitoService.build;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@QuarkusTest
class KogitoServiceTest extends AbstractControllerTest {

    private final static String KAFKA_COMMON_AUTH_SECRET = "kafka-common-auth";

    @Inject
    KogitoService service;

    @InjectMock
    DecisionVersionService versionService;

    final Secret dashboardSecret = new SecretBuilder()
            .withMetadata(new ObjectMetaBuilder()
                    .withName(BAAAS_DASHBOARD_AUTH_SECRET).build())
            .withData(Map.of(
                    BAAAS_DASHBOARD_BOOTSTRAP_SERVERS, "dashboard-kafka-server:443",
                    BAAAS_DASHBOARD_CLIENTID, "dashboard-client-id",
                    BAAAS_DASHBOARD_CLIENTSECRET, "dashboard-client-secret"))
            .build();

    final Secret kafkaSecret = new SecretBuilder()
            .withMetadata(new ObjectMetaBuilder()
                    .withName(KAFKA_COMMON_AUTH_SECRET).build())
            .withData(Map.of(
                    BAAAS_KAFKA_BOOTSTRAP_SERVERS, "decision-kafka-server:443",
                    BAAAS_KAFKA_CLIENTID, "decision-kafka-client-id",
                    BAAAS_KAFKA_CLIENTSECRET, "decision-kafka-client-secret"))
            .build();

    @Test
    void testNotBuilt() {
        //Given
        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision-1")
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .addToLabels(DECISION_LABEL, "whatever")
                        .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                        .build())
                .withSpec(new DecisionVersionSpec()
                        .setSource(URI.create("somesource"))
                        .setVersion("1"))
                .withStatus(new DecisionVersionStatus())
                .build();

        //When
        service.createOrUpdate(version);

        //Then
        assertThat(client.customResources(KogitoRuntime.class).list().getItems(), empty());
        assertThat(client.secrets().inNamespace(version.getMetadata().getNamespace()).list().getItems(), empty());
        verifyNoInteractions(versionService);
    }

    @Test
    void testNotCurrent() {
        //Given
        Decision decision = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision")
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .build())
                .withSpec(new DecisionSpec()
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("2")))
                .build();
        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision-1")
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .addToLabels(DECISION_LABEL, decision.getMetadata().getName())
                        .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                        .build())
                .withSpec(new DecisionVersionSpec()
                        .setSource(URI.create("othersource"))
                        .setVersion("1"))
                .withStatus(new DecisionVersionStatus().setReady(Boolean.TRUE)
                        .setCondition(CONDITION_BUILD, new ConditionBuilder()
                                .withType(CONDITION_BUILD)
                                .withStatus(ResourceUtils.capitalize(Boolean.TRUE))
                                .build())
                        .setImageRef("quay.io/baaas/test-some-decision:1"))
                .build();
        client.customResources(Decision.class).inNamespace(CUSTOMER_NS).create(decision);

        //When
        service.createOrUpdate(version);

        //Then
        assertThat(client.customResources(KogitoRuntime.class).list().getItems(), empty());
        assertThat(client.secrets().inNamespace(version.getMetadata().getNamespace()).list().getItems(), empty());
        verifyNoInteractions(versionService);
    }

    @Test
    void testCreate() {
        //Given
        Decision decision = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision")
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .build())
                .withSpec(new DecisionSpec()
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();
        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision-1")
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .addToLabels(DECISION_LABEL, decision.getMetadata().getName())
                        .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                        .withOwnerReferences(decision.getOwnerReference())
                        .build())
                .withSpec(decision.getSpec().getDefinition())
                .withStatus(new DecisionVersionStatus().setReady(Boolean.TRUE)
                        .setCondition(CONDITION_BUILD, new ConditionBuilder()
                                .withType(CONDITION_BUILD)
                                .withStatus(ResourceUtils.capitalize(Boolean.TRUE))
                                .build())
                        .setImageRef("quay.io/baaas/test-some-decision:1"))
                .build();

        client.secrets().inNamespace(CONTROLLER_NS).create(dashboardSecret);
        client.customResources(Decision.class).inNamespace(CUSTOMER_NS).create(decision);
        client.customResources(DecisionVersion.class).inNamespace(CUSTOMER_NS).create(version);

        //When
        service.createOrUpdate(version);

        //Then
        assertThat(client.secrets().inNamespace(version.getMetadata().getNamespace()).withName(BAAAS_DASHBOARD_AUTH_SECRET).get(), notNullValue());
        KogitoRuntime runtime = client.customResources(KogitoRuntime.class)
                .inNamespace(version.getMetadata().getNamespace())
                .withName(decision.getMetadata().getName()).get();
        assertThat(runtime, notNullValue());
        assertThat(runtime.getMetadata().getLabels(), aMapWithSize(4));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(BAAAS_RESOURCE_LABEL, BAAAS_RESOURCE_KOGITO_SERVICE));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(DECISION_LABEL, decision.getMetadata().getName()));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(CUSTOMER_LABEL, decision.getMetadata().getLabels().get(CUSTOMER_LABEL)));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(MANAGED_BY_LABEL, OPERATOR_NAME));
        assertThat(runtime.getMetadata().getOwnerReferences(), contains(version.getOwnerReference()));
        assertThat(runtime.getSpec(), hasEntry("image", version.getStatus().getImageRef()));
        assertThat(runtime.getSpec(), hasEntry("replicas", REPLICAS));
        Collection<Map<String, Object>> env = (Collection<Map<String, Object>>) runtime.getSpec().get("env");
        assertThat(env, hasSize(3));
        assertThat(env.stream()
                .allMatch(e -> assertSecretKeyEnv(e, BAAAS_DASHBOARD_AUTH_SECRET, BAAAS_DASHBOARD_BOOTSTRAP_SERVERS, BOOTSTRAP_SERVERS_KEY)
                        || assertSecretKeyEnv(e, BAAAS_DASHBOARD_AUTH_SECRET, BAAAS_DASHBOARD_CLIENTID, CLIENTID_KEY)
                        || assertSecretKeyEnv(e, BAAAS_DASHBOARD_AUTH_SECRET, BAAAS_DASHBOARD_CLIENTSECRET, CLIENTSECRET_KEY))

                , is(true));
        assertThat(version.getStatus().getKogitoServiceRef(), is(decision.getMetadata().getName()));
        verify(versionService, times(1)).setServiceStatus(version, Boolean.FALSE, "Unknown", "");
    }

    @Test
    void testReplaceCurrentDifferentImageRef() throws IOException {
        //Given
        Decision decision = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision")
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .build())
                .withSpec(new DecisionSpec()
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();
        String expectedImageRef = "quay.io/baaas/test-some-decision:1";
        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision-1")
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .addToLabels(DECISION_LABEL, decision.getMetadata().getName())
                        .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                        .withOwnerReferences(decision.getOwnerReference())
                        .withUid(UUID.randomUUID().toString())
                        .build())
                .withSpec(decision.getSpec().getDefinition())
                .withStatus(new DecisionVersionStatus().setReady(Boolean.TRUE)
                        .setCondition(CONDITION_BUILD, new ConditionBuilder()
                                .withType(CONDITION_BUILD)
                                .withStatus(ResourceUtils.capitalize(Boolean.TRUE))
                                .build())
                        .setImageRef(expectedImageRef)
                        .setKogitoServiceRef(decision.getMetadata().getName()))
                .build();

        version.getStatus().setImageRef("replace-me");
        JsonObject existing = addCondition(build(version), new ConditionBuilder().withType("Provisioning").withStatus("True").build());
        version.getStatus().setImageRef(expectedImageRef);

        client.secrets().inNamespace(CONTROLLER_NS).create(dashboardSecret);
        client.customResources(Decision.class).inNamespace(CUSTOMER_NS).create(decision);
        client.customResources(DecisionVersion.class).inNamespace(CUSTOMER_NS).create(version);
        client.customResource(KOGITO_RUNTIME_CONTEXT).create(decision.getMetadata().getNamespace(), existing.toString());

        //When
        service.createOrUpdate(version);

        //Then
        assertThat(client.secrets().inNamespace(version.getMetadata().getNamespace()).withName(BAAAS_DASHBOARD_AUTH_SECRET).get(), notNullValue());
        KogitoRuntime runtime = client.customResources(KogitoRuntime.class)
                .inNamespace(version.getMetadata().getNamespace())
                .withName(decision.getMetadata().getName()).get();
        assertThat(runtime, nullValue());
        assertThat(version.getStatus().getKogitoServiceRef(), nullValue());
        verify(versionService, times(1)).setServiceStatus(version, Boolean.FALSE, "KogitoRuntimeRedeploy", "re-creating KogitoRuntime");
    }

    @Test
    void testServiceReady() throws IOException {
        //Given
        Decision decision = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision")
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .build())
                .withSpec(new DecisionSpec()
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")))
                .build();
        String expectedImageRef = "quay.io/baaas/test-some-decision:1";
        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision-1")
                        .withUid(UUID.randomUUID().toString())
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .addToLabels(DECISION_LABEL, decision.getMetadata().getName())
                        .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                        .withOwnerReferences(decision.getOwnerReference())
                        .build())
                .withSpec(decision.getSpec().getDefinition())
                .withStatus(new DecisionVersionStatus().setReady(Boolean.TRUE)
                        .setCondition(CONDITION_BUILD, new ConditionBuilder()
                                .withType(CONDITION_BUILD)
                                .withStatus(ResourceUtils.capitalize(Boolean.TRUE))
                                .build())
                        .setImageRef(expectedImageRef)
                        .setKogitoServiceRef(decision.getMetadata().getName()))
                .build();

        JsonObject existing = addCondition(build(version), new ConditionBuilder().withType("Deployed").withStatus("True").build());

        client.secrets().inNamespace(CONTROLLER_NS).create(dashboardSecret);
        client.customResources(Decision.class).inNamespace(CUSTOMER_NS).create(decision);
        client.customResources(DecisionVersion.class).inNamespace(CUSTOMER_NS).create(version);
        client.customResource(KOGITO_RUNTIME_CONTEXT).create(decision.getMetadata().getNamespace(), existing.toString());

        //When
        service.createOrUpdate(version);

        //Then
        assertThat(client.secrets().inNamespace(version.getMetadata().getNamespace()).withName(BAAAS_DASHBOARD_AUTH_SECRET).get(), notNullValue());
        KogitoRuntime runtime = client.customResources(KogitoRuntime.class)
                .inNamespace(version.getMetadata().getNamespace())
                .withName(decision.getMetadata().getName()).get();
        assertThat(runtime, notNullValue());
        assertThat(runtime.getMetadata().getLabels(), aMapWithSize(4));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(BAAAS_RESOURCE_LABEL, BAAAS_RESOURCE_KOGITO_SERVICE));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(DECISION_LABEL, decision.getMetadata().getName()));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(CUSTOMER_LABEL, decision.getMetadata().getLabels().get(CUSTOMER_LABEL)));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(MANAGED_BY_LABEL, OPERATOR_NAME));
        assertThat(runtime.getMetadata().getOwnerReferences(), contains(version.getOwnerReference()));
        assertThat(runtime.getSpec(), hasEntry("image", version.getStatus().getImageRef()));
        assertThat(runtime.getSpec(), hasEntry("replicas", REPLICAS));
        Collection<Map<String, Object>> env = (Collection<Map<String, Object>>) runtime.getSpec().get("env");
        assertThat(env, hasSize(3));
        assertThat(env.stream()
                .allMatch(e -> assertSecretKeyEnv(e, BAAAS_DASHBOARD_AUTH_SECRET, BAAAS_DASHBOARD_BOOTSTRAP_SERVERS, BOOTSTRAP_SERVERS_KEY)
                        || assertSecretKeyEnv(e, BAAAS_DASHBOARD_AUTH_SECRET, BAAAS_DASHBOARD_CLIENTID, CLIENTID_KEY)
                        || assertSecretKeyEnv(e, BAAAS_DASHBOARD_AUTH_SECRET, BAAAS_DASHBOARD_CLIENTSECRET, CLIENTSECRET_KEY))

                , is(true));
        assertThat(version.getStatus().getKogitoServiceRef(), is(decision.getMetadata().getName()));
        verify(versionService, times(1)).setReadyStatus(version);
        verify(versionService, times(1)).setServiceStatus(version, Boolean.TRUE, "Deployed", "");
    }

    @Test
    void testKafkaConfig() {
        //Given
        Decision decision = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision")
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .build())
                .withSpec(new DecisionSpec()
                        .setDefinition(new DecisionVersionSpec()
                                .setSource(URI.create("somesource"))
                                .setVersion("1")
                                .setKafka(new Kafka()
                                        .setBootstrapServers("decision-kafka-server:443")
                                        .setInputTopic("decision-input")
                                        .setOutputTopic("decision-output")
                                        .setSecretName(KAFKA_COMMON_AUTH_SECRET))))
                .build();
        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-decision-1")
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .addToLabels(DECISION_LABEL, decision.getMetadata().getName())
                        .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                        .withOwnerReferences(decision.getOwnerReference())
                        .build())
                .withSpec(decision.getSpec().getDefinition())
                .withStatus(new DecisionVersionStatus().setReady(Boolean.TRUE)
                        .setCondition(CONDITION_BUILD, new ConditionBuilder()
                                .withType(CONDITION_BUILD)
                                .withStatus(ResourceUtils.capitalize(Boolean.TRUE))
                                .build())
                        .setImageRef("quay.io/baaas/test-some-decision:1"))
                .build();

        client.secrets().inNamespace(CONTROLLER_NS).create(dashboardSecret);
        client.secrets().inNamespace(CONTROLLER_NS).create(kafkaSecret);
        client.customResources(Decision.class).inNamespace(CUSTOMER_NS).create(decision);
        client.customResources(DecisionVersion.class).inNamespace(CUSTOMER_NS).create(version);

        //When
        service.createOrUpdate(version);

        //Then
        String expectedKafkaSecretName = "some-decision-1-kafka-auth";
        assertThat(client.secrets().inNamespace(version.getMetadata().getNamespace()).withName(BAAAS_DASHBOARD_AUTH_SECRET).get(), notNullValue());
        assertThat(client.secrets().inNamespace(version.getMetadata().getNamespace()).withName(expectedKafkaSecretName).get(), notNullValue());
        KogitoRuntime runtime = client.customResources(KogitoRuntime.class)
                .inNamespace(version.getMetadata().getNamespace())
                .withName(decision.getMetadata().getName()).get();
        assertThat(runtime, notNullValue());
        assertThat(runtime.getMetadata().getLabels(), aMapWithSize(4));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(BAAAS_RESOURCE_LABEL, BAAAS_RESOURCE_KOGITO_SERVICE));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(DECISION_LABEL, decision.getMetadata().getName()));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(CUSTOMER_LABEL, decision.getMetadata().getLabels().get(CUSTOMER_LABEL)));
        assertThat(runtime.getMetadata().getLabels(), hasEntry(MANAGED_BY_LABEL, OPERATOR_NAME));
        assertThat(runtime.getMetadata().getOwnerReferences(), contains(version.getOwnerReference()));
        assertThat(runtime.getSpec(), hasEntry("image", version.getStatus().getImageRef()));
        assertThat(runtime.getSpec(), hasEntry("replicas", REPLICAS));
        Collection<Map<String, Object>> env = (Collection<Map<String, Object>>) runtime.getSpec().get("env");
        assertThat(env, hasSize(8));
        assertThat(env.stream()
                .allMatch(e -> assertSecretKeyEnv(e, BAAAS_DASHBOARD_AUTH_SECRET, BAAAS_DASHBOARD_BOOTSTRAP_SERVERS, BOOTSTRAP_SERVERS_KEY)
                        || assertSecretKeyEnv(e, BAAAS_DASHBOARD_AUTH_SECRET, BAAAS_DASHBOARD_CLIENTID, CLIENTID_KEY)
                        || assertSecretKeyEnv(e, BAAAS_DASHBOARD_AUTH_SECRET, BAAAS_DASHBOARD_CLIENTSECRET, CLIENTSECRET_KEY)
                        || assertSecretKeyEnv(e, expectedKafkaSecretName, BAAAS_KAFKA_CLIENTID, CLIENTID_KEY)
                        || assertSecretKeyEnv(e, expectedKafkaSecretName, BAAAS_KAFKA_CLIENTSECRET, CLIENTSECRET_KEY)
                        || assertKeyEnv(e, BAAAS_KAFKA_BOOTSTRAP_SERVERS, decision.getSpec().getDefinition().getKafka().getBootstrapServers())
                        || assertKeyEnv(e, BAAAS_KAFKA_INCOMING_TOPIC, decision.getSpec().getDefinition().getKafka().getInputTopic())
                        || assertKeyEnv(e, BAAAS_KAFKA_OUTGOING_TOPIC, decision.getSpec().getDefinition().getKafka().getOutputTopic()))

                , is(true));
        assertThat(version.getStatus().getKogitoServiceRef(), is(decision.getMetadata().getName()));
        verify(versionService, times(1)).setServiceStatus(version, Boolean.FALSE, "Unknown", "");
    }

    boolean assertSecretKeyEnv(Map<String, Object> params, String secretName, String envKey, String secretKey) {
        EnvVar envVar = Serialization.unmarshal(Json.createObjectBuilder(params).build().toString(), EnvVar.class);
        return envVar.getName().equals(envKey)
                && envVar.getValue() == null
                && envVar.getValueFrom().getSecretKeyRef().getKey().equals(secretKey)
                && envVar.getValueFrom().getSecretKeyRef().getName().equals(secretName);
    }

    boolean assertKeyEnv(Map<String, Object> params, String envName, String envValue) {
        EnvVar envVar = Serialization.unmarshal(Json.createObjectBuilder(params).build().toString(), EnvVar.class);
        return envVar.getName().equals(envName)
                && envVar.getValueFrom() == null
                && envVar.getValue().equals(envValue);
    }

    private JsonObject addCondition(JsonObject object, Condition condition) {
        JsonArrayBuilder conditionsBuilder = Json.createArrayBuilder();
        conditionsBuilder.add(JsonResourceUtils.toJson(condition));

        JsonObjectBuilder statusBuilder = Json.createObjectBuilder();
        if (object.containsKey("status")) {
            object.getJsonObject("status").entrySet().stream()
                    .filter(e -> !e.getKey().equals("conditions"))
                    .forEach(e -> statusBuilder.add(e.getKey(), e.getValue()));
            object.getJsonObject("status").getJsonArray("conditions").stream().forEach(c -> conditionsBuilder.add(c));

        }
        statusBuilder.add("conditions", conditionsBuilder.build());
        JsonObjectBuilder builder = Json.createObjectBuilder();
        object.entrySet().stream().filter(e -> !e.getKey().equals("status")).forEach(e -> builder.add(e.getKey(), e.getValue()));
        return builder.add("status", statusBuilder.build()).build();
    }

}
