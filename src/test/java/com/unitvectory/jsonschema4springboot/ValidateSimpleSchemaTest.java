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

import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.params.ParameterizedTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unitvectory.jsonparamunit.JsonNodeParamUnit;
import com.unitvectory.fileparamunit.ListFileSource;

/**
 * The ValidateSimpleSchemaTest test cases.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class ValidateSimpleSchemaTest extends JsonNodeParamUnit {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @ListFileSource(resources = "/examples/", fileExtension = ".json", recurse = false)
    public void testIt(String file) {
        run(file);
    }

    @Override
    protected JsonNode process(JsonNode input, String context) {

        ValidateJsonSchemaArgumentResolver resolver =
                ValidateJsonSchemaArgumentResolver.newInstance();

        try {
            String json = objectMapper.writeValueAsString(input);
            ValidateJsonSchemaVersion version = getJsonSchemaVersion(context);
            Object obj = ArgumentResolverMockHelper.resolveArgument(resolver, ExampleValue.class,
                    json, version, "classpath:schema/" + context);

            // In the case of success return the input
            return objectMapper.valueToTree(obj);
        } catch (ValidateJsonSchemaException e) {
            // Expecting the JSON from the error, expected output is the error
            ValidateJsonSchemaFailedResponse response = new ValidateJsonSchemaFailedResponse(e);
            return objectMapper.valueToTree(response);
        } catch (Exception e) {
            fail(e);
        }

        return null;
    }

    private ValidateJsonSchemaVersion getJsonSchemaVersion(String context) {
        for (ValidateJsonSchemaVersion version : ValidateJsonSchemaVersion.values()) {
            if (context.endsWith(version.name() + ".json")) {
                return version;
            }
        }

        return null;
    }
}
