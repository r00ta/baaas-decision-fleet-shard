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

import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import org.kie.baaas.api.AdmissionStatus;
import org.kie.baaas.api.Decision;
import org.kie.baaas.api.DecisionBuilder;
import org.kie.baaas.api.DecisionRequest;
import org.kie.baaas.api.DecisionRequestStatusBuilder;
import org.kie.baaas.api.DecisionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@ApplicationScoped
public class DecisionRequestController implements ResourceController<DecisionRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionRequestController.class);
    private static final String BAAAS_NS_TEMPLATE = "baaas-%s";
    private static final String DECISION_REQUEST_ANNOTATION = "org.kie.baaas.decisionrequest";

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    Validator validator;

    public DeleteControl deleteResource(DecisionRequest request, Context<DecisionRequest> context) {
        LOGGER.info("Delete DecisionRequest: {} in namespace {}", request.getMetadata().getName(), request.getMetadata().getNamespace());
        return DeleteControl.DEFAULT_DELETE;
    }

    public UpdateControl<DecisionRequest> createOrUpdateResource(DecisionRequest request, Context<DecisionRequest> context) {
        LOGGER.info("Create or update DecisionRequest: {} in namespace {}", request.getMetadata().getName(), request.getMetadata().getNamespace());
        String targetNamespace = getTargetNamespace(request);
        String validationError = validateSpec(request.getSpec(), targetNamespace);
        if (validationError != null) {
            request.setStatus(new DecisionRequestStatusBuilder()
                    .withMessage(validationError)
                    .withAdmission(AdmissionStatus.REJECTED)
                    .build());
            return UpdateControl.updateCustomResource(request);
        }
        return createOrUpdateDecision(request, targetNamespace);
    }

    private String validateSpec(DecisionSpec spec, String namespace) {
        if (!KubernetesResourceUtil.isValidName(namespace)) {
            return "Invalid target namespace: " + namespace;
        }
        Set<ConstraintViolation<DecisionSpec>> violations = validator.validate(spec);
        if (violations.isEmpty()) {
            return null;
        }
        return "Invalid spec: " +
                violations.stream()
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .collect(Collectors.joining(","));
    }

    private String getTargetNamespace(DecisionRequest decision) {
        return String.format(BAAAS_NS_TEMPLATE, decision.getSpec().getCustomerId());
    }

    private UpdateControl<DecisionRequest> createOrUpdateDecision(DecisionRequest request, String namespace) {
        Namespace targetNs = kubernetesClient.namespaces().withName(namespace).get();
        try {
            if (targetNs == null) {
                kubernetesClient.namespaces()
                        .create(new NamespaceBuilder().withNewMetadata().withName(namespace).endMetadata().build());
            }
            Decision decision = new DecisionBuilder()
                    .withMetadata(new ObjectMetaBuilder()
                            .withName(request.getMetadata().getName())
                            .withNamespace(namespace)
                            .addToAnnotations(DECISION_REQUEST_ANNOTATION, request.getMetadata().getUid())
                            .build())
                    .withSpec(request.getSpec())
                    .build();
            Decision current = kubernetesClient.customResources(Decision.class).inNamespace(namespace).withName(request.getMetadata().getName()).get();
            if (current == null || decision.getSpec().needsUpdate(current.getSpec())) {
                kubernetesClient.customResources(Decision.class)
                        .inNamespace(namespace)
                        .withName(decision.getMetadata().getName())
                        .createOrReplace(decision);
            }
        } catch (KubernetesClientException e) {
            request.setStatus(new DecisionRequestStatusBuilder()
                    .withAdmission(AdmissionStatus.REJECTED)
                    .withMessage(e.getMessage())
                    .build());
            return UpdateControl.updateCustomResource(request);
        }
        if (request.getStatus() == null || AdmissionStatus.REJECTED.equals(request.getStatus().getAdmission())) {
            request.setStatus(new DecisionRequestStatusBuilder()
                    .withAdmission(AdmissionStatus.SUCCESS)
                    .withDecisionName(request.getMetadata().getName())
                    .withDecisionNamespace(namespace)
                    .build());
            return UpdateControl.updateCustomResource(request);
        }
        return UpdateControl.noUpdate();
    }
}
