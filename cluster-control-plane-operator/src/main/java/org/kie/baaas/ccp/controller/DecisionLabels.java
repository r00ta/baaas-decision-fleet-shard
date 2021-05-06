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

public class DecisionLabels {

    public static final String MANAGED_BY_LABEL = "app.kubernetes.io/managed-by";
    public static final String OPERATOR_NAME = "cluster-control-plane-operator";
    public static final String DECISION_LABEL = "org.kie.baaas/decision";
    public static final String DECISION_NAMESPACE_LABEL = "org.kie.baaas/decisionnamespace";
    public static final String DECISION_REQUEST_LABEL = "org.kie.baaas/decisionrequest";
    public static final String DECISION_VERSION_LABEL = "org.kie.baaas/decisionversion";
    public static final String OWNER_UID_LABEL = "org.kie.baaas/owneruid";
    public static final String CUSTOMER_LABEL = "org.kie.baaas/customer";
    public static final String BAAAS_RESOURCE_LABEL = "org.kie.baaas/resource";
    public static final String BAAAS_RESOURCE_KOGITO_SERVICE = "kogitoservice";
    public static final String BAAAS_RESOURCE_PIPELINE_RUN = "pipelinerun";

    private DecisionLabels() {
    }
}
