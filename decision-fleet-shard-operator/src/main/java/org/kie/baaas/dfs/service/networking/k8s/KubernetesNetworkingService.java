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
package org.kie.baaas.dfs.service.networking.k8s;

import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.controller.k8s.IngressResourceEventSource;
import org.kie.baaas.dfs.model.NetworkResource;
import org.kie.baaas.dfs.service.networking.NetworkingConstants;
import org.kie.baaas.dfs.service.networking.NetworkingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressRuleValueBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBackend;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBackendBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressServiceBackendBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressSpec;
import io.fabric8.kubernetes.api.model.networking.v1.IngressSpecBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.ServiceBackendPortBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;

import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_VERSION_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.OPERATOR_NAME;

public class KubernetesNetworkingService implements NetworkingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkingService.class);
    private static final String PATH_REGEX = "(/|$)(.*)";
    private static final String NGINX_REWRITE_TARGET_ANNOTATION = "nginx.ingress.kubernetes.io/rewrite-target";
    private static final String REWRITE_TARGET_PLACEHOLDER = "/$2";

    private final String kubernetesInternalRegistry;

    private final KubernetesClient client;

    public KubernetesNetworkingService(KubernetesClient client, String kubernetesInternalRegistry) {
        this.client = client;
        this.kubernetesInternalRegistry = kubernetesInternalRegistry;
    }

    @Override
    public String getLocalRegistryUrl() {
        return kubernetesInternalRegistry;
    }

    @Override
    public AbstractEventSource createAndRegisterWatchNetworkingResource() {
        return IngressResourceEventSource.createAndRegisterWatch(client);
    }

    @Override
    public NetworkResource getOrCreate(String endpointName, DecisionVersion decisionVersion, OwnerReference ownerReference) {
        Ingress ingress = client.network().v1().ingresses().inNamespace(decisionVersion.getMetadata().getNamespace()).withName(endpointName).get();

        if (ingress == null) {
            LOGGER.info("No networking resource exists for {}, creating..", endpointName);
            createOrUpdate(endpointName, decisionVersion, ownerReference);
            return null;
        }

        return buildNetworkingResource(ingress, decisionVersion);
    }

    @Override
    public boolean delete(String name, String namespace) {
        try {
            return client.network().v1().ingresses().inNamespace(namespace).withName(name).delete();
        } catch (Exception e) {
            LOGGER.debug("Can't delete ingress with name {} because it does not exist", name);
            return false;
        }
    }

    @Override
    public void createOrUpdate(String endpointName, DecisionVersion decisionVersion, OwnerReference ownerReference) {
        Ingress ingress = buildIngress(endpointName, decisionVersion, ownerReference);
        client.network().v1().ingresses().inNamespace(decisionVersion.getMetadata().getNamespace()).createOrReplace(ingress);
    }

    private Ingress buildIngress(String endpointName, DecisionVersion decisionVersion, OwnerReference ownerReference) {
        ObjectMeta metadata = new ObjectMetaBuilder()
                .withOwnerReferences(ownerReference)
                .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                .addToLabels(DECISION_LABEL, decisionVersion.getMetadata().getLabels().get(DECISION_LABEL))
                .addToLabels(DECISION_VERSION_LABEL, decisionVersion.getMetadata().getName())
                .addToAnnotations(NGINX_REWRITE_TARGET_ANNOTATION, REWRITE_TARGET_PLACEHOLDER)
                .withName(endpointName)
                .build();

        IngressBackend ingressBackend = new IngressBackendBuilder()
                .withService(new IngressServiceBackendBuilder()
                        .withName(decisionVersion.getStatus().getKogitoServiceRef())
                        .withPort(new ServiceBackendPortBuilder().withNumber(80).build())
                        .build())
                .build();

        HTTPIngressPath httpIngressPath = new HTTPIngressPathBuilder()
                .withBackend(ingressBackend)
                .withPath("/" + endpointName + PATH_REGEX)
                .withPathType("Prefix")
                .build();

        IngressRule ingressRule = new IngressRuleBuilder()
                .withHttp(new HTTPIngressRuleValueBuilder()
                        .withPaths(httpIngressPath)
                        .build())
                .build();

        IngressSpec ingressSpec = new IngressSpecBuilder()
                .withRules(ingressRule)
                .build();

        return new IngressBuilder().withMetadata(metadata).withSpec(ingressSpec).build();
    }

    private NetworkResource buildNetworkingResource(Ingress ingress, DecisionVersion decisionVersion) {
        if (ingress.getStatus() == null || ingress.getStatus().getLoadBalancer() == null || ingress.getStatus().getLoadBalancer().getIngress() == null
                || ingress.getStatus().getLoadBalancer().getIngress().isEmpty() || ingress.getStatus().getLoadBalancer().getIngress().get(0).getIp() == null) {
            LOGGER.info("Ingress {} not ready yet", ingress.getMetadata().getName());
            return null;
        }
        String host = ingress.getStatus().getLoadBalancer().getIngress().get(0).getIp();
        String endpoint = NetworkingConstants.HTTP_SCHEME + host + ingress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath().replace(PATH_REGEX, "");
        String kogitoServiceRef = ingress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getBackend().getService().getName();
        return new NetworkResource(endpoint, kogitoServiceRef);
    }
}
