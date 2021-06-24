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
package org.kie.baaas.dfs.service.networking;

import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.model.NetworkResource;

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;

public interface NetworkingService {

    default NetworkResource getOrCreateCurrentEndpoint(String decisionName, DecisionVersion decisionVersion, OwnerReference ownerReference) {
        return getOrCreate(getCurrentEndpointName(decisionName), decisionVersion, ownerReference);
    }

    default NetworkResource getOrCreateVersionEndpoint(DecisionVersion decisionVersion, OwnerReference ownerReference) {
        return getOrCreate(decisionVersion.getMetadata().getName(), decisionVersion, ownerReference);
    }

    default void updateCurrentEndpoint(String decisionName, DecisionVersion decisionVersion, OwnerReference ownerReference) {
        createOrUpdate(getCurrentEndpointName(decisionName), decisionVersion, ownerReference);
    }

    default boolean deleteCurrentEndpoint(String name, String namespace) {
        return delete(getCurrentEndpointName(name), namespace);
    }

    default String getCurrentEndpointName(String decisionName) {
        return decisionName + "-current-endpoint";
    }

    void createOrUpdate(String endpointName, DecisionVersion decisionVersion, OwnerReference ownerReference);

    String getLocalRegistryUrl();

    AbstractEventSource createAndRegisterWatchNetworkingResource();

    NetworkResource getOrCreate(String endpointName, DecisionVersion decisionVersion, OwnerReference ownerReference);

    boolean delete(String name, String namespace);
}
