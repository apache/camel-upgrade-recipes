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

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openrewrite.maven.Assertions.pomXml;

/**
 * Regression tests for the property-managed BOM version upgrade in the aggregate recipes.
 *
 * {@code UpgradeDependencyVersion} resolves the new version from {@code maven-metadata.xml}.
 * When that file is absent from the repository (e.g. staging or mirror repos that do not
 * publish metadata), the recipe silently no-ops on a property reference such as
 * {@code ${camel-spring-boot-version}}. The companion {@code ChangePropertyValue} entries
 * set the property value directly and do not require metadata resolution.
 * <p>
 * The rewrite tests run the LTS recipe because its target is a fixed released version, unlike
 * the latest recipe whose target follows {@code project.version}: an unreleased SNAPSHOT cannot be
 * resolved, which leaves Maven-parser warning markers in the output that depend on the environment
 * (and on the number of recipes that touch the POM afterwards), and the expectation would break
 * at release time. {@link #aggregatesChangeTheBomProperties()} covers {@code latest.yaml} itself.
 */
class CamelSpringBootBomVersionTest implements RewriteTest {

    private static final Path REWRITE_DIR = Paths.get("src/main/resources/META-INF/rewrite");

    private static final String[] BOM_PROPERTIES = {
            "camel-spring-boot-version", "camel.springboot.version", "camel-spring-boot.version"};

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("org.apache.camel.upgrade.CamelSpringBoot418LTSMigrationRecipe");
    }

    /**
     * BOM imported via {@code ${camel-spring-boot-version}} property, the shape used by
     * Apache camel-spring-boot-examples.
     */
    @DocumentExample
    @Test
    void propertyManagedBomIsUpdated() {
        //language=xml
        rewriteRun(
                pomXml(
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <camel-spring-boot-version>4.10.0</camel-spring-boot-version>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>org.apache.camel.springboot</groupId>
                                        <artifactId>camel-spring-boot-dependencies</artifactId>
                                        <version>${camel-spring-boot-version}</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                        </project>
                        """,
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <camel-spring-boot-version>4.18.5</camel-spring-boot-version>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>org.apache.camel.springboot</groupId>
                                        <artifactId>camel-spring-boot-dependencies</artifactId>
                                        <version>${camel-spring-boot-version}</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                        </project>
                        """
                )
        );
    }

    /**
     * Alternate property name variant {@code camel.springboot.version}.
     */
    @Test
    void alternateCamelSpringbootVersionPropertyIsUpdated() {
        //language=xml
        rewriteRun(
                pomXml(
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <camel.springboot.version>4.10.0</camel.springboot.version>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>org.apache.camel.springboot</groupId>
                                        <artifactId>camel-spring-boot-dependencies</artifactId>
                                        <version>${camel.springboot.version}</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                        </project>
                        """,
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <camel.springboot.version>4.18.5</camel.springboot.version>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>org.apache.camel.springboot</groupId>
                                        <artifactId>camel-spring-boot-dependencies</artifactId>
                                        <version>${camel.springboot.version}</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                        </project>
                        """
                )
        );
    }

    /**
     * Literal-version BOM, handled by the {@code UpgradeDependencyVersion} entry.
     */
    @Test
    void literalVersionBomIsUpdated() {
        //language=xml
        rewriteRun(
                pomXml(
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0.0</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>org.apache.camel.springboot</groupId>
                                        <artifactId>camel-spring-boot-dependencies</artifactId>
                                        <version>4.10.0</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                        </project>
                        """,
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0.0</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>org.apache.camel.springboot</groupId>
                                        <artifactId>camel-spring-boot-dependencies</artifactId>
                                        <version>4.18.5</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                        </project>
                        """
                )
        );
    }

    /**
     * Every aggregate ({@code latest.yaml} and the {@code *LTS.yaml} files) that upgrades the
     * {@code org.apache.camel.springboot} BOM must also set the property names it is commonly
     * managed through, to the same target version. This is checked on the yaml source so that it
     * does not depend on the target version being resolvable.
     */
    @Test
    void aggregatesChangeTheBomProperties() throws IOException {
        List<Path> aggregates;
        try (Stream<Path> files = Files.list(REWRITE_DIR)) {
            aggregates = files
                    .filter(p -> p.getFileName().toString().equals("latest.yaml")
                            || p.getFileName().toString().endsWith("LTS.yaml"))
                    .sorted()
                    .toList();
        }
        assertFalse(aggregates.isEmpty(), "no aggregate yaml found in " + REWRITE_DIR.toAbsolutePath());

        for (Path aggregate : aggregates) {
            List<Map<String, String>> entries = recipeEntries(Files.readString(aggregate));
            String bomVersion = entries.stream()
                    .filter(e -> "org.openrewrite.maven.UpgradeDependencyVersion".equals(e.get("recipe")))
                    .filter(e -> "org.apache.camel.springboot".equals(e.get("groupId")))
                    .map(e -> e.get("newVersion"))
                    .findFirst()
                    .orElse(null);
            assertNotNull(bomVersion, aggregate.getFileName() + " does not upgrade the org.apache.camel.springboot BOM");
            for (String property : BOM_PROPERTIES) {
                assertTrue(entries.stream().anyMatch(e -> "org.openrewrite.maven.ChangePropertyValue".equals(e.get("recipe"))
                                && property.equals(e.get("key"))
                                && bomVersion.equals(e.get("newValue"))),
                        aggregate.getFileName() + " does not set the " + property + " property to " + bomVersion);
            }
        }
    }

    private static final Pattern ENTRY = Pattern.compile("^  - ([\\w.]+):?\\s*$");
    private static final Pattern OPTION = Pattern.compile("^      (\\w+):\\s*(.+?)\\s*$");

    /**
     * Reads the {@code recipeList} entries of a yaml file as maps of the recipe name ({@code recipe})
     * and its options. Only the flat {@code key: value} options used by the aggregates are read.
     */
    private static List<Map<String, String>> recipeEntries(String yaml) {
        List<Map<String, String>> entries = new ArrayList<>();
        Map<String, String> current = null;
        for (String line : yaml.split("\n")) {
            Matcher entry = ENTRY.matcher(line);
            if (entry.matches()) {
                current = new LinkedHashMap<>();
                current.put("recipe", entry.group(1));
                entries.add(current);
                continue;
            }
            Matcher option = OPTION.matcher(line);
            if (current != null && option.matches()) {
                current.put(option.group(1), option.group(2).replaceAll("^['\"]|['\"]$", ""));
            }
        }
        return entries;
    }
}
