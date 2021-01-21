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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import org.kie.baaas.api.Decision;
import org.kie.baaas.ccp.client.RemoteResourceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@ApplicationScoped
public class DecisionController implements ResourceController<Decision> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionController.class);

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    RemoteResourceClient resourceClient;

    public DeleteControl deleteResource(Decision decision, Context<Decision> context) {
        LOGGER.info("Create or update Decision: {} in namespace {}", decision.getMetadata().getName(), decision.getMetadata().getNamespace());
        return DeleteControl.DEFAULT_DELETE;
    }

    public UpdateControl<Decision> createOrUpdateResource(Decision Decision, Context<Decision> context) {
        LOGGER.info("Create or update Decision: {} in namespace {}", Decision.getMetadata().getName(), Decision.getMetadata().getNamespace());
        //ensureResources();
        return UpdateControl.noUpdate();
    }
}
