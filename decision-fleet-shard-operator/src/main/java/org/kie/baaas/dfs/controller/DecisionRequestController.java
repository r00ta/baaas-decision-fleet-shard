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
package org.kie.baaas.dfs.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.kie.baaas.dfs.api.AdmissionStatus;
import org.kie.baaas.dfs.api.Decision;
import org.kie.baaas.dfs.api.DecisionBuilder;
import org.kie.baaas.dfs.api.DecisionRequest;
import org.kie.baaas.dfs.api.DecisionRequestSpec;
import org.kie.baaas.dfs.api.DecisionRequestStatus;
import org.kie.baaas.dfs.api.DecisionRequestStatusBuilder;
import org.kie.baaas.dfs.api.DecisionSpecBuilder;
import org.kie.baaas.dfs.api.DecisionVersion;
import org.kie.baaas.dfs.api.DecisionVersionRef;
import org.kie.baaas.dfs.api.DecisionVersionSpec;
import org.kie.baaas.dfs.api.Kafka;
import org.kie.baaas.dfs.api.Phase;
import org.kie.baaas.dfs.client.RemoteResourceClient;
import org.kie.baaas.dfs.model.DecisionValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;

import static org.kie.baaas.dfs.api.AdmissionStatus.REJECTED;
import static org.kie.baaas.dfs.api.DecisionConstants.CLIENTID_KEY;
import static org.kie.baaas.dfs.api.DecisionConstants.CLIENTSECRET_KEY;
import static org.kie.baaas.dfs.api.DecisionConstants.DUPLICATED_VERSION;
import static org.kie.baaas.dfs.api.DecisionConstants.SERVER_ERROR;
import static org.kie.baaas.dfs.api.DecisionConstants.VALIDATION_ERROR;
import static org.kie.baaas.dfs.api.DecisionConstants.VERSION_BUILD_FAILED;
import static org.kie.baaas.dfs.api.DecisionVersionStatus.REASON_FAILED;
import static org.kie.baaas.dfs.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.DECISION_REQUEST_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.dfs.controller.DecisionLabels.OPERATOR_NAME;

@Controller
@ApplicationScoped
public class DecisionRequestController implements ResourceController<DecisionRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionRequestController.class);
    private static final String BAAAS_NS_TEMPLATE = "baaas-%s";
    private static final String KAFKA_AUTH_SUFFIX = "-kafka-auth";

    @Inject
    KubernetesClient client;

    @Inject
    RemoteResourceClient resourceClient;

    @Inject
    Validator validator;

    public DeleteControl deleteResource(DecisionRequest request, Context<DecisionRequest> context) {
        LOGGER.info("Delete DecisionRequest: {} in namespace {}", request.getMetadata().getName(), request.getMetadata().getNamespace());
        return DeleteControl.DEFAULT_DELETE;
    }

    public UpdateControl<DecisionRequest> createOrUpdateResource(DecisionRequest request, Context<DecisionRequest> context) {
        LOGGER.info("Create or update DecisionRequest: {} in namespace {}", request.getMetadata().getName(), request.getMetadata().getNamespace());
        String targetNamespace = getTargetNamespace(request);
        try {
            validateSpec(request.getSpec(), targetNamespace);
        } catch (DecisionValidationException e) {
            request.setStatus(new DecisionRequestStatusBuilder()
                    .withReason(e.getReason())
                    .withMessage(e.getMessage())
                    .withState(REJECTED)
                    .build());
            resourceClient.notify(request, e.getMessage(), Phase.FAILED);
            return UpdateControl.updateStatusSubResource(request);
        }
        try {
            Namespace targetNs = client.namespaces().withName(targetNamespace).get();
            if (targetNs == null) {
                client.namespaces()
                        .create(new NamespaceBuilder().withNewMetadata().withName(targetNamespace).endMetadata().build());
            }
            if (request.getSpec().getKafka() != null) {
                createOrUpdateKafkaAuthSecret(request);
            }
            Decision decision = createOrUpdateDecision(request, targetNamespace);
            return updateSuccessRequestStatus(request, decision);
        } catch (KubernetesClientException e) {
            LOGGER.error("Unable to process DecisionRequest", e);
            request.setStatus(new DecisionRequestStatusBuilder()
                    .withReason(SERVER_ERROR)
                    .withMessage(e.getMessage())
                    .withState(REJECTED)
                    .build());
            resourceClient.notify(request, e.getMessage(), Phase.FAILED);
            return UpdateControl.updateStatusSubResource(request);
        }
    }

    //TODO: Fix - Validator cannot discover the annotated classes
    private void validateSpec(DecisionRequestSpec spec, String namespace) throws DecisionValidationException {
        //TODO: Remove once validator is fixed
        if (spec.getCustomerId() == null || spec.getCustomerId().isBlank()) {
            throw new DecisionValidationException(VALIDATION_ERROR, "Invalid spec: customerId must not be blank");
        }
        // End
        Set<ConstraintViolation<DecisionRequestSpec>> violations = validator.validate(spec);
        if (!violations.isEmpty()) {
            throw new DecisionValidationException(VALIDATION_ERROR, "Invalid spec: " +
                    violations.stream()
                            .map(v -> v.getPropertyPath() + " " + v.getMessage())
                            .collect(Collectors.joining(",")));
        }
        if (!KubernetesResourceUtil.isValidName(namespace)) {
            throw new DecisionValidationException(VALIDATION_ERROR, "Invalid target namespace: " + namespace);
        }
        validateVersion(spec, namespace);
    }

    private void validateVersion(DecisionRequestSpec spec, String namespace) throws DecisionValidationException {
        List<DecisionVersion> versions = client.customResources(DecisionVersion.class)
                .inNamespace(namespace)
                .withLabel(DECISION_LABEL, spec.getName())
                .list()
                .getItems().stream().filter(v -> Objects.equals(v.getSpec().getVersion(), spec.getVersion())).collect(Collectors.toList());
        for (DecisionVersion v : versions) {
            if (REASON_FAILED.equals(v.getStatus().getBuildStatus())) {
                throw new DecisionValidationException(VERSION_BUILD_FAILED, "Requested DecisionVersion build failed");
            }
            if (!v.getSpec().equals(toDefinition(spec))) {
                throw new DecisionValidationException(DUPLICATED_VERSION, "The provided version already exists with a different spec");
            }
        }
    }

    private String getTargetNamespace(DecisionRequest decision) {
        return String.format(BAAAS_NS_TEMPLATE, decision.getSpec().getCustomerId());
    }

    private Decision createOrUpdateDecision(DecisionRequest request, String namespace) {
        Decision expected = new DecisionBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(request.getSpec().getName())
                        .withNamespace(namespace)
                        .addToLabels(DECISION_REQUEST_LABEL, request.getMetadata().getName())
                        .addToLabels(CUSTOMER_LABEL, request.getSpec().getCustomerId())
                        .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                        .build())
                .withSpec(new DecisionSpecBuilder()
                        .withDefinition(toDefinition(request.getSpec()))
                        .withWebhooks(request.getSpec().getWebhooks())
                        .build())
                .build();
        Decision current = client.customResources(Decision.class)
                .inNamespace(namespace)
                .withName(request.getSpec().getName())
                .get();
        if (current == null || !Objects.equals(expected.getSpec(), current.getSpec())) {
            return client.customResources(Decision.class)
                    .inNamespace(namespace)
                    .withName(expected.getMetadata().getName())
                    .createOrReplace(expected);
        }
        return current;
    }

    private UpdateControl<DecisionRequest> updateSuccessRequestStatus(DecisionRequest request, Decision decision) {
        DecisionRequestStatus expected = new DecisionRequestStatusBuilder()
                .withState(AdmissionStatus.SUCCESS)
                .withVersionRef(new DecisionVersionRef()
                        .setName(decision.getMetadata().getName())
                        .setNamespace(decision.getMetadata().getNamespace())
                        .setVersion(request.getSpec().getVersion()))
                .build();
        if (request.getStatus() == null || !expected.equals(request.getStatus())) {
            request.setStatus(expected);
            return UpdateControl.updateStatusSubResource(request);
        }
        return UpdateControl.noUpdate();
    }

    private void createOrUpdateKafkaAuthSecret(DecisionRequest request) {
        String namespace = getTargetNamespace(request);
        String kafkaSecretName = getKafkaSecretName(request.getSpec());
        Secret current = client.secrets()
                .inNamespace(namespace)
                .withName(kafkaSecretName)
                .get();

        Secret expected = new SecretBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withNamespace(namespace)
                        .withName(kafkaSecretName)
                        .addToLabels(CUSTOMER_LABEL, request.getSpec().getCustomerId())
                        .addToLabels(MANAGED_BY_LABEL, OPERATOR_NAME)
                        .build())
                .withStringData(Map.of(
                        CLIENTID_KEY, request.getSpec().getKafka().getCredential().getClientId(),
                        CLIENTSECRET_KEY, request.getSpec().getKafka().getCredential().getClientSecret()))
                .build();
        if (current == null || !Objects.equals(current.getStringData(), expected.getStringData())) {
            LOGGER.debug("Create or replace kafka-auth secret {} in {}", expected.getMetadata().getName(), expected.getMetadata().getNamespace());
            client.secrets().inNamespace(namespace).createOrReplace(expected);
        }
    }

    static DecisionVersionSpec toDefinition(DecisionRequestSpec reqSpec) {
        if (reqSpec == null) {
            return null;
        }
        DecisionVersionSpec spec = new DecisionVersionSpec()
                .setSource(reqSpec.getSource())
                .setVersion(reqSpec.getVersion())
                .setEnv(reqSpec.getEnv());
        if (reqSpec.getKafka() != null) {
            spec.setKafka(new Kafka()
                    .setBootstrapServers(reqSpec.getKafka().getBootstrapServers())
                    .setInputTopic(reqSpec.getKafka().getInputTopic())
                    .setOutputTopic(reqSpec.getKafka().getOutputTopic())
                    .setSecretName(getKafkaSecretName(reqSpec)));
        }
        return spec;
    }

    private static String getKafkaSecretName(DecisionRequestSpec request) {
        return request.getCustomerId() + KAFKA_AUTH_SUFFIX;
    }
}
