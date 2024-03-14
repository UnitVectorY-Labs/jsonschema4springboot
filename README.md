[![License](https://img.shields.io/badge/License-EPL%202.0-blue.svg)](https://www.eclipse.org/legal/epl-v20.html) [![codecov](https://codecov.io/gh/UnitVectorY-Labs/jsonschema4springboot/graph/badge.svg?token=UJ2HAD30E7)](https://codecov.io/gh/UnitVectorY-Labs/jsonschema4springboot)

# jsonschema4springboot

Add JSON Schema Validation to Spring Boot 3 with Annotations

## Purpose

This library provides an annotation for deserializing JSON in Spring Boot 3 to include JSON Schema validation as part of the process using the [networknt/json-schema-validator](https://github.com/networknt/json-schema-validator) library. This replaces the `@RequestBody` annotation with the `@ValidateJsonSchema` annotation that allows the JSON Schema to be specified for a specific payload.

This is an opinionated design first approach to implementing the APIs where the input validation logic lives in the JSON Schema instead of in the Java code. While the JSON Schema and POJO are not validated against one another, the flexible input validation

## Getting Started

This library requires Java 17 and Spring Boot 3.

It is still under active development.

## Usage

For a Spring Boot 3 project to utilize the annotation the `ValidateJsonSchemaArgumentResolver` argument resolver must be registered. This can be accomplished with the following code.

```java
package example;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unitvectory.jsonschema4springboot.JsonSchemaLookupDefault;
import com.unitvectory.jsonschema4springboot.ValidateJsonSchemaArgumentResolver;
import com.unitvectory.jsonschema4springboot.ValidateJsonSchemaConfig;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

@Configuration
public class JsonValidationConfiguration implements WebMvcConfigurer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Override
    public void addArgumentResolvers(
            @SuppressWarnings("null") List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(ValidateJsonSchemaArgumentResolver.newInstance(ValidateJsonSchemaConfig
                .builder().objectMapper(objectMapper)
                .lookup(JsonSchemaLookupDefault.newInstance(resourcePatternResolver)).build()));
    }
}
```

Each request can have a schema defined, in this example the schema is located at `src/main/resources/jsonschema.json` but multiple. While this example shows a draft-07 JSON Schema example, all versions of JSON Schema supported by [networknt/json-schema-validator](https://github.com/networknt/json-schema-validator) can be used.

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "value": {
      "type": "string",
      "pattern": "^[a-zA-Z0-9]+$"
    }
  },
  "required": ["value"],
  "additionalProperties": false
}
```

The following example shows a controller that utilizies the prior JSON Schema with a nominal API implementation.

```java
package example;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.unitvectory.jsonschema4springboot.JsonSchemaVersion;
import com.unitvectory.jsonschema4springboot.ValidateJsonSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
public class ExampleController {

    @PostMapping(path = "/example", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ExampleResponse example(@ValidateJsonSchema(version = JsonSchemaVersion.V7,
            schemaPath = "classpath:jsonschema.json") ExampleRequest request) {
        // Trivial API example
        return new ExampleResponse(request.getValue().length());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExampleRequest {
        private String value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExampleResponse {
        private int length;
    }
}
```

In the case the JSON Schema does not validate a `ValidateJsonSchemaException` will be thrown which can be mapped to an API response.
