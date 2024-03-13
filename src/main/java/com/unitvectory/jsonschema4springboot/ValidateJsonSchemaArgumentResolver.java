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
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import lombok.AllArgsConstructor;

/**
 * The argument resolver that supports JSON Schema validation.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@AllArgsConstructor
public class ValidateJsonSchemaArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * The configuration
     */
    private final ValidateJsonSchemaConfig config;

    @Override
    public final boolean supportsParameter(MethodParameter parameter) {
        // Only applies to ValidateJsonSchema annotation
        return parameter.hasParameterAnnotation(ValidateJsonSchema.class);
    }

    @Override
    public final Object resolveArgument(MethodParameter parameter,
            ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        // Get the annotation
        String schemaPath = parameter.getParameterAnnotation(ValidateJsonSchema.class).schemaPath();

        // First try the cache
        JsonSchema schema = this.config.getCache().getSchema(schemaPath);
        if (schema == null) {
            schema = this.config.getLookup().getSchema(this.config.getSpecVersion(), schemaPath);

            // Failed to get the
            if (schema == null) {
                throw new ValidateJsonSchemaException("Failed to load JSON Schema");
            }

            // Cache the value
            this.config.getCache().cacheSchema(schemaPath, schema);
        }

        // Get the JSON as a String
        HttpServletRequest httpServletRequest =
                webRequest.getNativeRequest(HttpServletRequest.class);
        String jsonString = StreamUtils.copyToString(httpServletRequest.getInputStream(),
                StandardCharsets.UTF_8);

        // Parse into a JsonNode, needed for validation
        JsonNode json = this.config.getObjectMapper().readTree(jsonString);

        // Validate the Json
        Set<ValidationMessage> validationResult = schema.validate(json);
        if (validationResult.isEmpty()) {
            // Convert the JSON into the object
            return this.config.getObjectMapper().treeToValue(json, parameter.getParameterType());
        } else {
            // Throw the validation exception
            throw new ValidateJsonSchemaException(validationResult);
        }
    }
}
