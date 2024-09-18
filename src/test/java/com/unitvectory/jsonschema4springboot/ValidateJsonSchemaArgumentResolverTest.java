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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestBody;
import com.networknt.schema.ValidationMessage;

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
        ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver.newInstance();

        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.hasParameterAnnotation(ValidateJsonSchema.class)).thenReturn(true);

        assertTrue(resolver.supportsParameter(parameter));
    }

    @Test
    public void supportsParamFalseTest() {
        ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver.newInstance();

        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.hasParameterAnnotation(RequestBody.class)).thenReturn(false);

        assertFalse(resolver.supportsParameter(parameter));
    }

    @Test
    public void validJsonTest() throws Exception {

        ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver
                .newInstance(new ValidateJsonSchemaConfigDefault());

        ExampleValue example = (ExampleValue) ArgumentResolverMockHelper.resolveArgument(resolver,
                ExampleValue.class, "{\"value\":\"123\"}",
                ValidateJsonSchemaVersion.V7,
                "classpath:schema/simpleschemaV7.json");

        assertEquals("123", example.getValue());

        // Now cached
        example = (ExampleValue) ArgumentResolverMockHelper.resolveArgument(resolver,
                ExampleValue.class, "{\"value\":\"123\"}",
                ValidateJsonSchemaVersion.V7,
                "classpath:schema/simpleschemaV7.json");

        assertEquals("123", example.getValue());
    }

    @Test
    public void malformedJsonTest() throws Exception {

        ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver.newInstance();

        ValidateJsonSchemaException thrown = assertThrows(ValidateJsonSchemaException.class,
                () -> ArgumentResolverMockHelper.resolveArgument(resolver,
                        ExampleValue.class, "{\"foo\"}",
                        ValidateJsonSchemaVersion.V7,
                        "classpath:schema/simpleschemaV7.json"),
                "Expected ValidateJsonSchemaException exception");

        assertEquals("JSON payload invalid an could not be parsed", thrown.getMessage());

        assertEquals(1, thrown.getValidationResult().size());

        ValidationMessage validationMessage = thrown.getValidationResult().iterator().next();
        assertEquals("Unexpected character ('}' (code 125)): was expecting a colon to separate field name and value",
                validationMessage.getMessage());
    }

    @Test
    public void malformed2JsonTest() throws Exception {

        ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver.newInstance();

        ValidateJsonSchemaException thrown = assertThrows(ValidateJsonSchemaException.class,
                () -> ArgumentResolverMockHelper.resolveArgument(resolver,
                        ExampleValue.class, "{\"foo\":\"b}",
                        ValidateJsonSchemaVersion.V7,
                        "classpath:schema/simpleschemaV7.json"),
                "Expected ValidateJsonSchemaException exception");

        assertEquals("JSON payload invalid an could not be parsed", thrown.getMessage());

        assertEquals(1, thrown.getValidationResult().size());

        ValidationMessage validationMessage = thrown.getValidationResult().iterator().next();
        assertEquals("Unexpected end-of-input: was expecting closing quote for a string value",
                validationMessage.getMessage());
    }

    @Test
    public void invalidJsonTest() throws Exception {

        ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver.newInstance();

        ValidateJsonSchemaException thrown = assertThrows(ValidateJsonSchemaException.class,
                () -> ArgumentResolverMockHelper.resolveArgument(resolver,
                        ExampleValue.class, "{}",
                        ValidateJsonSchemaVersion.V7,
                        "classpath:schema/simpleschemaV7.json"),
                "Expected ValidateJsonSchemaException exception");

        assertEquals("JSON did not validate against JSON Schema", thrown.getMessage());

        assertEquals(1, thrown.getValidationResult().size());

        ValidationMessage validationMessage = thrown.getValidationResult().iterator().next();
        assertEquals("$: required property 'value' not found",
                validationMessage.getMessage());
    }

    @Test
    public void jsonSchemaVersionNullTest() {
        ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver.newInstance();

        LoadJsonSchemaException thrown = assertThrows(LoadJsonSchemaException.class,
                () -> ArgumentResolverMockHelper.resolveArgument(resolver,
                        ExampleValue.class, "{}", null,
                        "classpath:schema/simpleschemaV7.json"),
                "Expected LoadJsonSchemaException exception");

        assertEquals("version is null in @ValidateJsonSchema annotation",
                thrown.getMessage());
    }

    @Test
    public void jsonSchemaPathNullTest() {
        ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver.newInstance();

        LoadJsonSchemaException thrown = assertThrows(LoadJsonSchemaException.class,
                () -> ArgumentResolverMockHelper.resolveArgument(resolver,
                        ExampleValue.class, "{}",
                        ValidateJsonSchemaVersion.V7, null),
                "Expected LoadJsonSchemaException exception");

        assertEquals("schemaPath is null in @ValidateJsonSchema annotation",
                thrown.getMessage());
    }

    @Test
    public void missingSchemaTest() {
        ValidateJsonSchemaArgumentResolver resolver = ValidateJsonSchemaArgumentResolver.newInstance();

        LoadJsonSchemaException thrown = assertThrows(LoadJsonSchemaException.class,
                () -> ArgumentResolverMockHelper.resolveArgument(resolver,
                        ExampleValue.class, "{}",
                        ValidateJsonSchemaVersion.V7,
                        "classpath:doesnotexist"),
                "Expected LoadJsonSchemaException exception");

        assertEquals("JSON Schema failed to load from path: classpath:doesnotexist",
                thrown.getMessage());

    }
}
