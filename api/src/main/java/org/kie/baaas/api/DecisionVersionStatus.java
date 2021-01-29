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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.fabric8.kubernetes.api.model.Condition;
import io.fabric8.kubernetes.api.model.ConditionBuilder;
import io.sundr.builder.annotations.Buildable;
import lombok.AccessLevel;
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
@Buildable(editableEnabled = false, generateBuilderPackage = true, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
@ToString
@EqualsAndHashCode
@Getter
@Setter
@Accessors(chain = true)
public class DecisionVersionStatus {

    private static final String BUILD = "Build";
    private static final String READY = "Ready";
    private static final String ACTIVE = "Active";
    private static final String BUILD_BUILDING = "Building";
    private static final String BUILD_FAILED = "BuildFailed";
    private static final String BUILD_COMPLETED = "Completed";

    private String pipelineRef;
    //Container imageRef as a result of a pipelineRun
    private String imageRef;
    private String kogitoSvc;
    //ConfigMap name where the application.properties are stored
    private String configRef;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, Condition> conditions;

    public DecisionVersionStatus setBuilding() {
        return setCondition(BUILD, BUILD_BUILDING);
    }

    public DecisionVersionStatus setBuildFailed() {
        return setCondition(BUILD, BUILD_FAILED);
    }

    public DecisionVersionStatus setBuildCompleted() {
        return setCondition(BUILD, BUILD_COMPLETED);
    }

    @JsonIgnore
    public boolean isBuildFailed() {
        return BUILD_FAILED.equals(conditions.get(BUILD));
    }

    @JsonIgnore
    public boolean isBuildCompleted() {
        return BUILD_COMPLETED.equals(conditions.get(BUILD));
    }

    public DecisionVersionStatus setReady(Boolean ready) {
        return setCondition(READY, ready.toString());
    }

    private DecisionVersionStatus setCondition(String type, String status) {
        if (conditions == null) {
            conditions = new HashMap<>();
        }
        conditions.put(type, new ConditionBuilder()
                .withLastTransitionTime(new Date().toString())
                .withType(type)
                .withStatus(status)
                .build());
        return this;
    }

    public Collection<Condition> getConditions() {
        return conditions.values();
    }

    public DecisionVersionStatus setConditions(Collection<Condition> conditions) {
        this.conditions = new HashMap<>();
        conditions.stream().forEach(c -> this.conditions.put(c.getType(), c));
        return this;
    }

}
