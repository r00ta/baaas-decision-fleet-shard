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
package org.kie.baaas.ccp.controller;

import javax.inject.Inject;
import javax.json.Json;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.AfterEach;
import org.kie.baaas.ccp.api.Decision;
import org.kie.baaas.ccp.api.DecisionVersion;

import static org.kie.baaas.ccp.service.KogitoService.KOGITO_RUNTIME_CONTEXT;
import static org.kie.baaas.ccp.service.PipelineService.PIPELINE_RUN_CONTEXT;

abstract class AbstractControllerTest {

    static final String NAMESPACE = "baaas-test";

    @Inject
    KubernetesClient client;

    @AfterEach
    void cleanUp() {
        client.customResources(Decision.class).inNamespace(NAMESPACE).delete();
        client.customResources(DecisionVersion.class).inNamespace(NAMESPACE).delete();
        client.configMaps().inNamespace(NAMESPACE).delete();
        client.secrets().inNamespace(NAMESPACE).delete();
        if (!Json.createObjectBuilder(client.customResource(PIPELINE_RUN_CONTEXT).list(NAMESPACE)).build().getJsonArray("items").isEmpty()) {
            client.customResource(PIPELINE_RUN_CONTEXT).delete(NAMESPACE);
        }
        if (!Json.createObjectBuilder(client.customResource(KOGITO_RUNTIME_CONTEXT).list(NAMESPACE)).build().getJsonArray("items").isEmpty()) {
            client.customResource(KOGITO_RUNTIME_CONTEXT).delete(NAMESPACE);
        }
    }

}
