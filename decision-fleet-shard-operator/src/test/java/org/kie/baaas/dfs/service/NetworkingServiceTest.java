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
import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.api.DecisionVersionBuilder;
import org.kie.baaas.dfs.api.DecisionVersionSpec;
import org.kie.baaas.dfs.api.DecisionVersionStatus;
import org.kie.baaas.dfs.controller.AbstractControllerTest;
import org.kie.baaas.dfs.model.NetworkResource;
import org.kie.baaas.dfs.networking.NetworkingTestConstants;
import org.kie.baaas.dfs.networking.NetworkingTestUtils;
import org.kie.baaas.dfs.service.networking.NetworkingService;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.quarkus.test.junit.QuarkusTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.kie.baaas.dfs.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_VERSION_LABEL;

@QuarkusTest
public class NetworkingServiceTest extends AbstractControllerTest {

    private static final String DECISION_NAME = "some-decision";
    private static final String CURRENT_ENDPOINT_NAME = DECISION_NAME + "-current-endpoint";

    private static final DecisionVersion VERSION = new DecisionVersionBuilder()
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
            .withStatus(new DecisionVersionStatus()
                    .setKogitoServiceRef("some-version-1-service"))
            .build();

    @Inject
    NetworkingService service;

    @Inject
    NetworkingTestUtils networkingTestUtils;

    @Test
    void testCreateOrUpdate() {
        //when
        service.createOrUpdate(DECISION_NAME, VERSION, VERSION.getOwnerReference());
        networkingTestUtils.populateNetworkingResource(DECISION_NAME, VERSION.getStatus().getKogitoServiceRef(), VERSION);
        NetworkResource networkResource = service.getOrCreate(DECISION_NAME, VERSION, VERSION.getOwnerReference());

        //then
        assertThat(networkResource.getEndpoint(), is(NetworkingTestConstants.HTTP_HOST + DECISION_NAME));
    }

    @Test
    void testGetOrCreateCurrentEndpoint() {
        //when
        service.getOrCreateCurrentEndpoint(DECISION_NAME, VERSION, VERSION.getOwnerReference());
        networkingTestUtils.populateNetworkingResource(CURRENT_ENDPOINT_NAME, VERSION.getStatus().getKogitoServiceRef(), VERSION);
        NetworkResource networkResource = service.getOrCreate(CURRENT_ENDPOINT_NAME, VERSION, VERSION.getOwnerReference());

        //then
        assertThat(networkResource.getEndpoint(), is(NetworkingTestConstants.HTTP_HOST + CURRENT_ENDPOINT_NAME));
        assertThat(networkResource.getKogitoServiceRef(), is(VERSION.getStatus().getKogitoServiceRef()));
    }

    @Test
    void testGetOrCreateVersionEndpoint() {
        //when
        service.getOrCreateVersionEndpoint(VERSION, VERSION.getOwnerReference());
        networkingTestUtils.populateNetworkingResource(VERSION.getMetadata().getName(), VERSION.getStatus().getKogitoServiceRef(), VERSION);
        NetworkResource currentEndpointPopulated = service.getOrCreateVersionEndpoint(VERSION, VERSION.getOwnerReference());

        //then
        assertThat(currentEndpointPopulated.getEndpoint(), is(NetworkingTestConstants.HTTP_HOST + VERSION.getMetadata().getName()));
        assertThat(currentEndpointPopulated.getKogitoServiceRef(), is(VERSION.getStatus().getKogitoServiceRef()));
    }

    @Test
    void testCreateOrUpdateResourceIsNotReady() {
        //when
        service.createOrUpdate(CURRENT_ENDPOINT_NAME, VERSION, VERSION.getOwnerReference());
        NetworkResource networkResource = service.getOrCreate(DECISION_NAME, VERSION, VERSION.getOwnerReference());

        //then
        assertThat(networkResource, nullValue());
    }

    @Test
    void testDelete() {
        //when
        service.createOrUpdate(DECISION_NAME, VERSION, VERSION.getOwnerReference());
        boolean deleted = service.delete(DECISION_NAME, VERSION.getMetadata().getNamespace());

        //then
        assertThat(deleted, is(true));
        assertThat(networkingTestUtils.getNetworkingResourceSize(VERSION.getMetadata().getNamespace()), is(0));
    }

    @Test
    void testLabels() {
        //when
        service.createOrUpdate(DECISION_NAME, VERSION, VERSION.getOwnerReference());

        //then
        assertThat(networkingTestUtils.getLabel(DECISION_NAME, DECISION_LABEL, VERSION), is("some-decision"));
        assertThat(networkingTestUtils.getLabel(DECISION_NAME, DECISION_VERSION_LABEL, VERSION), is(VERSION.getMetadata().getName()));
    }
}
