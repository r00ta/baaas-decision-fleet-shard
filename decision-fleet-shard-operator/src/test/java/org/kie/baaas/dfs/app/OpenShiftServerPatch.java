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

import java.util.HashMap;
import java.util.Queue;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.ServerRequest;
import io.fabric8.mockwebserver.ServerResponse;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftMockServer;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;

import okhttp3.mockwebserver.MockWebServer;

// See https://issues.redhat.com/browse/BAAAS-250
public class OpenShiftServerPatch extends OpenShiftServer {
    private NamespacedOpenShiftClient client;

    private boolean https;
    // In this mode the mock web server will store, read, update and delete
    // kubernetes resources using an in memory map and will appear as a real api
    // server.
    private boolean crudMode;

    public OpenShiftServerPatch(boolean https, boolean crudMode) {
        super(https, crudMode);
        this.https = https;
        this.crudMode = crudMode;
    }

    @Override
    public void before() {
        mock = crudMode
                ? new OpenShiftMockServer(new Context(), new MockWebServer(), new HashMap<ServerRequest, Queue<ServerResponse>>(), new KubernetesCrudDispatcher(), this.https)
                : new OpenShiftMockServer(https);
        mock.init();
        this.client = mock.createOpenShiftClient();
    }

    @Override
    public void after() {
        mock.destroy();
        client.close();
    }

    @Override
    public KubernetesClient getKubernetesClient() {
        return client;
    }

    @Override
    public NamespacedOpenShiftClient getOpenshiftClient() {
        return client;
    }
}
