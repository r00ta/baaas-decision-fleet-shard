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
    private static final String DECISION_REQUEST_LABEL = "org.kie.baaas.decisionrequest";

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
            return UpdateControl.updateStatusSubResource(request);
        }
        try {
            Decision decision = createOrUpdateDecision(request, targetNamespace);
            return updateSuccessRequestStatus(request, decision);
        } catch (KubernetesClientException e) {
            request.setStatus(new DecisionRequestStatusBuilder()
                    .withMessage(e.getMessage())
                    .withAdmission(AdmissionStatus.REJECTED)
                    .build());
            return UpdateControl.updateStatusSubResource(request);
        }
    }

    //TODO: Fix - Validator cannot discover the annotated classes
    private String validateSpec(DecisionSpec spec, String namespace) {
        //TODO: Remove once validator is fixed
        if (spec.getCustomerId() == null || spec.getCustomerId().isBlank()) {
            return "Invalid spec: customerId must not be blank";
        }
        // End
        Set<ConstraintViolation<DecisionSpec>> violations = validator.validate(spec);
        if (!violations.isEmpty()) {
            return "Invalid spec: " +
                    violations.stream()
                            .map(v -> v.getPropertyPath() + " " + v.getMessage())
                            .collect(Collectors.joining(","));
        }
        if (!KubernetesResourceUtil.isValidName(namespace)) {
            return "Invalid target namespace: " + namespace;
        }
        return null;
    }

    private String getTargetNamespace(DecisionRequest decision) {
        return String.format(BAAAS_NS_TEMPLATE, decision.getSpec().getCustomerId());
    }

    private Decision createOrUpdateDecision(DecisionRequest request, String namespace) {
        Namespace targetNs = kubernetesClient.namespaces().withName(namespace).get();
        if (targetNs == null) {
            kubernetesClient.namespaces()
                    .create(new NamespaceBuilder().withNewMetadata().withName(namespace).endMetadata().build());
        }
        Decision expected = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(request.getMetadata().getName())
                        .withNamespace(namespace)
                        .addToLabels(DECISION_REQUEST_LABEL, request.getMetadata().getUid())
                        .build())
                .withSpec(request.getSpec())
                .build();
        Decision current = kubernetesClient.customResources(Decision.class).inNamespace(namespace).withName(request.getMetadata().getName()).get();
        if (current != null && expected.getSpec().equals(current.getSpec())) {
            return null;
        }
        kubernetesClient.customResources(Decision.class)
                .inNamespace(namespace)
                .withName(expected.getMetadata().getName())
                .createOrReplace(expected);
        return expected;
    }

    private UpdateControl<DecisionRequest> updateSuccessRequestStatus(DecisionRequest request, Decision decision) {
        if(decision == null) {
            return UpdateControl.noUpdate();
        }
        if (request.getStatus() == null || (AdmissionStatus.SUCCESS.equals(request.getStatus().getAdmission()) &&
                decision.getMetadata().getName().equals(request.getStatus().getDecisionName()) &&
                decision.getMetadata().getNamespace().equals(request.getStatus().getDecisionNamespace()))) {
            request.setStatus(new DecisionRequestStatusBuilder()
                    .withAdmission(AdmissionStatus.SUCCESS)
                    .withDecisionName(decision.getMetadata().getName())
                    .withDecisionNamespace(decision.getMetadata().getNamespace())
                    .build());
            return UpdateControl.updateStatusSubResource(request);
        }
        return UpdateControl.noUpdate();
    }
}
