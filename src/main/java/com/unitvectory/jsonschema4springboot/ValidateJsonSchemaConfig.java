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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaValidatorsConfig;

/**
 * The configuration for validating the JSON Schema.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public interface ValidateJsonSchemaConfig {

    /**
     * Get the object mapper
     * 
     * @return the object mapper
     */
    default ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    /**
     * Get the SchemaValidatorsConfig
     * 
     * @return the SchemaValidatorsConfig
     */
    default SchemaValidatorsConfig getSchemaValidatorsConfig() {
        return new SchemaValidatorsConfig.Builder()
                .pathType(PathType.LEGACY)
                .errorMessageKeyword("message")
                .nullableKeywordEnabled(true)
                .build();
    }

    /**
     * Customizes the JsonSchemaFactory builder
     * 
     * @param builder the builder
     * @param version the JSON Schema version
     */
    default void customizeJsonSchemaFactoryBuilder(JsonSchemaFactory.Builder builder,
            ValidateJsonSchemaVersion version) {
    }
}
