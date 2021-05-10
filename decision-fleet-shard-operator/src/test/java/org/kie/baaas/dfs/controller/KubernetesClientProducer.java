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
package org.kie.baaas.dfs.controller;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;

import static org.kie.baaas.dfs.service.KogitoService.KOGITO_RUNTIME_CONTEXT;
import static org.kie.baaas.dfs.service.PipelineService.PIPELINE_RUN_CONTEXT;

@ApplicationScoped
public class KubernetesClientProducer {

    private final KubernetesServer mockServer;

    private final KubernetesClient client;

    public KubernetesClientProducer() {
        List<CustomResourceDefinitionContext> crds = new ArrayList<>();
        crds.add(KOGITO_RUNTIME_CONTEXT);
        crds.add(PIPELINE_RUN_CONTEXT);
        this.mockServer = new KubernetesServer(false, true, crds);
        this.mockServer.before();
        this.client = mockServer.getClient();
    }

    @Produces
    KubernetesServer getMockServer() {
        return mockServer;
    }

    @Produces
    KubernetesClient getClient() {
        return client;
    }

}