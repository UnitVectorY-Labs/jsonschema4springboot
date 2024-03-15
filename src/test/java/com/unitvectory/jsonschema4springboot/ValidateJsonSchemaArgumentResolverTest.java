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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.SpecVersion.VersionFlag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The ValidateJsonSchemaArgumentResolver test cases.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class ValidateJsonSchemaArgumentResolverTest {

        @Test
        public void missingConfigTest() {
                NullPointerException thrown = assertThrows(NullPointerException.class,
                                () -> ValidateJsonSchemaArgumentResolver.newInstance(null),
                                "ValidateJsonSchemaArgumentResolver config cannot be null");

                assertEquals("config is marked non-null but is null", thrown.getMessage());
        }

        @Test
        public void supportsParamTrueTest() {
                ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver
                                .newInstance(ValidateJsonSchemaConfig.builder().build());

                MethodParameter parameter = mock(MethodParameter.class);
                when(parameter.hasParameterAnnotation(ValidateJsonSchema.class)).thenReturn(true);

                assertTrue(resolver.supportsParameter(parameter));
        }

        @Test
        public void supportsParamFalseTest() {
                ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver
                                .newInstance(ValidateJsonSchemaConfig.builder().build());

                MethodParameter parameter = mock(MethodParameter.class);
                when(parameter.hasParameterAnnotation(RequestBody.class)).thenReturn(false);

                assertFalse(resolver.supportsParameter(parameter));
        }

        @Test
        public void validJsonTest() throws Exception {

                ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver
                                .newInstance(ValidateJsonSchemaConfig.builder().build());

                ExampleValue example = (ExampleValue) resolveArgument(resolver, ExampleValue.class,
                                "{\"value\":\"123\"}", JsonSchemaVersion.V7, "simpleschemaV7.json");

                assertEquals("123", example.getValue());

                // Now cached
                example = (ExampleValue) resolveArgument(resolver, ExampleValue.class,
                                "{\"value\":\"123\"}", JsonSchemaVersion.V7, "simpleschemaV7.json");

                assertEquals("123", example.getValue());
        }

        @Test
        public void failedLoadSchemaTest() throws Exception {
                ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver
                                .newInstance(ValidateJsonSchemaConfig.builder()
                                                .lookup(new JsonSchemaLookup() {

                                                        @Override
                                                        public JsonSchema getSchema(
                                                                        VersionFlag version,
                                                                        String path) {
                                                                // Intentionally returning null
                                                                return null;
                                                        }
                                                }).build());

                LoadJsonSchemaException thrown = assertThrows(LoadJsonSchemaException.class,
                                () -> resolveArgument(resolver, ExampleValue.class, "{}",
                                                JsonSchemaVersion.V7, "simpleschemaV7.json"),
                                "Expected exception if JSON Schema cannot be loaded.");

                assertEquals("Failed to load JSON Schema", thrown.getMessage());
        }


        @Test
        public void invalidJsonTest() throws Exception {

                ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver
                                .newInstance(ValidateJsonSchemaConfig.builder().build());

                ValidateJsonSchemaException thrown = assertThrows(ValidateJsonSchemaException.class,
                                () -> resolveArgument(resolver, ExampleValue.class, "{}",
                                                JsonSchemaVersion.V7, "simpleschemaV7.json"),
                                "Invalid JSON expected ValidateJsonSchemaException exception");

                assertEquals("JSON did not validate against JSON Schema", thrown.getMessage());

                assertEquals(1, thrown.getValidationResult().size());

                ValidationMessage validationMessage =
                                thrown.getValidationResult().iterator().next();
                assertEquals("$: required property 'value' not found",
                                validationMessage.getMessage());
        }

        private Object resolveArgument(ValidateJsonSchemaArgumentResolver resolver,
                        @SuppressWarnings("rawtypes") Class parameterType, String json,
                        JsonSchemaVersion schemaVersion, String schemaPath) throws Exception {

                // These are not used so they are not mocked
                ModelAndViewContainer mavContainer = null;
                WebDataBinderFactory binderFactory = null;

                // Mock everything so this can be tested outside of the typical Spring Boot
                // implementation

                ValidateJsonSchema validateJsonSchema = mock(ValidateJsonSchema.class);
                when(validateJsonSchema.version()).thenReturn(schemaVersion);
                when(validateJsonSchema.schemaPath()).thenReturn(schemaPath);

                MethodParameter parameter = mock(MethodParameter.class);
                when(parameter.getParameterAnnotation(ValidateJsonSchema.class))
                                .thenReturn(validateJsonSchema);
                doReturn(parameterType).when(parameter).getParameterType();

                try (MockServletInputStream mockServletInputStream =
                                new MockServletInputStream(json)) {
                        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
                        when(httpServletRequest.getInputStream())
                                        .thenReturn(mockServletInputStream);

                        NativeWebRequest webRequest = mock(NativeWebRequest.class);
                        when(webRequest.getNativeRequest(HttpServletRequest.class))
                                        .thenReturn(httpServletRequest);

                        // Resolve the argument or throw an exception if input was not validated
                        return resolver.resolveArgument(parameter, mavContainer, webRequest,
                                        binderFactory);
                }
        }
}
