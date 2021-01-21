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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.sundr.builder.annotations.Buildable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, generateBuilderPackage = true, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
@JsonPropertyOrder({
        "host",
        "secretName",
        "inputTopic",
        "outputTopic"
})
public class Kafka {

    @NotBlank
    private String host;
    @NotBlank
    private String secretName;
    @NotBlank
    private String inputTopic;
    @NotBlank
    private String outputTopic;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }

    public String getInputTopic() {
        return inputTopic;
    }

    public void setInputTopic(String inputTopic) {
        this.inputTopic = inputTopic;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }

    public boolean needsUpdate(Kafka other) {
        if (other == null) {
            return true;
        }
        if (!this.host.equals(other.host)) {
            return true;
        }
        if (!this.secretName.equals(other.secretName)) {
            return true;
        }
        if (!this.inputTopic.equals(other.inputTopic)) {
            return true;
        }
        if (!this.outputTopic.equals(other.outputTopic)) {
            return true;
        }
        return false;
    }
}
