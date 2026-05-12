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
package org.apache.camel.upgrade.springboot;

import org.apache.camel.upgrade.RecipeVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Test to ensure the latest.yaml recipe references the highest version recipe
 * from the camel-upgrade-recipes dependency.
 *
 * The test uses the camel-upgrade-recipes dependency as the source of truth,
 * finds the highest version there, and verifies that latest.yaml references
 * the Spring Boot migration recipe for that version (but NOT the core Camel
 * migration recipe, which is included transitively via the version-specific
 * recipe chain).
 *
 * This prevents forgetting to update latest.yaml when a new version is added
 * to the dependency.
 *
 * Note: This test requires running from the camel-spring-boot-upgrade-recipes
 * module directory with the full multi-module checkout structure intact.
 */
class LatestRecipeTest {

    @Test
    void latestRecipeReferencesHighestVersion() throws Exception {
        // Get versions from camel-upgrade-recipes dependency - this is the source of truth
        // Navigate to the sibling camel-upgrade-recipes module using relative path
        Path coreCamelRecipes = Paths.get("../camel-upgrade-recipes/src/main/resources/META-INF/rewrite");
        RecipeVersion latestVersion = RecipeVersion.getHighestVersionFromRecipeDirectory(coreCamelRecipes,
                "core Camel recipes (ensure multi-module structure is intact)");

        // Read latest.yaml directly
        Path latestYamlPath = Paths.get("src/main/resources/META-INF/rewrite/latest.yaml").toAbsolutePath();
        if (!Files.exists(latestYamlPath)) {
            Assertions.fail("latest.yaml not found at: " + latestYamlPath);
        }
        String latestYamlContent = Files.readString(latestYamlPath);

        // Build recipe names using version string without dot (e.g., "4.20" -> "420")
        String versionWithoutDot = String.format("%d%d", latestVersion.major, latestVersion.minor);

        // Expected Spring Boot recipe name: org.apache.camel.upgrade.camelXY.CamelSpringBootMigrationRecipe
        String latestSpringBootRecipeName = String.format(
                "org.apache.camel.upgrade.camel%s.CamelSpringBootMigrationRecipe",
                versionWithoutDot);

        // Verify Spring Boot recipe is present in recipeList section
        if (!latestYamlContent.contains("recipeList:")) {
            Assertions.fail("latest.yaml does not contain 'recipeList:' section");
        }
        String recipeListSection = latestYamlContent.substring(latestYamlContent.indexOf("recipeList:"));

        Assertions.assertTrue(recipeListSection.contains(latestSpringBootRecipeName),
                String.format("Expected Spring Boot recipe for version %s not found in recipeList: %s",
                        latestVersion.toDottedString(), latestSpringBootRecipeName));

        // Expected core Camel recipe name from dependency: org.apache.camel.upgrade.camelXY.CamelMigrationRecipe
        String latestCoreCamelRecipeName = String.format(
                "org.apache.camel.upgrade.camel%s.CamelMigrationRecipe",
                versionWithoutDot);

        // Verify core Camel recipe is NOT directly present (it's included transitively)
        Assertions.assertFalse(recipeListSection.contains(latestCoreCamelRecipeName),
                String.format("Core Camel recipe for version %s should NOT be directly present in latest.yaml " +
                                "(it's included transitively via Spring Boot recipe): %s",
                        latestVersion.toDottedString(), latestCoreCamelRecipeName));
    }
}
