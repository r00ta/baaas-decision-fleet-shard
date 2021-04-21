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
package org.kie.baaas.ccp.model;

import org.kie.baaas.ccp.api.DecisionVersion;

import io.fabric8.kubernetes.client.Watcher;
import io.javaoperatorsdk.operator.processing.event.AbstractEvent;
import io.javaoperatorsdk.operator.processing.event.EventSource;

public class DecisionVersionEvent extends AbstractEvent {

    private final Watcher.Action action;
    private final DecisionVersion version;

    public DecisionVersionEvent(Watcher.Action action, DecisionVersion resource, EventSource eventSource) {
        super(resource.getMetadata().getOwnerReferences().get(0).getUid(), eventSource);
        this.action = action;
        this.version = resource;
    }

    public Watcher.Action getAction() {
        return action;
    }

    public DecisionVersion getVersion() {
        return version;
    }

    public String resourceUid() {
        return getVersion().getMetadata().getUid();
    }
}
