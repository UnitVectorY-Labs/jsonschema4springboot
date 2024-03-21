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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.networknt.schema.ValidationMessage;
import lombok.Getter;

/**
 * The response object for when JSON Schema validation failed.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@Getter
public class ValidateJsonSchemaFailedResponse {

    /**
     * The message
     */
    private final String message;

    /**
     * The details
     */
    private final List<String> details;

    /**
     * Creates a new instance of the ValidateJsonSchemaFailedResponse class
     * 
     * @param ex the ValidateJsonSchemaException exception to render into a response
     */
    public ValidateJsonSchemaFailedResponse(ValidateJsonSchemaException ex) {
        this.message = "JSON validation failed";
        List<String> detailsList = new ArrayList<String>();
        for (ValidationMessage m : ex.getValidationResult()) {
            detailsList.add(m.getMessage());
        }

        this.details = Collections.unmodifiableList(detailsList);
    }
}
