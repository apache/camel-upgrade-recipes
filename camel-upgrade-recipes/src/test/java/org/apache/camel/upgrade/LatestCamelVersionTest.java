/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.upgrade;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test to ensure the camel-latest-version property in pom.xml is kept aligned
 * with the highest version recipe file.
 *
 * This test prevents forgetting to update the camel-latest-version property
 * when a new version recipe is added.
 */
class LatestCamelVersionTest {

    @Test
    void camelLatestVersionPropertyMatchesHighestRecipe() throws Exception {
        // Find the highest version from recipe YAML files
        Path recipesPath = Paths.get("src/main/resources/META-INF/rewrite");
        RecipeVersion highestVersion = RecipeVersion.getHighestVersionFromRecipeDirectory(recipesPath, "recipe directory");

        // Read camel-latest-version from versions.properties
        String camelLatestVersion = CamelTestUtil.getCamelLatestVersion();

        if (camelLatestVersion == null || camelLatestVersion.trim().isEmpty()) {
            Assertions.fail("camel.latest.version not found in versions.properties. " +
                    "Ensure the project has been built (mvn process-resources).");
        }

        // Parse the version from the property (e.g., "4.20.0")
        RecipeVersion propertyVersion = RecipeVersion.parseFromFullVersion(camelLatestVersion);

        // Compare major.minor versions
        Assertions.assertEquals(highestVersion, propertyVersion,
                String.format("camel-latest-version property (%s) does not match the highest recipe version (%s). " +
                                "Please update the <camel-latest-version> property in pom.xml to %s.0",
                        camelLatestVersion, highestVersion.toDottedString(), highestVersion.toDottedString()));
    }
}
