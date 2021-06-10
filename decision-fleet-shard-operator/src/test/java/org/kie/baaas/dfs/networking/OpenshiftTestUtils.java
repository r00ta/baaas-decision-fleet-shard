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
package org.kie.baaas.dfs.networking;

import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.service.networking.NetworkingService;

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteIngress;
import io.fabric8.openshift.api.model.RouteIngressBuilder;
import io.fabric8.openshift.api.model.RouteIngressConditionBuilder;
import io.fabric8.openshift.api.model.RouteSpec;
import io.fabric8.openshift.api.model.RouteSpecBuilder;
import io.fabric8.openshift.api.model.RouteStatus;
import io.fabric8.openshift.api.model.RouteStatusBuilder;
import io.fabric8.openshift.api.model.RouteTargetReference;
import io.fabric8.openshift.api.model.RouteTargetReferenceBuilder;
import io.fabric8.openshift.client.OpenShiftClient;

public class OpenshiftTestUtils implements NetworkingTestUtils {

    private final OpenShiftClient client;
    private final NetworkingService networkingService;

    public OpenshiftTestUtils(OpenShiftClient client, NetworkingService networkingService) {
        this.client = client;
        this.networkingService = networkingService;
    }

    @Override
    public void mockDecisionNetworkingResource(String endpointName, String serviceRef, DecisionVersion decisionVersion, OwnerReference ownerReference) {
        networkingService.createOrUpdate(endpointName, decisionVersion, ownerReference);
        populateNetworkingResource(endpointName, serviceRef, decisionVersion);
    }

    @Override
    public void populateNetworkingResource(String endpointName, String serviceRef, DecisionVersion decisionVersion) {
        Route route = getRoute(endpointName, decisionVersion.getMetadata().getNamespace());

        RouteTargetReference routeTargetReference = new RouteTargetReferenceBuilder()
                .withName(serviceRef)
                .withKind("Service")
                .build();

        RouteSpec routeSpec = new RouteSpecBuilder()
                .withHost(NetworkingTestConstants.HTTP_HOST + endpointName)
                .withTo(routeTargetReference)
                .build();

        route.setSpec(routeSpec);

        RouteIngress routeIngress = new RouteIngressBuilder()
                .withConditions(new RouteIngressConditionBuilder()
                        .withType("Admitted")
                        .build())
                .build();

        RouteStatus routeStatus = new RouteStatusBuilder()
                .withIngress(routeIngress)
                .build();

        route.setStatus(routeStatus);

        client.routes().inNamespace(decisionVersion.getMetadata().getNamespace()).createOrReplace(route);
    }

    @Override
    public int getNetworkingResourceSize(String namespace) {
        return client.routes().inNamespace(namespace).list().getItems().size();
    }

    @Override
    public String getLabel(String endpointName, String label, DecisionVersion decisionVersion) {
        Route route = getRoute(endpointName, decisionVersion.getMetadata().getNamespace());
        return route.getMetadata().getLabels().get(label);
    }

    @Override
    public void cleanUp(String namespace) {
        try {
            client.routes().inNamespace(namespace).delete();
        } catch (Exception ignored) {
        }
    }

    private Route getRoute(String name, String namespace) {
        return client.routes().inNamespace(namespace).withName(name).get();
    }
}
