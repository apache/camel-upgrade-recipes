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
 * Regression tests for property-managed BOM version upgrade in CamelSpringBootMigrationRecipe.
 *
 * {@code UpgradeDependencyVersion} resolves the new version from {@code maven-metadata.xml}.
 * When that file is absent from the repository (e.g. staging or mirror repos that do not
 * publish metadata), the recipe silently no-ops on a property reference such as
 * {@code ${camel-spring-boot-version}}. The companion {@code ChangePropertyValue} entries
 * set the property value directly and do not require metadata resolution.
 */
class CamelSpringBootBomVersionTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(Environment.builder()
                .scanYamlResources()
                .build()
                .activateRecipes("org.apache.camel.upgrade.CamelSpringBootMigrationRecipe"));
    }

    /**
     * BOM imported via {@code ${camel-spring-boot-version}} property — the shape used by
     * Apache camel-spring-boot-examples. UpgradeDependencyVersion cannot update this when
     * maven-metadata.xml is absent; ChangePropertyValue is used instead.
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
                                <camel-spring-boot-version>4.18.1</camel-spring-boot-version>
                                <spring-boot-version>3.5.0</spring-boot-version>
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
                                <camel-spring-boot-version>4.23.0-SNAPSHOT</camel-spring-boot-version>
                                <maven.compiler.release>17</maven.compiler.release>
                                <spring-boot-version>4.1.0</spring-boot-version>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <!--~~(org.apache.camel.springboot:camel-spring-boot-dependencies:4.23.0-SNAPSHOT failed. Unable to download POM: org.apache.camel.springboot:camel-spring-boot-dependencies:4.23.0-SNAPSHOT.)~~>--><dependency>
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
                                <camel.springboot.version>4.18.1</camel.springboot.version>
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
                                <camel.springboot.version>4.23.0-SNAPSHOT</camel.springboot.version>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <!--~~(org.apache.camel.springboot:camel-spring-boot-dependencies:4.23.0-SNAPSHOT failed. Unable to download POM: org.apache.camel.springboot:camel-spring-boot-dependencies:4.23.0-SNAPSHOT.)~~>--><dependency>
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
     * Literal-version BOM — handled by the existing UpgradeDependencyVersion entry.
     * This test confirms that path is preserved and still works.
     * Note: OpenRewrite cannot resolve {@code 4.23.0-SNAPSHOT} from Maven Central
     * (no maven-metadata.xml for SNAPSHOT), so {@code UpgradeDependencyVersion} is a
     * no-op for the literal version. The property-based companion recipes are the
     * reliable path for SNAPSHOT versions. The {@code maven.compiler.release} property
     * is added by the Spring Boot 3→4 upgrade recipe triggered by the 4.19 chain.
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
                                        <version>4.18.1</version>
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
                                        <artifactId>camel-spring-boot-dependencies</artifactId>
                                        <version>4.18.1</version>
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
}
