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

package org.kie.baaas.dfs.api;

import java.net.URI;

import javax.inject.Inject;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class ValidationTest {

    @Inject
    Validator validator;

    @Test
    void validateDecisionRequestSpec() {
        DecisionRequestSpec spec = new DecisionRequestSpecBuilder().build();
        assertThat(validator.validate(spec), hasSize(4));
        spec.setSource(URI.create("http://example.com"));
        assertThat(validator.validate(spec), hasSize(3));
        spec.setCustomerId("kermit");
        assertThat(validator.validate(spec), hasSize(2));
        spec.setName("my decision");
        assertThat(validator.validate(spec), hasSize(1));
        spec.setVersion("1.0");
        assertThat(validator.validate(spec), empty());

        spec.setKafka(new KafkaRequest());
        assertThat(validator.validate(spec), hasSize(4));
        spec.getKafka().setBootstrapServers("server:9002");
        assertThat(validator.validate(spec), hasSize(3));
        spec.getKafka().setInputTopic("topicInput");
        assertThat(validator.validate(spec), hasSize(2));
        spec.getKafka().setOutputTopic("topicOutput");
        assertThat(validator.validate(spec), hasSize(1));

        spec.getKafka().setCredential(new KafkaCredential());
        assertThat(validator.validate(spec), hasSize(2));
        spec.getKafka().getCredential().setClientId("svc-001");
        assertThat(validator.validate(spec), hasSize(1));
        spec.getKafka().getCredential().setClientSecret("client-secret-abc");
        assertThat(validator.validate(spec), empty());
    }

    @Test
    void validateDecisionSpec() {
        DecisionSpec spec = new DecisionSpecBuilder().build();
        assertThat(validator.validate(spec), hasSize(1));
        spec.setDefinition(new DecisionVersionSpecBuilder().build());
        assertThat(validator.validate(spec), hasSize(2));
        spec.getDefinition().setVersion("v1");
        assertThat(validator.validate(spec), hasSize(1));
        spec.getDefinition().setSource(URI.create("http://example.com"));
        assertThat(validator.validate(spec), empty());
        spec.getDefinition().setKafka(new Kafka());
        assertThat(validator.validate(spec), hasSize(2));
        spec.getDefinition().getKafka().setBootstrapServers("my-kafka.example.com:9092");
        assertThat(validator.validate(spec), hasSize(1));
        spec.getDefinition().getKafka().setSecretName("customer1-kafka-auth");
        assertThat(validator.validate(spec), empty());
    }

    @Test
    void validateKafkaDecisionSpec() {
        DecisionVersionSpec spec = new DecisionVersionSpecBuilder()
                .withVersion("v1.0")
                .withSource(URI.create("http://somewhere.com"))
                .withKafka(new KafkaBuilder().build())
                .build();
        assertThat(validator.validate(spec), hasSize(2));
        spec.getKafka().setBootstrapServers("my-kafka.example.com:9092");
        assertThat(validator.validate(spec), hasSize(1));
        spec.getKafka().setSecretName("customer1-kafka-auth");
        assertThat(validator.validate(spec), empty());
    }
}
