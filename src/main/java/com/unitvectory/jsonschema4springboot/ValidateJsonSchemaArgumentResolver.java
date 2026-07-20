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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.MethodParameter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.serialization.DefaultNodeReader;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;

/**
 * The argument resolver that supports JSON Schema validation.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class ValidateJsonSchemaArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * The schema registries, one per schema version
     */
    private final ConcurrentHashMap<ValidateJsonSchemaVersion, SchemaRegistry> registries;

    /**
     * The schema registry config used for all schemas
     */
    private final SchemaRegistryConfig schemaRegistryConfig;

    /**
     * The Jackson ObjectMapper
     */
    private final ObjectMapper objectMapper;

    /**
     * The configuration
     */
    private final ValidateJsonSchemaConfig config;

    /**
     * Creates a new instance of the ValidateJsonSchemaArgumentResolver class
     * 
     * @param config the config
     */
    private ValidateJsonSchemaArgumentResolver(ValidateJsonSchemaConfig config) {
        this.registries = new ConcurrentHashMap<>();
        this.objectMapper = config.getObjectMapper();
        this.schemaRegistryConfig = config.getSchemaRegistryConfig();
        this.config = config;
    }

    /**
     * Creates a new instance of the ValidateJsonSchemaArgumentResolver class
     * 
     * @return the ValidateJsonSchemaArgumentResolver
     */
    public static ValidateJsonSchemaArgumentResolver newInstance() {
        return new ValidateJsonSchemaArgumentResolver(new ValidateJsonSchemaConfigDefault());
    }

    /**
     * Creates a new instance of the ValidateJsonSchemaArgumentResolver class
     * 
     * @param config the config
     * @return the ValidateJsonSchemaArgumentResolver
     */
    public static ValidateJsonSchemaArgumentResolver newInstance(
            @NonNull ValidateJsonSchemaConfig config) {
        return new ValidateJsonSchemaArgumentResolver(config);
    }

    @SuppressWarnings("null")
    @Override
    public final boolean supportsParameter(MethodParameter parameter) {
        // Only applies to ValidateJsonSchema annotation
        return parameter.hasParameterAnnotation(ValidateJsonSchema.class);
    }

    @SuppressWarnings("null")
    @Override
    public final Object resolveArgument(MethodParameter parameter,
            ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {

        // Get the annotation
        ValidateJsonSchema validateJsonSchema = parameter.getParameterAnnotation(ValidateJsonSchema.class);

        String schemaPath = validateJsonSchema.schemaPath();
        if (schemaPath == null) {
            throw new LoadJsonSchemaException(
                    "schemaPath is null in @ValidateJsonSchema annotation");
        }

        ValidateJsonSchemaVersion jsonSchemaVersion = validateJsonSchema.version();
        if (jsonSchemaVersion == null) {
            throw new LoadJsonSchemaException("version is null in @ValidateJsonSchema annotation");
        }

        // Get the registry for the version, only one registry per version as the
        // caching is utilized and in theory there could be multiple versions used
        // concurrently
        SchemaRegistry registry = this.registries.computeIfAbsent(jsonSchemaVersion,
                v -> createRegistry(jsonSchemaVersion));

        // Load the schema
        Schema schema;
        try {
            schema = registry.getSchema(SchemaLocation.of(schemaPath));
        } catch (Exception e) {
            throw new LoadJsonSchemaException("JSON Schema failed to load from path: " + schemaPath,
                    e);
        }

        // Get the JSON as a String
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        String jsonString = StreamUtils.copyToString(httpServletRequest.getInputStream(),
                StandardCharsets.UTF_8);

        // Parse into a JsonNode, needed for validation
        JsonNode json;
        try {
            json = objectMapper.readTree(jsonString);
        } catch (StreamReadException ex) {
            throw new ValidateJsonSchemaException(ex);
        }

        // Validate the Json
        List<Error> validationResult = schema.validate(json);
        if (validationResult.isEmpty()) {
            // Convert the JSON into the object
            return objectMapper.treeToValue(json, parameter.getParameterType());
        } else {
            // Throw the validation exception
            throw new ValidateJsonSchemaException(validationResult);
        }
    }

    private SchemaRegistry createRegistry(ValidateJsonSchemaVersion validateJsonSchemaVersion) {
        SpecificationVersion specVersion = validateJsonSchemaVersion.getSpecVersion();
        SchemaRegistry.Builder builder = SchemaRegistry.builder();
        builder.defaultDialectId(specVersion.getDialectId());
        builder.schemaRegistryConfig(this.schemaRegistryConfig);
        builder.nodeReader(DefaultNodeReader.builder().jsonMapper(this.objectMapper).build());
        config.customizeSchemaRegistryBuilder(builder, validateJsonSchemaVersion);
        return builder.build();
    }
}

