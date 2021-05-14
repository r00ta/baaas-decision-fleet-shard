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

    @Test
    void testCreateOrUpdate() {
        //given
        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName("some-version-1")
                        .withNamespace("baaas-kermit")
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
        assertItem(params, PipelineService.VAR_REGISTRY_LOCATION, "image-registry.openshift-image-registry.svc:5000/baaas-kermit/some-decision:5");
    }

    void assertItem(List<Map<String, String>> params, String name, String expected) {
        Optional<Map<String, String>> match = params.stream().filter(v -> v.get("name").equals(name)).findFirst();
        assertTrue(match.isPresent());
        assertThat(match.get(), hasEntry("name", name));
        assertThat(match.get(), hasEntry("value", expected));
    }
}
