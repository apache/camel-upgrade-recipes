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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test to ensure no Moderne recipes are used in this project.
 * Moderne recipes are not available due to the terms of the Moderne license.
 */
class NoModerneRecipesTest {

    private static final String MODERNE_RECIPE_PREFIX = "io.moderne";
    private static final String MODERNE_LICENSE = "Moderne Proprietary License";
    private static final String REWRITE_PATH = "META-INF/rewrite";

    @Test
    void ensureNoModerneRecipesAreUsed() throws Exception {
        List<String> violations = new ArrayList<>();

        // Get all YAML files from META-INF/rewrite
        List<Path> yamlFiles = getYamlFilesFromResources();

        for (Path yamlFile : yamlFiles) {
            try (Stream<String> lines = Files.lines(yamlFile)) {
                String fileName = yamlFile.getFileName().toString();
                int lineNumber = 0;
                for (String line : lines.toList()) {
                    lineNumber++;
                    String trimmedLine = line.trim();

                    // Check if line starts with "- " (recipe list item) or contains recipe reference
                    if (trimmedLine.startsWith("- " + MODERNE_RECIPE_PREFIX) ||
                        (trimmedLine.contains(MODERNE_RECIPE_PREFIX) &&
                         (trimmedLine.contains("recipeList:") || trimmedLine.startsWith("- ")))) {
                        violations.add(String.format("%s:%d - Found Moderne recipe reference: %s",
                                                     fileName, lineNumber, trimmedLine));
                    }

                    // Check for Moderne Proprietary License in comments or headers
                    if (line.contains(MODERNE_LICENSE)) {
                        violations.add(String.format("%s:%d - Found Moderne Proprietary License: %s",
                                                     fileName, lineNumber, trimmedLine));
                    }
                }
            }
        }

        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("\n\nModerne recipes detected! These cannot be used due to Moderne license restrictions.\n");
            errorMessage.append("Moderne recipes starting with 'io.moderne' are not available for use in this project.\n\n");
            errorMessage.append("Violations found:\n");
            for (String violation : violations) {
                errorMessage.append("  - ").append(violation).append("\n");
            }
            fail(errorMessage.toString());
        }
    }

    private List<Path> getYamlFilesFromResources() throws IOException, URISyntaxException {
        List<Path> yamlFiles = new ArrayList<>();

        // Try to get from file system first (during development)
        Path resourcePath = Paths.get("src/main/resources", REWRITE_PATH);
        if (Files.exists(resourcePath)) {
            try (Stream<Path> paths = Files.walk(resourcePath)) {
                paths.filter(Files::isRegularFile)
                     .filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                     .forEach(yamlFiles::add);
            }
        }

        // Also check in classpath (when running from JAR)
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            URI uri = classLoader.getResource(REWRITE_PATH).toURI();
            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath("/" + REWRITE_PATH);
            } else {
                myPath = Paths.get(uri);
            }
            try (Stream<Path> paths = Files.walk(myPath)) {
                paths.filter(Files::isRegularFile)
                     .filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                     .forEach(yamlFiles::add);
            }
        } catch (Exception e) {
            // If classpath resource not found, that's okay - we already checked file system
        }

        return yamlFiles;
    }
}
