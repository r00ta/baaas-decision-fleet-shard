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
package org.kie.baaas.dfs.app;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import io.quarkus.test.Mock;

@Mock
@Singleton
public class ClientProducerMock implements ClientProducer {

    private OpenShiftServer mockOpenshiftServer;

    private OpenShiftClient client;

    @PostConstruct
    protected void init() {
        this.mockOpenshiftServer = new OpenShiftServerPatch(false, true);
        this.mockOpenshiftServer.before();
        this.client = mockOpenshiftServer.getOpenshiftClient();
    }

    @Produces
    OpenShiftServer getMockOpenshiftServer() {
        return mockOpenshiftServer;
    }

    @Override
    @Produces
    public OpenShiftClient produceClient() {
        return client;
    }
}
