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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.MethodParameter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.JsonSchemaVersion;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.SpecVersion.VersionFlag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;

/**
 * The argument resolver that supports JSON Schema validation.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class ValidateJsonSchemaArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * The factories
     */
    private final ConcurrentHashMap<ValidateJsonSchemaVersion, JsonSchemaFactory> factories;

    /**
     * The schema validatiors config used for all schemas
     */
    private final SchemaValidatorsConfig schemaValidatorsConfig;

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
        this.factories = new ConcurrentHashMap<>();
        this.objectMapper = config.getObjectMapper();
        this.schemaValidatorsConfig = config.getSchemaValidatorsConfig();
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

        // Get the factory for the version, only one factory per version as the caching
        // is utilized
        // and in theory there could be multiple versions used concurrently
        JsonSchemaFactory factory = this.factories.computeIfAbsent(jsonSchemaVersion,
                v -> createFactory(jsonSchemaVersion));

        // Load the schema
        JsonSchema jsonSchema;
        try {
            jsonSchema = factory.getSchema(SchemaLocation.of(schemaPath), this.schemaValidatorsConfig);
        } catch (Exception e) {
            throw new LoadJsonSchemaException("JSON Schema failed to load from path: " + schemaPath,
                    e);
        }

        // Get the JSON as a String
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        String jsonString = StreamUtils.copyToString(httpServletRequest.getInputStream(),
                StandardCharsets.UTF_8);

        // Parse into a JsonNode, needed for validation
        JsonNode json = objectMapper.readTree(jsonString);

        // Validate the Json
        Set<ValidationMessage> validationResult = jsonSchema.validate(json);
        if (validationResult.isEmpty()) {
            // Convert the JSON into the object
            return objectMapper.treeToValue(json, parameter.getParameterType());
        } else {
            // Throw the validation exception
            throw new ValidateJsonSchemaException(validationResult);
        }
    }

    private final JsonSchemaFactory createFactory(
            ValidateJsonSchemaVersion validateJsonSchemaVersion) {
        JsonSchemaFactory.getInstance(VersionFlag.V7);
        JsonSchemaFactory.Builder builder = JsonSchemaFactory.builder();

        VersionFlag versionFlag = validateJsonSchemaVersion.getSpecVersion();
        JsonSchemaVersion jsonSchemaVersion = JsonSchemaFactory.checkVersion(versionFlag);
        JsonMetaSchema metaSchema = jsonSchemaVersion.getInstance();
        builder.jsonMapper(this.objectMapper);
        builder.metaSchema(metaSchema);
        builder.defaultMetaSchemaIri(metaSchema.getIri());
        config.customizeJsonSchemaFactoryBuilder(builder, validateJsonSchemaVersion);
        return builder.build();
    }
}
