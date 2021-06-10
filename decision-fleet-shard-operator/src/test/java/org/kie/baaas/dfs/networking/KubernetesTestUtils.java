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

import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.LoadBalancerStatusBuilder;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressStatus;
import io.fabric8.kubernetes.api.model.networking.v1.IngressStatusBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesTestUtils implements NetworkingTestUtils {

    private final KubernetesClient client;
    private final NetworkingService networkingService;

    public KubernetesTestUtils(KubernetesClient kubernetesClient, NetworkingService networkingService) {
        this.client = kubernetesClient;
        this.networkingService = networkingService;
    }

    @Override
    public void mockDecisionNetworkingResource(String endpointName, String serviceRef, DecisionVersion decisionVersion, OwnerReference ownerReference) {
        networkingService.createOrUpdate(endpointName, decisionVersion, ownerReference);
        populateNetworkingResource(endpointName, serviceRef, decisionVersion);
    }

    @Override
    public void populateNetworkingResource(String endpointName, String serviceRef, DecisionVersion decisionVersion) {
        Ingress i = getIngress(endpointName, decisionVersion.getMetadata().getNamespace());

        IngressStatus ingressStatus = new IngressStatusBuilder()
                .withLoadBalancer(new LoadBalancerStatusBuilder()
                        .withIngress(new LoadBalancerIngress("test", NetworkingTestConstants.HOST))
                        .build())
                .build();
        i.setStatus(ingressStatus);

        client.network().v1().ingresses().inNamespace(decisionVersion.getMetadata().getNamespace()).createOrReplace(i);
    }

    @Override
    public int getNetworkingResourceSize(String namespace) {
        return client.network().v1().ingresses().inNamespace(namespace).list().getItems().size();
    }

    @Override
    public String getLabel(String endpointName, String label, DecisionVersion decisionVersion) {
        Ingress i = getIngress(endpointName, decisionVersion.getMetadata().getNamespace());
        return i.getMetadata().getLabels().get(label);
    }

    @Override
    public void cleanUp(String namespace) {
        client.network().v1().ingresses().inNamespace(namespace).delete();
    }

    private Ingress getIngress(String name, String namespace) {
        return client.network().v1().ingresses().inNamespace(namespace).withName(name).get();
    }
}
