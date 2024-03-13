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

import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * JSON Schema lookup interface.
 * 
 * This loads schemas from the class path by default.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class JsonSchemaLookupDefault implements JsonSchemaLookup {

    @Override
    public JsonSchema getSchema(VersionFlag version, String path)
            throws ValidateJsonSchemaException {
        // Get the factory for the specified version
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(version);

        // Load the resource from the class path
        Resource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new ValidateJsonSchemaException("JSON Schema not found at path: " + path);
        }

        // Load the schema
        try (InputStream schemaStream = resource.getInputStream()) {
            return schemaFactory.getSchema(schemaStream);
        } catch (Exception e) {
            throw new ValidateJsonSchemaException("JSON Schema failed to load from path: " + path,
                    e);
        }
    }
}
