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
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import io.fabric8.kubernetes.client.utils.Serialization;

public class JsonResourceUtils {

    private static final String STATUS = "status";
    private static final String TYPE = "type";
    private static final String METADATA = "metadata";
    private static final String LABELS = "labels";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String SPEC = "spec";
    private static final String NAMESPACE = "namespace";
    private static final String CONDITIONS = "conditions";
    private static final String KEY = "key";
    private static final String SECRET_KEY_REF = "secretKeyRef";
    private static final String VALUE_FROM = "valueFrom";
    private static final String UID = "uid";
    private static final String RESOURCE_VERSION = "resourceVersion";
    private static final String OWNER_REFERENCES = "ownerReferences";

    private JsonResourceUtils() {
    }

    public static JsonObject getStatus(JsonObject object) {
        JsonValue value = get(object, STATUS);
        if (value != null) {
            return value.asJsonObject();
        }
        return null;
    }

    public static JsonObject getCondition(JsonObject object, String type) {
        JsonArray conditions = getConditions(object);
        if (conditions == null) {
            return null;
        }
        Optional<JsonValue> condition = conditions.stream()
                .filter(c -> type.equals(c.asJsonObject().getString(TYPE)))
                .findFirst();
        if (condition.isEmpty()) {
            return null;
        }
        return condition.get().asJsonObject();
    }

    public static Boolean getConditionStatus(JsonObject object, String type) {
        JsonArray conditions = getConditions(object);
        if (conditions == null) {
            return Boolean.FALSE;
        }
        Optional<JsonValue> condition = conditions.stream()
                .filter(c -> type.equals(c.asJsonObject().getString(TYPE)))
                .findFirst();
        if (condition.isEmpty()) {
            return Boolean.FALSE;
        }
        return Boolean.parseBoolean(condition.get().asJsonObject().getString(STATUS));
    }

    public static JsonArray getConditions(JsonObject object) {
        JsonValue value = get(object, STATUS, CONDITIONS);
        if (value != null) {
            return value.asJsonArray();
        }
        return null;
    }

    public static JsonObject getLabels(JsonObject object) {
        JsonValue value = get(object, METADATA, LABELS);
        if (value != null) {
            return value.asJsonObject();
        }
        return null;
    }

    public static String getLabel(JsonObject object, String labelName) {
        JsonValue value = get(object, METADATA, LABELS);
        if (value != null && value.asJsonObject().containsKey(labelName)) {
            return value.asJsonObject().getString(labelName);
        }
        return null;
    }

    public static String getName(JsonObject object) {
        JsonValue value = get(object, METADATA);
        if (value != null) {
            return value.asJsonObject().getString(NAME);
        }
        return null;
    }

    public static String getNamespace(JsonObject object) {
        JsonValue value = get(object, METADATA);
        if (value != null) {
            return value.asJsonObject().getString(NAMESPACE);
        }
        return null;
    }

    public static JsonValue get(JsonObject object, String... path) {
        if (path == null) {
            return object;
        }
        if (object == null) {
            return null;
        }
        JsonObject result = object;
        for (int i = 0; i < path.length - 1; i++) {
            JsonValue value = result.get(path[i]);
            if (value == null || !JsonValue.ValueType.OBJECT.equals(value.getValueType())) {
                return null;
            }
            result = value.asJsonObject();
        }
        return result.get(path[path.length - 1]);
    }

    public static JsonObject buildEnvValue(String name, String value) {
        return Json.createObjectBuilder()
                .add(NAME, name)
                .add(VALUE, value)
                .build();
    }

    public static JsonObject buildEnvValueFromSecret(String name, String secretName, String secretPropertyName) {
        return Json.createObjectBuilder()
                .add(NAME, name)
                .add(VALUE_FROM, Json.createObjectBuilder()
                        .add(SECRET_KEY_REF, Json.createObjectBuilder()
                                .add(KEY, secretName)
                                .add(NAME, secretPropertyName)
                                .build())
                        .build())
                .build();
    }

    public static JsonObject getSpec(JsonObject object) {
        JsonValue value = get(object, SPEC);
        if (value == null) {
            return null;
        }
        return value.asJsonObject();
    }

    public static String getUID(JsonObject object) {
        JsonValue value = get(object, METADATA);
        if (value != null) {
            return value.asJsonObject().getString(UID);
        }
        return null;
    }

    public static String getResourceVersion(JsonObject object) {
        JsonValue value = get(object, METADATA);
        if (value != null) {
            return value.asJsonObject().getString(RESOURCE_VERSION);
        }
        return null;
    }

    public static String getOwnerUid(JsonObject resource) {
        JsonValue ownerRefs = get(resource, METADATA, OWNER_REFERENCES);
        if (ownerRefs == null || ownerRefs.asJsonArray().isEmpty()) {
            return null;
        }
        return ownerRefs.asJsonArray().getJsonObject(0).getString(UID);
    }

    public static JsonObject toJson(Object object) {
        return Json.createReader(new StringReader(Serialization.asJson(object))).readObject();
    }
}
