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
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "phase",
        "pipelineRun",
        "imageRef",
        "kogitoSvc",
        "propertiesConfigMap",
        "credentialsSecret",
        "message"
})
@RegisterForReflection
@Buildable(editableEnabled = false, generateBuilderPackage = true, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
@ToString
@EqualsAndHashCode
public class DecisionRevisionStatus {

    private Phase phase;

    private String pipelineRun;

    //Container imageRef as a result of a pipelineRun
    private String imageRef;

    private String kogitoSvc;

    //ConfigMap name where the application.properties are stored
    private String propertiesConfigMap;

    //SecretMap name where any additional credentials are stored
    private String credentialsSecret;

    private String message;

    public Phase getPhase() {
        return phase;
    }

    public DecisionRevisionStatus setPhase(Phase phase) {
        this.phase = phase;
        return this;
    }

    public String getPipelineRun() {
        return pipelineRun;
    }

    public DecisionRevisionStatus setPipelineRun(String pipelineRun) {
        this.pipelineRun = pipelineRun;
        return this;
    }

    public String getImageRef() {
        return imageRef;
    }

    public DecisionRevisionStatus setImageRef(String imageRef) {
        this.imageRef = imageRef;
        return this;
    }

    public String getKogitoSvc() {
        return kogitoSvc;
    }

    public DecisionRevisionStatus setKogitoSvc(String kogitoSvc) {
        this.kogitoSvc = kogitoSvc;
        return this;
    }

    public String getPropertiesConfigMap() {
        return propertiesConfigMap;
    }

    public DecisionRevisionStatus setPropertiesConfigMap(String propertiesConfigMap) {
        this.propertiesConfigMap = propertiesConfigMap;
        return this;
    }

    public String getCredentialsSecret() {
        return credentialsSecret;
    }

    public DecisionRevisionStatus setCredentialsSecret(String credentialsSecret) {
        this.credentialsSecret = credentialsSecret;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public DecisionRevisionStatus setMessage(String message) {
        this.message = message;
        return this;
    }
}
