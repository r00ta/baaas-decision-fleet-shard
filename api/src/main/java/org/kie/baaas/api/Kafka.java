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

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.sundr.builder.annotations.Buildable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, generateBuilderPackage = true, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
@JsonPropertyOrder({
        "host",
        "secretName",
        "inputTopic",
        "outputTopic"
})
@ToString
@EqualsAndHashCode
@Getter
@Setter
@Accessors(chain = true)
public class Kafka {

    @NotBlank
    private String host;
    @NotBlank
    private String secretName;
    @NotBlank
    private String inputTopic;
    @NotBlank
    private String outputTopic;

}
