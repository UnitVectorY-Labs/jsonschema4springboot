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
import org.springframework.core.io.ResourceLoader;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion.VersionFlag;
import lombok.NonNull;

/**
 * JSON Schema lookup interface.
 * 
 * This loads schemas from the class path by default.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class JsonSchemaLookupDefault implements JsonSchemaLookup {

    private final ResourceLoader resourceLoader;

    private final SchemaValidatorsConfig schemaValidatorsConfig;

    /**
     * Creates a new instance of the JsonSchemaLookupDefault class.
     */
    private JsonSchemaLookupDefault() {
        this(null, null);
    }

    /**
     * Creates a new instance of the JsonSchemaLookupDefault class.
     * 
     * @param resourceLoader the resource loader
     * @param schemaValidatorsConfig the schema validators config
     */
    private JsonSchemaLookupDefault(ResourceLoader resourceLoader,
            SchemaValidatorsConfig schemaValidatorsConfig) {
        this.resourceLoader = resourceLoader;
        this.schemaValidatorsConfig = schemaValidatorsConfig;
    }

    /**
     * Creates a new instance of the JsonSchemaLookupDefault class.
     * 
     * @return the instance
     */
    public static JsonSchemaLookup newInstance() {
        return new JsonSchemaLookupDefault();
    }

    /**
     * Creates a new instance of the JsonSchemaLookupDefault class.
     * 
     * @param resourceLoader the Spring Boot resource loader
     * @return the instance
     */
    public static JsonSchemaLookup newInstance(@NonNull ResourceLoader resourceLoader) {
        return new JsonSchemaLookupDefault(resourceLoader, null);
    }

    /**
     * Creates a new instance of the JsonSchemaLookupDefault class.
     * 
     * @param schemaValidatorsConfig the schema validators config
     * @return the instance
     */
    public static JsonSchemaLookup newInstance(
            @NonNull SchemaValidatorsConfig schemaValidatorsConfig) {
        return new JsonSchemaLookupDefault(null, schemaValidatorsConfig);
    }

    /**
     * Creates a new instance of the JsonSchemaLookupDefault class.
     * 
     * @param resourceLoader the Spring Boot resource loader
     * @param schemaValidatorsConfig the schema validators config
     * @return the instance
     */
    public static JsonSchemaLookup newInstance(@NonNull ResourceLoader resourceLoader,
            @NonNull SchemaValidatorsConfig schemaValidatorsConfig) {
        return new JsonSchemaLookupDefault(resourceLoader, schemaValidatorsConfig);
    }

    /**
     * Load in a resource from the specified loader, if no loader is specified a class path resource
     * will be loaded.
     * 
     * @param path the path
     * @return the resource
     */
    @SuppressWarnings("null")
    private Resource loadResource(String path) {
        if (resourceLoader != null) {
            return resourceLoader.getResource(path);
        } else {
            return new ClassPathResource(path);
        }
    }

    @Override
    public JsonSchema getSchema(VersionFlag version, String path) {
        // Get the factory for the specified version
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(version);

        // Load the resource from the class path
        Resource resource = this.loadResource(path);
        if (!resource.exists()) {
            throw new LoadJsonSchemaException("JSON Schema not found at path: " + path);
        }

        // Load the schema
        try (InputStream schemaStream = resource.getInputStream()) {
            if (this.schemaValidatorsConfig == null) {
                return schemaFactory.getSchema(schemaStream);
            } else {
                return schemaFactory.getSchema(schemaStream, this.schemaValidatorsConfig);
            }
        } catch (Exception e) {
            throw new LoadJsonSchemaException("JSON Schema failed to load from path: " + path, e);
        }
    }
}
