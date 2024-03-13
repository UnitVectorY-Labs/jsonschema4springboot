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

import com.networknt.schema.SpecVersion;
import com.networknt.schema.SpecVersion.VersionFlag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The JSON Schema version
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@AllArgsConstructor
public enum JsonSchemaVersion {

    /**
     * Version 4.
     */
    V4(VersionFlag.V4),

    /**
     * Version 6
     */
    V6(VersionFlag.V6),

    /**
     * Version 7
     */
    V7(VersionFlag.V7),

    /**
     * Version 2019-09
     */
    V201909(VersionFlag.V201909),

    /**
     * Version 2020-12
     */
    V202012(VersionFlag.V202012);

    @Getter(AccessLevel.PACKAGE)
    private final SpecVersion.VersionFlag specVersion;
}
