[![License](https://img.shields.io/badge/License-EPL%202.0-blue.svg)](https://www.eclipse.org/legal/epl-v20.html) [![Active](https://img.shields.io/badge/Status-Active-green)](https://unitvectory-labs.github.io/uvy-labs-guide/bestpractices/status.html#active) [![Maven Central](https://img.shields.io/maven-central/v/com.unitvectory/jsonschema4springboot)](https://central.sonatype.com/artifact/com.unitvectory/jsonschema4springboot) [![javadoc](https://javadoc.io/badge2/com.unitvectory/jsonschema4springboot/javadoc.svg)](https://javadoc.io/doc/com.unitvectory/jsonschema4springboot) [![codecov](https://codecov.io/gh/UnitVectorY-Labs/jsonschema4springboot/graph/badge.svg?token=UJ2HAD30E7)](https://codecov.io/gh/UnitVectorY-Labs/jsonschema4springboot)

# jsonschema4springboot

Add JSON Schema Validation to Spring Boot 3 with Annotations

## Purpose

This library provides an annotation for deserializing JSON in Spring Boot 3 to include JSON Schema validation as part of the process using the [networknt/json-schema-validator](https://github.com/networknt/json-schema-validator) library. This replaces the `@RequestBody` annotation with the `@ValidateJsonSchema` annotation that allows the JSON Schema to be specified for a specific payload when parsing it into an object.

This is an opinionated design first approach to implementing the APIs where the input validation logic lives in the JSON Schema instead of in the Java code. While the JSON Schema and POJO are not validated against one another, the flexible input validation provided by a JSON Schema is intended to reduce the complexity of the API implementation.

## Getting Started

This library requires Java 17 and Spring Boot 3 and is available in the Maven Central Repository:

```xml
<dependency>
    <groupId>com.unitvectory</groupId>
    <artifactId>jsonschema4springboot</artifactId>
    <version>0.0.2</version>
</dependency>
```

## Usage

For a Spring Boot 3 project to utilize the `@ValidateJsonSchema` annotation the `ValidateJsonSchemaArgumentResolver` class must be registered with Spring Boot using the `addArgumentResolvers` method. This can be accomplished with the following code.

```java
package example;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.unitvectory.jsonschema4springboot.ValidateJsonSchemaArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

@Configuration
public class JsonValidationConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(
            @SuppressWarnings("null") List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(ValidateJsonSchemaArgumentResolver.newInstance());
    }
}
```

Each request can have a schema defined, in this example the schema is located at `src/main/resources/jsonschema.json`. While this example shows a draft-07 JSON Schema example, all versions of JSON Schema supported by [networknt/json-schema-validator](https://github.com/networknt/json-schema-validator) can be used.

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
import com.unitvectory.jsonschema4springboot.ValidateJsonSchemaVersion;
import com.unitvectory.jsonschema4springboot.ValidateJsonSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
public class ExampleController {

    @PostMapping(path = "/example", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ExampleResponse example(@ValidateJsonSchema(version = ValidateJsonSchemaVersion.V7,
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

In the case the JSON Schema does not validate a `ValidateJsonSchemaException` will be thrown which can be mapped to an API response. The `ValidateJsonSchemaFailedResponse` class is provided and provides a simple error object listing the JSON Schema validation errors.

```json
{
  "message": "JSON validation failed",
  "details": ["$: required property 'value' not found"]
}
```

To return this error an exception mapper must be provided. However, this is flexible and a different error structure can be used specific to an implementation.

```java
package example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.unitvectory.jsonschema4springboot.ValidateJsonSchemaException;
import com.unitvectory.jsonschema4springboot.ValidateJsonSchemaFailedResponse;

@ControllerAdvice
public class JsonValidationExceptionHandler {

    @ExceptionHandler(ValidateJsonSchemaException.class)
    public ResponseEntity<ValidateJsonSchemaFailedResponse> onValidateJsonSchemaException(
            ValidateJsonSchemaException ex) {
        return ResponseEntity.badRequest().body(new ValidateJsonSchemaFailedResponse(ex));
    }
}
```
