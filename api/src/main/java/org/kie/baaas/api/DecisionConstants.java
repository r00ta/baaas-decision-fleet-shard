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

package org.kie.baaas.api;

public interface DecisionConstants {

    String GROUP = "operator.baaas";
    String VERSION = "v1alpha1";
    int CODE_100 = 100;
    String CODE_100_MESSAGE = "OK";
    int CODE_200 = 200;
    String CODE_200_MESSAGE = "Client Error";
    int CODE_201 = 201;
    String CODE_201_MESSAGE = "Validation Error";
    int CODE_300 = 300;
    String CODE_300_MESSAGE = "Server Error";

}
