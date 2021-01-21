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
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.sundr.builder.annotations.Buildable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "customerId",
        "version",
        "source",
        "kafka",
        "env",
        "webhooks"
})
@RegisterForReflection
@Buildable(editableEnabled = false, generateBuilderPackage = true, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class DecisionSpec {

    @NotBlank
    private String name;
    @NotBlank
    private String customerId;
    @NotNull
    private URI source;
    @Valid
    private Kafka kafka;
    private Collection<EnvVar> env;
    private Collection<String> webhooks;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public URI getSource() {
        return source;
    }

    public void setSource(URI source) {
        this.source = source;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }

    public Collection<EnvVar> getEnv() {
        return env;
    }

    public void setEnv(Collection<EnvVar> env) {
        this.env = env;
    }

    public Collection<String> getWebhooks() {
        return webhooks;
    }

    public void setWebhooks(Collection<String> webhooks) {
        this.webhooks = webhooks;
    }

    public boolean needsUpdate(DecisionSpec other) {
        if (other == null) {
            return true;
        }
        if (!this.customerId.equals(other.customerId)) {
            return true;
        }
        if (!this.name.equals(other.name)) {
            return true;
        }
        if (!this.source.equals(other.source)) {
            return true;
        }
        if (this.webhooks == null && other.webhooks != null) {
            return true;
        }
        if (this.webhooks != null) {
            if (other.webhooks == null) {
                return true;
            }
            if (this.webhooks.size() != other.webhooks.size()) {
                return true;
            }
            if (this.webhooks.stream().anyMatch(e -> !other.webhooks.contains(e))) {
                return true;
            }
        }
        if (this.env == null && other.env != null) {
            return true;
        }
        if (this.env != null) {
            if (other.env == null) {
                return true;
            }
            if (this.env.stream().anyMatch(e -> !other.env.stream().anyMatch(o -> o.equals(e)))) {
                return true;
            }
        }
        if (this.kafka == null && other.kafka != null) {
            return true;
        }
        if (this.kafka != null && this.kafka.needsUpdate(other.kafka)) {
            return true;
        }
        return false;
    }
}
