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

package org.kie.baaas.ccp.api;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.sundr.builder.annotations.Buildable;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "customer",
        "decision",
        "version",
        "phase",
        "endpoint",
        "message",
        "at",
        "namespace",
        "version_resource"
})
@JsonDeserialize
@RegisterForReflection
@Buildable(editableEnabled = false, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
@ToString
@EqualsAndHashCode
public class Webhook {

    private final String customer;
    private final String decision;
    private final String version;
    private final Phase phase;
    private final URI endpoint;
    private final String message;
    private final String at;
    private final String namespace;
    @JsonProperty("version_resource")
    private final String versionResource;

    public Webhook(String customer, String decision, String version, Phase phase, URI endpoint, String message, String at, String namespace, String versionResource) {
        this.customer = customer;
        this.decision = decision;
        this.version = version;
        this.phase = phase;
        this.endpoint = endpoint;
        this.message = message;
        this.at = at;
        this.namespace = namespace;
        this.versionResource = versionResource;
    }

    public String getCustomer() {
        return customer;
    }

    public String getDecision() {
        return decision;
    }

    public String getVersion() {
        return version;
    }

    public Phase getPhase() {
        return phase;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public String getMessage() {
        return message;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getAt() {
        return at;
    }

    public String getVersionResource() {
        return versionResource;
    }
}
