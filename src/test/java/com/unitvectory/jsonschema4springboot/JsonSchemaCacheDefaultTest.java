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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * The JsonSchemaCacheDefault test cases.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class JsonSchemaCacheDefaultTest {

    @Test
    public void testCache() {
        String path = "simpleschemaV7.json";
        JsonSchema jsonSchema =
                JsonSchemaLookupDefault.newInstance().getSchema(VersionFlag.V7, path);
        assertNotNull(jsonSchema);
        JsonSchemaCache cache = JsonSchemaCacheDefault.newInstance();
        assertNull(cache.getSchema(path));
        cache.cacheSchema(path, jsonSchema);
        assertNotNull(cache.getSchema(path));
    }
}
