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

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.baaas.ccp.service.JsonResourceUtils.get;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getCondition;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getConditionStatus;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getLabel;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getLabels;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getName;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getNamespace;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getSpec;
import static org.kie.baaas.ccp.service.JsonResourceUtils.getStatus;

public class JsonResourceUtilsTest {

    @Test
    void testGet() {
        assertThat(get(null, "foo"), nullValue());
        assertThat(get(Json.createObjectBuilder().build(), "foo"), nullValue());
        assertThat(get(Json.createObjectBuilder().add("one", "two").build(), "foo"), nullValue());
        assertThat(get(Json.createObjectBuilder().add("bar", "baz").build(), "foo", "bar"), nullValue());
        assertThat(get(Json.createObjectBuilder().add("bar", "baz").build(), "bar", "foo"), nullValue());

        assertThat(get(Json.createObjectBuilder()
                        .add("foo", "val1").build(),
                "foo"), equalTo(Json.createValue("val1")));
        assertThat(get(Json.createObjectBuilder()
                .add("foo", Json.createObjectBuilder()
                        .add("bar", "val2").build())
                .build(), "foo", "bar"), equalTo(Json.createValue("val2")));
    }

    @Test
    void testGetName() {
        assertThat(getName(null), nullValue());
        assertThat(getName(Json.createObjectBuilder().build()), nullValue());
        assertThat(getName(buildBasicResource()), is("resourcename"));
    }

    @Test
    void testGetNamespace() {
        assertThat(getNamespace(null), nullValue());
        assertThat(getNamespace(Json.createObjectBuilder().build()), nullValue());
        assertThat(getNamespace(buildBasicResource()), is("resourcenamespace"));
    }

    @Test
    void testGetLabels() {
        assertThat(getLabels(null), nullValue());
        assertThat(getLabels(Json.createObjectBuilder().build()), nullValue());
        assertTrue(getLabels(buildBasicResource()).containsKey("label1"));
        assertThat(getLabels(buildBasicResource()).getString("label1"), is("labelvalue1"));
    }

    @Test
    void testGetLabel() {
        assertThat(getLabel(null, "foo"), nullValue());
        assertThat(getLabel(Json.createObjectBuilder().build(), "foo"), nullValue());
        assertThat(getLabel(buildBasicResource(), "label1"), is("labelvalue1"));
        assertThat(getLabel(buildBasicResource(), "label2"), nullValue());
    }


    @Test
    void testGetSpec() {
        assertThat(getSpec(null), nullValue());
        assertThat(getSpec(Json.createObjectBuilder().build()), nullValue());
        assertThat(getSpec(buildBasicResource()), equalTo(Json.createObjectBuilder().build()));
    }

    @Test
    void testGetStatus() {
        assertThat(getStatus(null), nullValue());
        assertThat(getStatus(Json.createObjectBuilder().build()), nullValue());
        assertThat(getStatus(buildBasicResource()), equalTo(Json.createObjectBuilder().build()));
    }

    @Test
    void testGetCondition() {
        assertThat(getCondition(null, "foo"), nullValue());
        assertThat(getCondition(Json.createObjectBuilder().build(), "foo"), nullValue());
        assertThat(getCondition(buildBasicResource(), "Condition1"), nullValue());
        assertThat(getCondition(buildBasicResourceWithConditions(), "Condition2"), nullValue());

        JsonObject expected = buildCondition("Condition1");
        assertThat(getCondition(buildBasicResourceWithConditions(), "Condition1"), equalTo(expected));
    }


    @Test
    void testGetConditionStatus() {
        assertFalse(getConditionStatus(null, "foo"));
        assertFalse(getConditionStatus(Json.createObjectBuilder().build(), "foo"));
        assertFalse(getConditionStatus(buildBasicResource(), "Condition1"));
        assertFalse(getConditionStatus(buildBasicResourceWithConditions(), "Condition2"));

        assertTrue(getConditionStatus(buildBasicResourceWithConditions(), "Condition1"));
    }

    @Test
    void testBuildParam() {

    }

    private static JsonObject buildBasicResource() {
        return Json.createObjectBuilder()
                .add("metadata", Json.createObjectBuilder()
                        .add("name", "resourcename")
                        .add("namespace", "resourcenamespace")
                        .add("labels", Json.createObjectBuilder().add("label1", "labelvalue1").build())
                        .build())
                .add("spec", Json.createObjectBuilder().build())
                .add("status", Json.createObjectBuilder().build())
                .build();
    }

    private static JsonObject buildBasicResourceWithConditions() {
        JsonArray conditions = Json.createArrayBuilder()
                .add(buildCondition("Condition1"))
                .build();
        return Json.createObjectBuilder(buildBasicResource())
                .add("status", Json.createObjectBuilder()
                        .add("conditions", conditions).build())
                .build();
    }

    private static JsonObject buildCondition(String name) {
        return Json.createObjectBuilder()
                .add("type", name)
                .add("reason", "Success")
                .add("status", "True")
                .build();
    }
}
