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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.networknt.schema.JsonSchema;

/**
 * The Default JSON Schema Cache implementation.
 * 
 * Caches an unbounded number of schemas perpetually.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
class JsonSchemaCacheDefault implements JsonSchemaCache {

    /**
     * The JSON Schema cache.
     */
    private Map<String, JsonSchema> cache;

    /**
     * Creates a new instance of the JsonSchemaCacheDefault class.
     */
    private JsonSchemaCacheDefault() {
        this.cache = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new instance of the JsonSchemaCacheDefault class.
     * 
     * @return the instance
     */
    public static JsonSchemaCache newInstance() {
        return new JsonSchemaCacheDefault();
    }

    @Override
    public JsonSchema getSchema(String path) {
        return this.cache.get(path);
    }

    @Override
    public void cacheSchema(String path, JsonSchema schema) {
        this.cache.put(path, schema);
    }
}
