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
package org.kie.baaas.dfs.model;

public class NetworkResource {

    private final String kogitoServiceRef;

    private final String endpoint;

    public NetworkResource(String endpoint, String kogitoServiceRef) {
        this.endpoint = endpoint;
        this.kogitoServiceRef = kogitoServiceRef;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getKogitoServiceRef() {
        return kogitoServiceRef;
    }
}
