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
package org.kie.baaas.dfs.model;

import org.kie.baaas.dfs.api.Decision;

import io.fabric8.kubernetes.client.Watcher;
import io.javaoperatorsdk.operator.processing.event.AbstractEvent;
import io.javaoperatorsdk.operator.processing.event.EventSource;

public class DecisionEvent extends AbstractEvent {

    private final Watcher.Action action;
    private final Decision decision;

    public DecisionEvent(Watcher.Action action, String versionUid, Decision resource, EventSource eventSource) {
        super(versionUid, eventSource);
        this.action = action;
        this.decision = resource;
    }

    public Watcher.Action getAction() {
        return action;
    }

    public Decision getDecision() {
        return decision;
    }

    public String resourceUid() {
        return getDecision().getMetadata().getUid();
    }
}
