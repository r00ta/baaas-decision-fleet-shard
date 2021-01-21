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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.sundr.builder.annotations.Buildable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "status",
        "decisionName",
        "decisionNamespace",
        "message"
})
@RegisterForReflection
@Buildable(editableEnabled = false, generateBuilderPackage = true, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class DecisionRequestStatus {

    private String decisionName;
    private String decisionNamespace;
    private AdmissionStatus admission;
    private String message;

    public String getDecisionName() {
        return decisionName;
    }

    public DecisionRequestStatus setDecisionName(String decisionName) {
        this.decisionName = decisionName;
        return this;
    }

    public String getDecisionNamespace() {
        return decisionNamespace;
    }

    public DecisionRequestStatus setDecisionNamespace(String decisionNamespace) {
        this.decisionNamespace = decisionNamespace;
        return this;
    }

    public AdmissionStatus getAdmission() {
        return admission;
    }

    public DecisionRequestStatus setAdmission(AdmissionStatus admission) {
        this.admission = admission;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public DecisionRequestStatus setMessage(String message) {
        this.message = message;
        return this;
    }
}
