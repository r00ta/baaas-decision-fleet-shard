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
package org.kie.baaas.ccp.service;

import java.io.StringReader;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.kie.baaas.ccp.api.DecisionVersion;
import org.kie.baaas.ccp.api.DecisionVersionSpec;

import static org.kie.baaas.ccp.controller.DecisionLabels.BAAAS_RESOURCE_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.BAAAS_RESOURCE_PIPELINE_RUN;
import static org.kie.baaas.ccp.controller.DecisionLabels.CUSTOMER_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_NAMESPACE_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.DECISION_VERSION_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.MANAGED_BY_LABEL;
import static org.kie.baaas.ccp.controller.DecisionLabels.OPERATOR_NAME;
import static org.kie.baaas.ccp.service.JsonResourceUtils.buildEnvValue;

@ApplicationScoped
public class PipelineService {

    private static final String PIPELINE_REF = "baaas-ccp-decision-build";
    private static final String VAR_POM_CONFIGMAP = "BUILD_INPUT_POM_XML_CONFIGMAP";
    private static final String VAR_DMN_LOCATION = "BUILD_INPUT_DMN_LOCATION";
    private static final String VAR_REGISTRY_LOCATION = "BUILD_OUTPUT_REGISTRY_PUSH_LOCATION";
    private static final String KAFKA_POM_XML_CONFIGMAP = "baaas-ccp-build-pom-kafka-xml";
    private static final String POM_XML_CONFIGMAP = "baaas-ccp-build-pom-xml";
    private static final String IMAGE_REF_TEMPLATE = "quay.io/%s/baaas-decision-builds:%s-%s-%s";

    public static final CustomResourceDefinitionContext PIPELINE_RUN_CONTEXT = new CustomResourceDefinitionContext
            .Builder()
            .withGroup("tekton.dev")
            .withVersion("v1beta1")
            .withName("pipelineruns.tekton.dev")
            .withKind("PipelineRun")
            .withPlural("pipelineruns")
            .withScope("Namespaced")
            .build();

    public static final String PIPELINE_REASON = "reason";
    public static final String PIPELINE_MESSAGE = "message";
    public static final String PIPELINE_REASON_FAILED = "Failed";
    public static final String PIPELINE_SUCCEEDED = "Succeeded";
    public static final String PIPELINE_REASON_RUNNING = "Running";

    public static String getPipelineRunName(DecisionVersion version) {
        return version.getMetadata().getLabels().get(CUSTOMER_LABEL) + "-" + version.getMetadata().getName();
    }

    public static JsonObject buildPipelineRun(String namespace, DecisionVersion version) {
        JsonArrayBuilder ownerRefs = Json.createArrayBuilder()
                .add(Json.createReader(new StringReader(Serialization.asJson(version.getOwnerReference()))).readObject());
        return Json.createObjectBuilder()
                .add("apiVersion", PIPELINE_RUN_CONTEXT.getGroup() + "/" + PIPELINE_RUN_CONTEXT.getVersion())
                .add("kind", PIPELINE_RUN_CONTEXT.getKind())
                .add("metadata", Json.createObjectBuilder()
                        .add("name", getPipelineRunName(version))
                        .add("namespace", namespace)
                        .add("labels", Json.createObjectBuilder()
                                .add(BAAAS_RESOURCE_LABEL, BAAAS_RESOURCE_PIPELINE_RUN)
                                .add(DECISION_VERSION_LABEL, version.getMetadata().getName())
                                .add(DECISION_LABEL, version.getMetadata().getLabels().get(DECISION_LABEL))
                                .add(CUSTOMER_LABEL, version.getMetadata().getLabels().get(CUSTOMER_LABEL))
                                .add(DECISION_NAMESPACE_LABEL, version.getMetadata().getNamespace())
                                .add(MANAGED_BY_LABEL, OPERATOR_NAME)
                                .build())
                        .add("ownerReferences", ownerRefs)
                        .build())
                .add("spec", Json.createObjectBuilder()
                        .add("pipelineRef", Json.createObjectBuilder()
                                .add("name", PIPELINE_REF)
                                .build())
                        .add("params", Json.createArrayBuilder()
                                .add(buildEnvValue(VAR_POM_CONFIGMAP, getConfigMapName(version.getSpec())))
                                .add(buildEnvValue(VAR_DMN_LOCATION, version.getSpec().getSource().toString()))
                                .add(buildEnvValue(VAR_REGISTRY_LOCATION, buildImageRef(version)))
                                .build())
                        .add("podTemplate", Json.createObjectBuilder()
                                .add("securityContext", Json.createObjectBuilder()
                                        .add("fsGroup", 1001)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public static String getConfigMapName(DecisionVersionSpec spec) {
        if (spec.getKafka() != null && spec.getKafka().getInputTopic() != null) {
            return KAFKA_POM_XML_CONFIGMAP;
        }
        return POM_XML_CONFIGMAP;
    }

    public static String buildImageRef(DecisionVersion version) {
        return String.format(IMAGE_REF_TEMPLATE,
                //TODO: changeme
                "ruben",
                version.getMetadata().getNamespace(),
                version.getMetadata().getLabels().get(DECISION_LABEL),
                version.getSpec().getVersion());
    }
}
