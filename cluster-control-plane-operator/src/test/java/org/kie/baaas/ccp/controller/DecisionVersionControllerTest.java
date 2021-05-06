package org.kie.baaas.ccp.controller;

import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.api.DecisionVersionBuilder;
import org.kie.baaas.ccp.api.DecisionVersionStatus;
import org.kie.baaas.ccp.model.PipelineRun;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;
import io.quarkus.test.junit.QuarkusTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.kie.baaas.ccp.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.OPERATOR_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@QuarkusTest
class DecisionVersionControllerTest extends AbstractControllerTest {

    @Inject
    DecisionVersionController versionController;

    EventSourceManager eventSourceManager = mock(EventSourceManager.class);

    @BeforeEach
    void init() {
        versionController.init(eventSourceManager);
    }

    @Test
    void testDelete() {
        //Given
        String versionName = "some-decision-1";
        PipelineRun pipelineRun = new PipelineRun();
        pipelineRun.setMetadata(new ObjectMetaBuilder().withName(CUSTOMER + "-" + versionName).build());
        client.customResources(PipelineRun.class).inNamespace(CONTROLLER_NS).create(pipelineRun);

        DecisionVersion version = new DecisionVersionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(versionName)
                        .withNamespace(CUSTOMER_NS)
                        .addToLabels(CUSTOMER_LABEL, CUSTOMER)
                        .addToLabels(DECISION_LABEL, "some-decision")
                        .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                        .withUid(UUID.randomUUID().toString())
                        .build())
                .withStatus(new DecisionVersionStatus().setReady(Boolean.TRUE))
                .build();

        //When
        assertThat(versionController.deleteResource(version, null), equalTo(DeleteControl.DEFAULT_DELETE));

        //Then
        assertThat(client.customResources(PipelineRun.class).inNamespace(CONTROLLER_NS).list().getItems(), empty());
        verify(eventSourceManager, times(1)).deRegisterCustomResourceFromEventSource("decision-event-source-" + versionName, version.getMetadata().getUid());
    }
}
