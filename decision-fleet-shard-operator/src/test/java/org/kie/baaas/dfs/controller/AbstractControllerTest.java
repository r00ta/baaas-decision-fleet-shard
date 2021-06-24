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

import javax.inject.Inject;
import javax.json.Json;

import org.junit.jupiter.api.BeforeEach;
import org.kie.baaas.dfs.api.Decision;
import org.kie.baaas.dfs.api.DecisionRequest;
import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.client.RemoteResourceClient;
import org.kie.baaas.dfs.model.KogitoRuntime;
import org.kie.baaas.dfs.model.PipelineRun;
import org.kie.baaas.dfs.networking.NetworkingTestUtils;
import org.mockito.Mockito;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import io.quarkus.test.junit.mockito.InjectMock;

import static org.kie.baaas.dfs.service.PipelineService.PIPELINE_RUN_CONTEXT;

public abstract class AbstractControllerTest {

    public static final String CONTROLLER_NS = "test";
    public static final String CUSTOMER_NS = "baaas-customer1";
    public static final String CUSTOMER = "customer1";

    @Inject
    protected KubernetesClient client;

    @Inject
    protected OpenShiftServer server;

    @Inject
    protected NetworkingTestUtils networkingTestUtils;

    @InjectMock
    protected RemoteResourceClient remoteResourceClient;

    @BeforeEach
    void cleanUp() {
        client.customResources(DecisionRequest.class).inNamespace(CONTROLLER_NS).delete();
        client.customResources(Decision.class).inNamespace(CUSTOMER_NS).delete();
        client.customResources(DecisionVersion.class).inNamespace(CUSTOMER_NS).delete();

        client.customResources(KogitoRuntime.class).inNamespace(CUSTOMER_NS).delete();
        client.customResources(PipelineRun.class).inNamespace(CONTROLLER_NS).delete();

        client.customResources(PipelineRun.class).inNamespace(CONTROLLER_NS).delete();

        networkingTestUtils.cleanUp(CONTROLLER_NS);
        networkingTestUtils.cleanUp(CUSTOMER_NS);

        client.namespaces().withName(CUSTOMER_NS).delete();
        if (!Json.createObjectBuilder(client.customResource(PIPELINE_RUN_CONTEXT).list(CONTROLLER_NS)).build().getJsonArray("items").isEmpty()) {
            client.customResource(PIPELINE_RUN_CONTEXT).delete(CONTROLLER_NS);
        }

        client.configMaps().inNamespace(CUSTOMER_NS).delete();
        client.secrets().inNamespace(CUSTOMER_NS).delete();
        Mockito.reset(remoteResourceClient);
    }
}
