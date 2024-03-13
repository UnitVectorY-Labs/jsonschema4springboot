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
import lombok.Builder;
import lombok.Value;

/**
 * The validate JSON Schema configuration.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@Value
@Builder
public class ValidateJsonSchemaConfig {

    @Builder.Default
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Builder.Default
    private final JsonSchemaCache cache = JsonSchemaCacheDefault.newInstance();

    @Builder.Default
    private final JsonSchemaLookup lookup = JsonSchemaLookupDefault.newInstance();
}
