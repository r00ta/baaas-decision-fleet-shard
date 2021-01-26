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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.sundr.builder.annotations.Buildable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "source",
        "kafka",
        "env",
        "webhooks"
})
@RegisterForReflection
@Buildable(editableEnabled = false, generateBuilderPackage = true, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DecisionRevisionSpec extends AbstractDecisionSpec {

    @NotNull
    private Long id;

    @NotBlank
    private String decision;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDecision() {
        return decision;
    }

    public DecisionRevisionSpec setDecision(String decision) {
        this.decision = decision;
        return this;
    }

    public static DecisionRevisionSpec build(Long id, String decision, DecisionSpec abstractSpec) {
        DecisionRevisionSpec spec = new DecisionRevisionSpec();
        spec.setId(id);
        spec.setDecision(decision);
        spec.setSource(abstractSpec.getSource());
        spec.setEnv(abstractSpec.getEnv());
        spec.setKafka(abstractSpec.getKafka());
        spec.setWebhooks(abstractSpec.getWebhooks());
        return spec;
    }
}
