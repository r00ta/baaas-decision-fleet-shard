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

package org.kie.baaas.api;

import java.net.URI;

import javax.inject.Inject;
import javax.validation.Validator;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

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
        assertThat(validator.validate(spec), hasSize(2));
        spec.setCustomerId("kermit");
        assertThat(validator.validate(spec), hasSize(1));
        spec.setName("my decision");
        assertThat(validator.validate(spec), empty());
    }

    @Test
    void validateDecisionSpec() {
        DecisionSpec spec = new DecisionSpecBuilder().build();
        assertThat(validator.validate(spec), hasSize(1));
        spec.setDefinition(new DecisionVersionSpec());
        assertThat(validator.validate(spec), hasSize(2));
        spec.getDefinition().setVersion("v1");
        assertThat(validator.validate(spec), hasSize(1));
        spec.getDefinition().setSource(URI.create("http://example.com"));
        assertThat(validator.validate(spec), empty());
    }

    @Test
    void validateKafkaDecisionSpec() {
        DecisionVersionSpec spec = new DecisionVersionSpecBuilder()
                .withVersion("v1.0")
                .withSource(URI.create("http://somewhere.com"))
                .withKafka(new KafkaBuilder().build())
                .build();
        assertThat(validator.validate(spec), hasSize(4));
        spec.getKafka().setHost("my-kafka.example.com:9092");
        assertThat(validator.validate(spec), hasSize(3));
        spec.getKafka().setSecretName("the-secret");
        assertThat(validator.validate(spec), hasSize(2));
        spec.getKafka().setInputTopic("the-input-topic");
        assertThat(validator.validate(spec), hasSize(1));
        spec.getKafka().setOutputTopic("the-output-topic");
        assertThat(validator.validate(spec), empty());
    }
}
