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
package org.kie.baaas.dfs.service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.api.DecisionVersionBuilder;
import org.kie.baaas.dfs.api.DecisionVersionSpec;
import org.kie.baaas.dfs.controller.AbstractControllerTest;
import org.kie.baaas.dfs.model.PipelineRun;
import org.kie.baaas.dfs.service.networking.NetworkingService;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.quarkus.test.junit.QuarkusTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.baaas.dfs.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_LABEL;

@QuarkusTest
class PipelineServiceTest extends AbstractControllerTest {

    @Inject
    PipelineService service;

    @Inject
    NetworkingService networkingService;

    @Test
    void testCreateOrUpdate() {
        //given
        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-version-1")
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, "kermit")
                        .addToLabels(DECISION_LABEL, "some-decision")
                        .withUid(UUID.randomUUID().toString())
                        .build())
                .withSpec(new DecisionVersionSpec()
                        .setVersion("5")
                        .setSource(URI.create("https://dmn-source.example.com/123")))
                .build();

        //when
        service.createOrUpdate(version);

        //then
        List<PipelineRun> pipelineRuns = client.customResources(PipelineRun.class).list().getItems();
        assertThat(pipelineRuns, hasSize(1));

        PipelineRun pipelineRun = pipelineRuns.get(0);
        assertThat((Map<String, String>) pipelineRun.getSpec().get("pipelineRef"), hasEntry("name", PipelineService.PIPELINE_REF));
        List<Map<String, String>> params = (List<Map<String, String>>) pipelineRun.getSpec().get("params");
        assertItem(params, PipelineService.VAR_DMN_LOCATION, version.getSpec().getSource().toString());
        assertItem(params, PipelineService.VAR_POM_CONFIGMAP, "baaas-dfs-build-pom-xml");
        assertItem(params, PipelineService.VAR_PROPS_CONFIGMAP, "baaas-dfs-build-application-props");
        assertItem(params, PipelineService.VAR_REGISTRY_LOCATION, networkingService.getLocalRegistryUrl() + "/baaas-customer1/some-decision:5");
    }

    void assertItem(List<Map<String, String>> params, String name, String expected) {
        Optional<Map<String, String>> match = params.stream().filter(v -> v.get("name").equals(name)).findFirst();
        assertTrue(match.isPresent());
        assertThat(match.get(), hasEntry("name", name));
        assertThat(match.get(), hasEntry("value", expected));
    }
}
