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

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.kie.baaas.dfs.model.Platform;
import org.kie.baaas.dfs.service.networking.NetworkingService;
import org.kie.baaas.dfs.service.networking.k8s.KubernetesNetworkingService;
import org.kie.baaas.dfs.service.networking.openshift.OpenshiftNetworkingService;

import io.fabric8.openshift.client.OpenShiftClient;

@Singleton
public class NetworkingServiceProducer {

    @Inject
    OpenShiftClient client;

    @Inject
    PlatformConfigProvider platformConfigProvider;

    @Produces
    public NetworkingService getService() {
        if (Platform.OPENSHIFT.equals(platformConfigProvider.getPlatform())) {
            return new OpenshiftNetworkingService(client, platformConfigProvider.getOpenshiftInternalRegistry());
        }
        return new KubernetesNetworkingService(client, platformConfigProvider.getKubernetesInternalRegistry());
    }
}
