/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.unitvectory.jsonschema4springboot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * The JsonSchemaLookupDefault test cases.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class JsonSchemaLookupDefaultTest {

    @Test
    public void loadSchemaTest() {
        JsonSchema jsonSchema = JsonSchemaLookupDefault.newInstance().getSchema(VersionFlag.V7,
                "simpleschemaV7.json");
        assertNotNull(jsonSchema);
    }

    @Test
    public void loadSchemaNotExistTest() {
        LoadJsonSchemaException thrown = assertThrows(LoadJsonSchemaException.class,
                () -> JsonSchemaLookupDefault.newInstance().getSchema(VersionFlag.V7,
                        "doesnotexist.json"),
                "Expected getSchema to throw exception for file that does not exit");

        assertEquals("JSON Schema not found at path: doesnotexist.json", thrown.getMessage());
    }

    @Test
    public void loadSchemaInvalidSchemaTest() {
        LoadJsonSchemaException thrown = assertThrows(LoadJsonSchemaException.class,
                () -> JsonSchemaLookupDefault.newInstance().getSchema(VersionFlag.V7,
                        "notaschemafile"),
                "Expected getSchema to throw exception for file is not a valid schema");

        assertEquals("JSON Schema failed to load from path: notaschemafile", thrown.getMessage());
    }

    @Test
    public void defaultResourceLoaderTest() {
        JsonSchema jsonSchema = JsonSchemaLookupDefault.newInstance(new DefaultResourceLoader())
                .getSchema(VersionFlag.V7, "simpleschemaV7.json");
        assertNotNull(jsonSchema);
    }

    @Test
    public void instanceNullTest() {
        ResourceLoader resourceLoader = null;
        NullPointerException thrown = assertThrows(NullPointerException.class,
                () -> JsonSchemaLookupDefault.newInstance(resourceLoader),
                "New instance must be null");

        assertEquals("resourceLoader is marked non-null but is null", thrown.getMessage());
    }
}
