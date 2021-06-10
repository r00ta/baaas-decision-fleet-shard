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
package org.kie.baaas.dfs.service.networking.openshift;

import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.controller.openshift.OpenshiftResourceEventSource;
import org.kie.baaas.dfs.model.NetworkResource;
import org.kie.baaas.dfs.service.networking.NetworkingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.api.model.RouteSpec;
import io.fabric8.openshift.api.model.RouteSpecBuilder;
import io.fabric8.openshift.api.model.RouteTargetReferenceBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;

import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_VERSION_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.OPERATOR_NAME;

public class OpenshiftNetworkingService implements NetworkingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkingService.class);

    private final String openshiftInternalRegistry;

    private final OpenShiftClient client;

    public OpenshiftNetworkingService(OpenShiftClient client, String openshiftInternalRegistry) {
        this.client = client;
        this.openshiftInternalRegistry = openshiftInternalRegistry;
    }

    @Override
    public String getLocalRegistryUrl() {
        return openshiftInternalRegistry;
    }

    @Override
    public AbstractEventSource createAndRegisterWatchNetworkingResource() {
        return OpenshiftResourceEventSource.createAndRegisterWatch(client);
    }

    @Override
    public NetworkResource getOrCreate(String endpointName, DecisionVersion decisionVersion, OwnerReference ownerReference) {
        Route route = client.routes().inNamespace(decisionVersion.getMetadata().getNamespace()).withName(endpointName).get();

        if (route == null) {
            LOGGER.info("No networking resource exists for {}, creating..", endpointName);
            createOrUpdate(endpointName, decisionVersion, ownerReference);
            return null;
        }

        return buildNetworkingResource(route);
    }

    public boolean delete(String name, String namespace) {
        try {
            return client.routes().inNamespace(namespace).withName(name).delete();
        } catch (Exception e) {
            LOGGER.debug("Can't delete ingress with name {} because it does not exist", name);
            return false;
        }
    }

    @Override
    public void createOrUpdate(String endpointName, DecisionVersion decisionVersion, OwnerReference ownerReference) {
        Route route = buildRoute(endpointName, decisionVersion, ownerReference);
        client.routes().inNamespace(decisionVersion.getMetadata().getNamespace()).createOrReplace(route);
    }

    private Route buildRoute(String endpointName, DecisionVersion decisionVersion, OwnerReference ownerReference) {
        ObjectMeta metadata = new ObjectMetaBuilder()
                .withOwnerReferences(ownerReference)
                .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                .addToLabels(DECISION_LABEL, decisionVersion.getMetadata().getLabels().get(DECISION_LABEL))
                .addToLabels(DECISION_VERSION_LABEL, decisionVersion.getMetadata().getName())
                .withName(endpointName)
                .build();

        RouteSpec routeSpec = new RouteSpecBuilder()
                .withTo(new RouteTargetReferenceBuilder()
                        .withKind("Service")
                        .withName(decisionVersion.getStatus().getKogitoServiceRef())
                        .build())
                .build();

        Route route = new RouteBuilder()
                .withMetadata(metadata)
                .withSpec(routeSpec)
                .build();

        return route;
    }

    private NetworkResource buildNetworkingResource(Route route) {
        if ("Admitted".equals(route.getStatus().getIngress().get(0).getConditions().get(0).getType())) {
            String endpoint = route.getSpec().getHost();
            String kogitoServiceRef = route.getSpec().getTo().getName();
            return new NetworkResource(endpoint, kogitoServiceRef);
        }

        LOGGER.info("Route {} not ready yet", route.getMetadata().getName());
        return null;
    }
}
