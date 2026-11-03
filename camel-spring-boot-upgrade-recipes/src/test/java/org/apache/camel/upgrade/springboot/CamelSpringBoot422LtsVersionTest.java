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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openrewrite.maven.Assertions.pomXml;

/**
 * Target versions of {@code CamelSpringBoot422LTSMigrationRecipe} (the 4.22 LTS aggregate).
 * <p>
 * The aggregate chains {@code camel422.CamelSpringBootMigrationRecipe}, and through it every version
 * recipe down to 4.9, so these tests also guard that none of them pins the project to the latest
 * Camel release: {@code UpgradeDependencyVersion} never downgrades, so such a bump would leave the
 * project on 4.23+ instead of 4.22.x.
 */
class CamelSpringBoot422LtsVersionTest implements RewriteTest {

    private static final String LTS_VERSION = "4.22.2";

    /**
     * Spring Boot line of camel-spring-boot 4.22.x. The aggregate shares its Spring targets with
     * {@code latest.yaml}; when the latest release moves to a newer Spring Boot line, the 4.22 LTS
     * aggregate needs Spring targets of its own, and the Spring migration chained through
     * {@code camel419.CamelSpringBootMigrationRecipe} must no longer pin the latest one.
     */
    private static final String SPRING_BOOT_LINE = "4.1.";

    private static final Pattern SPRING_BOOT_PARENT_VERSION = Pattern.compile(
            "<artifactId>spring-boot-starter-parent</artifactId>\\s*<version>([^<]+)</version>");

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("org.apache.camel.upgrade.CamelSpringBoot422LTSMigrationRecipe");
    }

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
                                <camel-spring-boot-version>4.18.5</camel-spring-boot-version>
                                <maven.compiler.release>17</maven.compiler.release>
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
                                <camel-spring-boot-version>%s</camel-spring-boot-version>
                                <maven.compiler.release>17</maven.compiler.release>
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
                        """.formatted(LTS_VERSION)
                )
        );
    }

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
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>org.apache.camel.springboot</groupId>
                                        <artifactId>camel-spring-boot-bom</artifactId>
                                        <version>4.18.5</version>
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
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>org.apache.camel.springboot</groupId>
                                        <artifactId>camel-spring-boot-bom</artifactId>
                                        <version>%s</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                        </project>
                        """.formatted(LTS_VERSION)
                )
        );
    }

    /**
     * The Java 17 step of the Spring Boot 3 to 4 migration (chained through
     * {@code camel419.CamelSpringBootMigrationRecipe}) also overrides the {@code javaVersion}
     * property, which the parent inherits from the Apache parent POM with an older value.
     */
    @Test
    void camelSpringBootParent() {
        //language=xml
        rewriteRun(
                pomXml(
                        """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <parent>
                                <groupId>org.apache.camel.springboot</groupId>
                                <artifactId>spring-boot</artifactId>
                                <version>4.18.5</version>
                            </parent>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                        </project>
                        """,
                        """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <parent>
                                <groupId>org.apache.camel.springboot</groupId>
                                <artifactId>spring-boot</artifactId>
                                <version>%s</version>
                            </parent>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <javaVersion>17</javaVersion>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                        </project>
                        """.formatted(LTS_VERSION)
                )
        );
    }

    /**
     * A 4.18 LTS application runs on Spring Boot 3.5; the 4.22 LTS line requires Spring Boot 4.1.
     * Only the Spring Boot line is asserted, so that a Spring Boot patch bump in the pom does not
     * break the test, while a move of the shared Spring targets to another line does.
     */
    @Test
    void springBootParentMovesToTheLtsLine() {
        //language=xml
        String before = """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>3.5.16</version>
                    </parent>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                </project>
                """;
        rewriteRun(
                pomXml(before, spec -> spec.after(actual -> {
                    Matcher version = SPRING_BOOT_PARENT_VERSION.matcher(actual);
                    assertTrue(version.find(), "spring-boot-starter-parent not found in:\n" + actual);
                    assertTrue(version.group(1).startsWith(SPRING_BOOT_LINE),
                            "spring-boot-starter-parent upgraded to " + version.group(1) + ", expected the "
                                    + SPRING_BOOT_LINE + "x line of camel-spring-boot " + LTS_VERSION);
                    return before.replace("3.5.16", version.group(1));
                }))
        );
    }
}
