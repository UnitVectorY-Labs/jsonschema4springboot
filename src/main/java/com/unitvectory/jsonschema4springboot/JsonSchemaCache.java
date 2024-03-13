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

import com.networknt.schema.JsonSchema;

/**
 * JSON Schema Cache interface.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public interface JsonSchemaCache {

    /**
     * Get a cached schema.
     * 
     * @param path the path
     * @return the JsonSchema; null if not found
     */
    JsonSchema getSchema(String path);

    /**
     * Cache a JsonSchema
     * 
     * @param path the path
     * @param schema the JsonSchema
     */
    void cacheSchema(String path, JsonSchema schema);
}
