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

package org.kie.baaas.ccp.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

@Group(DecisionConstants.GROUP)
@Version(DecisionConstants.VERSION)
@Buildable(editableEnabled = false, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", refs = {
        @BuildableReference(CustomResource.class),
})
public class Decision extends CustomResource<DecisionSpec, DecisionStatus> implements Namespaced {

    public Decision() {
        super();
        this.setStatus(new DecisionStatus());
    }

    @JsonIgnore
    public OwnerReference getOwnerReference() {
        return new OwnerReferenceBuilder()
                .withApiVersion(getApiVersion())
                .withKind(getKind())
                .withName(getMetadata().getName())
                .withUid(getMetadata().getUid())
                .withController(Boolean.TRUE)
                .build();
    }

}
