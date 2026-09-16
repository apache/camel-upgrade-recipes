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
import org.openrewrite.config.Environment;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.maven.Assertions.pomXml;

/**
 * Each test activates its own narrow camel423 sub-recipe rather than the full
 * camel423.CamelSpringBootMigrationRecipe chain. That chain transitively includes
 * camel419.CamelSpringBootMigrationRecipe, which unconditionally upgrades every existing
 * org.apache.camel.springboot:* dependency to @camel-spring-boot-version@ (this reactor's own
 * project.version) as its very first step; exercising the full chain here would make every
 * assertion depend on that unrelated, reactor-version-sensitive bump instead of on the recipe under
 * test. LatestRecipeTest and the coverage in CamelSpringBoot422Test already guard that the chain is
 * wired correctly.
 */
class CamelSpringBoot423Test implements RewriteTest {

    private static RecipeSpec withRecipe(RecipeSpec spec, String... recipeNames) {
        return spec.recipe(Environment.builder()
                .scanYamlResources()
                .build()
                .activateRecipes(recipeNames));
    }

    @DocumentExample
    @Test
    void removeCSimpleJoorStarterDependency() {
        //language=xml
        rewriteRun(
                spec -> withRecipe(spec, "org.apache.camel.upgrade.camel423.removeCSimpleJoorStarterDependency"),
                pomXml(
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencies>
                                <dependency>
                                    <groupId>org.apache.camel.springboot</groupId>
                                    <artifactId>camel-csimple-joor-starter</artifactId>
                                    <version>4.22.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """,
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                        </project>
                        """
                )
        );
    }

    @Test
    void migrateLangchain4jToolsStarterDependency() {
        //language=xml
        rewriteRun(
                spec -> withRecipe(spec, "org.apache.camel.upgrade.camel423.migrateLangchain4jToolsStarterDependency"),
                pomXml(
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencies>
                                <dependency>
                                    <groupId>org.apache.camel.springboot</groupId>
                                    <artifactId>camel-langchain4j-tools-starter</artifactId>
                                    <version>4.22.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """,
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencies>
                                <dependency>
                                    <groupId>org.apache.camel.springboot</groupId>
                                    <artifactId>camel-ai-tool-starter</artifactId>
                                    <version>4.23.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """
                )
        );
    }

    @Test
    void migrateLangchain4jToolsStarterDoesNotDuplicateAiToolStarter() {
        // camel422.migrateAiToolStarterDependency only swaps a plain org.apache.camel:camel-ai-tool
        // dependency; running it alongside camel423's starter-to-starter rename must not produce two
        // camel-ai-tool-starter entries.
        //language=xml
        rewriteRun(
                spec -> withRecipe(spec,
                        "org.apache.camel.upgrade.camel422.migrateAiToolStarterDependency",
                        "org.apache.camel.upgrade.camel423.migrateLangchain4jToolsStarterDependency"),
                pomXml(
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencies>
                                <dependency>
                                    <groupId>org.springframework.boot</groupId>
                                    <artifactId>spring-boot-starter</artifactId>
                                    <version>4.1.0</version>
                                </dependency>
                                <dependency>
                                    <groupId>org.apache.camel.springboot</groupId>
                                    <artifactId>camel-langchain4j-tools-starter</artifactId>
                                    <version>4.22.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """,
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencies>
                                <dependency>
                                    <groupId>org.springframework.boot</groupId>
                                    <artifactId>spring-boot-starter</artifactId>
                                    <version>4.1.0</version>
                                </dependency>
                                <dependency>
                                    <groupId>org.apache.camel.springboot</groupId>
                                    <artifactId>camel-ai-tool-starter</artifactId>
                                    <version>4.23.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """
                )
        );
    }

    @Test
    void migrateZeebeStarterDependency() {
        //language=xml
        rewriteRun(
                spec -> withRecipe(spec, "org.apache.camel.upgrade.camel423.migrateZeebeStarterDependency"),
                pomXml(
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencies>
                                <dependency>
                                    <groupId>org.apache.camel.springboot</groupId>
                                    <artifactId>camel-zeebe-starter</artifactId>
                                    <version>4.22.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """,
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencies>
                                <dependency>
                                    <groupId>org.apache.camel.springboot</groupId>
                                    <artifactId>camel-camunda-starter</artifactId>
                                    <version>4.23.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """
                )
        );
    }
}
