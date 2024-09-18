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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.networknt.schema.ValidationMessage;

/**
 * The ValidateJsonSchemaException test cases.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class ValidateJsonSchemaExceptionTest {

    @Test
    public void nullValidationResultsTest() {
        Set<ValidationMessage> validationResult = null;
        NullPointerException thrown = assertThrows(NullPointerException.class,
                () -> new ValidateJsonSchemaException(validationResult));

        assertEquals("validationResult is marked non-null but is null", thrown.getMessage());
    }

    @Test
    public void nullJsonParseExceptionTest() {
        JsonParseException jsonParseException = null;
        NullPointerException thrown = assertThrows(NullPointerException.class,
                () -> new ValidateJsonSchemaException(jsonParseException));

        assertEquals("jsonParseException is marked non-null but is null", thrown.getMessage());
    }
}
