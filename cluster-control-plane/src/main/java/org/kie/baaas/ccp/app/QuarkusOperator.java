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

package org.kie.baaas.ccp.app;

import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.config.ConfigurationService;
import io.javaoperatorsdk.operator.api.config.ControllerConfiguration;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.kie.baaas.api.Decision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class QuarkusOperator implements QuarkusApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusOperator.class);

    @Inject
    KubernetesClient client;

    @Inject
    Operator operator;

    @Inject
    ConfigurationService configuration;

    @Inject
    ResourceController<Decision> decisionController;

    public static void main(String... args) {
        Quarkus.run(QuarkusOperator.class, args);
    }

    public int run(String... args) {
        ControllerConfiguration<Decision> controllerConfig = configuration.getConfigurationFor(decisionController);
        LOGGER.info("CR: {}", controllerConfig.getCustomResourceClass());

        Quarkus.waitForExit();
        return 0;
    }
}
