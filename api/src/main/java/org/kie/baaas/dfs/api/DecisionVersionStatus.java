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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.Condition;
import io.fabric8.kubernetes.api.model.ConditionBuilder;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "pipelineRef",
        "imageRef",
        "kogitoServiceRef",
        "configRef",
        "conditions"
})
@JsonDeserialize
@Buildable(editableEnabled = false, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", refs = {
        @BuildableReference(Condition.class)
})
@ToString
@EqualsAndHashCode
@Getter
@Setter
@Accessors(chain = true)
public class DecisionVersionStatus {

    public static final String CONDITION_BUILD = "Build";
    public static final String CONDITION_SERVICE = "Service";
    public static final String CONDITION_READY = "Ready";

    public static final String REASON_SUCCESS = "Success";
    public static final String REASON_FAILED = "Failed";

    @JsonProperty
    private String pipelineRef;
    //Container imageRef as a result of a pipelineRun
    @JsonProperty
    private String imageRef;
    @JsonProperty
    private String kogitoServiceRef;

    @JsonIgnore
    private Map<String, Condition> conditions = new HashMap<>();

    public DecisionVersionStatus setReady(Boolean ready) {
        return setCondition(CONDITION_READY, ready, "", "");
    }

    private DecisionVersionStatus setCondition(String type, Boolean status, String reason, String message) {
        return setCondition(type, new ConditionBuilder()
                .withLastTransitionTime(ResourceUtils.now())
                .withType(type)
                .withStatus(ResourceUtils.capitalize(status.toString()))
                .withReason(reason)
                .withMessage(message)
                .build());
    }

    public DecisionVersionStatus setCondition(String type, Condition condition) {
        conditions.put(type, condition);
        return this;
    }

    @JsonIgnore
    public Map<String, Condition> getConditions() {
        return conditions;
    }

    @JsonProperty("conditions")
    public Collection<Condition> getConditionValues() {
        return conditions.values();
    }

    @JsonProperty("conditions")
    public void setConditions(Collection<Condition> conditions) {
        this.conditions = new HashMap<>();
        conditions.forEach(c -> this.conditions.put(c.getType(), c));
    }

    @JsonIgnore
    public String getBuildStatus() {
        if (conditions.containsKey(CONDITION_BUILD)) {
            return conditions.get(CONDITION_BUILD).getStatus();
        }
        return null;
    }

    public Condition getCondition(String key) {
        return conditions.get(key);
    }

    public String isReady() {
        Condition readyCondition = getCondition(CONDITION_READY);
        if (readyCondition == null) {
            return ResourceUtils.capitalize(Boolean.FALSE);
        }
        return readyCondition.getStatus();
    }
}
