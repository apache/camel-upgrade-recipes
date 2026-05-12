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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Semantic version holder for comparing X.Y version strings used in recipe files.
 * This is a test utility class shared across recipe verification tests.
 */
public class RecipeVersion implements Comparable<RecipeVersion> {
    public final int major;
    public final int minor;

    public RecipeVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    /**
     * Parse a version string in X.Y format (e.g., "4.20").
     *
     * @param version version string
     * @return parsed RecipeVersion object
     * @throws IllegalArgumentException if format is invalid
     */
    public static RecipeVersion parse(String version) {
        String[] parts = version.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Version must be in format X.Y, got: " + version);
        }
        try {
            return new RecipeVersion(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version format: " + version, e);
        }
    }

    /**
     * Parse a version string in X.Y.Z or X.Y format (e.g., "4.20.0" or "4.20") and return X.Y.
     *
     * @param fullVersion version string in X.Y.Z or X.Y format
     * @return parsed RecipeVersion object with major.minor
     * @throws IllegalArgumentException if format is invalid
     */
    public static RecipeVersion parseFromFullVersion(String fullVersion) {
        String[] parts = fullVersion.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Version must be in format X.Y.Z or X.Y, got: " + fullVersion);
        }
        try {
            return new RecipeVersion(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version format: " + fullVersion, e);
        }
    }

    @Override
    public int compareTo(RecipeVersion other) {
        int majorCompare = Integer.compare(this.major, other.major);
        if (majorCompare != 0) {
            return majorCompare;
        }
        return Integer.compare(this.minor, other.minor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RecipeVersion version = (RecipeVersion) obj;
        return major == version.major && minor == version.minor;
    }

    @Override
    public int hashCode() {
        return 31 * major + minor;
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
    public String toDottedString() {
        return String.format("%d.%d", major, minor);
    }

    /**
     * Finds the highest version from recipe YAML files in the specified directory.
     * Scans the directory for files matching the pattern X.Y.yaml (e.g., 4.20.yaml)
     * and returns the highest version found.
     *
     * @param recipesPath the directory to scan for recipe files
     * @param pathDescription description of the path for error messages (e.g., "core recipes", "Spring Boot recipes")
     * @return the highest RecipeVersion found
     * @throws Exception if an I/O error occurs during directory traversal
     * @throws IllegalStateException if no version files are found in the directory
     */
    public static RecipeVersion getHighestVersionFromRecipeDirectory(Path recipesPath, String pathDescription) throws Exception {
        if (!Files.exists(recipesPath)) {
            throw new IllegalStateException(
                String.format("Recipe directory not found for %s: %s",
                    pathDescription, recipesPath.toAbsolutePath()));
        }

        try (Stream<Path> paths = Files.walk(recipesPath)) {
            return paths.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".yaml") && Character.isDigit(name.charAt(0)))
                    .map(name -> name.substring(0, name.lastIndexOf('.')))
                    .filter(version -> version.matches("^\\d+\\.\\d+$"))
                    .map(RecipeVersion::parse)
                    .max(RecipeVersion::compareTo)
                    .orElseThrow(() -> new IllegalStateException(
                        String.format("No version recipe files found (pattern: X.Y.yaml) in %s. Searched in: %s",
                            pathDescription, recipesPath.toAbsolutePath())));
        }
    }
}
