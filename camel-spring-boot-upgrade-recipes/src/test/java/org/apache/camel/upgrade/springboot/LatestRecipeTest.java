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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;


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
        List<Version> versions = getVersionsFromCoreRecipes();

        if (versions.isEmpty()) {
            Path coreCamelRecipes = Paths.get("../camel-upgrade-recipes/src/main/resources/META-INF/rewrite")
                    .toAbsolutePath();
            Assertions.fail(String.format("No core Camel version recipe files found (pattern: X.Y.yaml). " +
                    "Searched in: %s. Ensure multi-module structure is intact.", coreCamelRecipes));
        }

        Version latestVersion = versions.stream()
                .max(Version::compareTo)
                .orElseThrow(() -> new IllegalStateException("No maximum version found despite non-empty list"));

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

    /**
     * Get version objects from YAML files in the camel-upgrade-recipes dependency.
     * Returns only version files matching the pattern X.Y.yaml (e.g., 4.20.yaml).
     *
     * @return List of Version objects found in the dependency
     */
    private List<Version> getVersionsFromCoreRecipes() throws Exception {
        // Navigate to the sibling camel-upgrade-recipes module using relative path
        Path coreCamelRecipes = Paths.get("../camel-upgrade-recipes/src/main/resources/META-INF/rewrite");

        if (!Files.exists(coreCamelRecipes)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(coreCamelRecipes)) {
            return paths.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".yaml") && Character.isDigit(name.charAt(0)))
                    .map(name -> name.substring(0, name.lastIndexOf('.')))
                    .filter(version -> version.matches("^\\d+\\.\\d+$"))
                    .map(Version::parse)
                    .toList();
        }
    }

    /**
     * Semantic version holder for comparing X.Y version strings.
     */
    private static class Version implements Comparable<Version> {
        final int major;
        final int minor;

        Version(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }

        /**
         * Parse a version string in X.Y format.
         *
         * @param version version string (e.g., "4.20")
         * @return parsed Version object
         * @throws IllegalArgumentException if format is invalid
         */
        static Version parse(String version) {
            String[] parts = version.split("\\.");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Version must be in format X.Y, got: " + version);
            }
            try {
                return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid version format: " + version, e);
            }
        }

        @Override
        public int compareTo(Version other) {
            int majorCompare = Integer.compare(this.major, other.major);
            if (majorCompare != 0) {
                return majorCompare;
            }
            return Integer.compare(this.minor, other.minor);
        }

        /**
         * Returns version without dot (e.g., "420" for version 4.20).
         * Used for recipe package names.
         */
        @Override
        public String toString() {
            return String.format("%d%d", major, minor);
        }

        /**
         * Returns version with dot (e.g., "4.20").
         * Used for user-facing messages.
         */
        String toDottedString() {
            return String.format("%d.%d", major, minor);
        }
    }
}
