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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The ArgumentResolverMockHelper cases.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class ArgumentResolverMockHelper {

    static Object resolveArgument(ValidateJsonSchemaArgumentResolver resolver,
            @SuppressWarnings("rawtypes") Class parameterType, String json,
            ValidateJsonSchemaVersion schemaVersion, String schemaPath) throws Exception {

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

        try (MockServletInputStream mockServletInputStream = new MockServletInputStream(json)) {
            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getInputStream()).thenReturn(mockServletInputStream);

            NativeWebRequest webRequest = mock(NativeWebRequest.class);
            when(webRequest.getNativeRequest(HttpServletRequest.class))
                    .thenReturn(httpServletRequest);

            // Resolve the argument or throw an exception if input was not validated
            return resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        }
    }
}
