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

import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.networknt.schema.ValidationMessage;
import lombok.Getter;
import lombok.NonNull;

/**
 * Validate JSON Schema Exception
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class ValidateJsonSchemaException extends RuntimeException {

    /**
     * The validation messages when the JSON Schema does not validate.
     */
    @Getter
    private Set<ValidationMessage> validationResult;

    /**
     * Creates a new instance of the ValidateJsonSchemaException class
     * 
     * @param validationResult the validation messages
     */
    public ValidateJsonSchemaException(@NonNull Set<ValidationMessage> validationResult) {
        super("JSON did not validate against JSON Schema");
        this.validationResult = validationResult;
    }

    ValidateJsonSchemaException(@NonNull JsonParseException jsonParseException) {
        super("JSON payload invalid an could not be parsed", jsonParseException);
        String message = jsonParseException.getMessage();

        if(message != null){
            message = message.split("\n")[0];
        }

        this.validationResult = Set.of(ValidationMessage.builder().message(message).build());
    }
}
