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

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.baaas.dfs.model.Platform;

@Singleton
public class PlatformConfigProvider {

    private Platform platform;

    @ConfigProperty(name = "baaas.k8s.platform")
    String platformConfig;

    @ConfigProperty(name = "baaas.k8s.registry")
    String kubernetesInternalRegistry;

    @ConfigProperty(name = "baaas.openshift.registry")
    String openshiftInternalRegistry;

    @PostConstruct
    void init() {

        if (platformConfig == null) {
            throw new IllegalStateException("baaas.k8s.platform configuration must be provided.");
        }

        try {
            this.platform = Platform.parse(platformConfig);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("baaas.k8s.platform configuration not recognized. Options are [%s]", Arrays.stream(Platform.values()).map(Platform::toString).collect(Collectors.joining(","))));
        }

        if (Platform.OPENSHIFT.equals(this.platform) && openshiftInternalRegistry == null) {
            throw new IllegalStateException("baaas.openshift.registry configuration must be provided.");
        }
        if (Platform.KUBERNETES.equals(this.platform) && kubernetesInternalRegistry == null) {
            throw new IllegalStateException("baaas.k8s.registry configuration must be provided.");
        }
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getKubernetesInternalRegistry() {
        return kubernetesInternalRegistry;
    }

    public String getOpenshiftInternalRegistry() {
        return openshiftInternalRegistry;
    }
}
