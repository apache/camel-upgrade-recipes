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
package org.apache.camel.upgrade.camel421;

import org.apache.camel.upgrade.CamelTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.maven.Assertions.pomXml;

/**
 * Tests for Grok library dependency migration (io.krakens → io.github.whatap)
 */
public class CamelGrokDependencyTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_21, "org.apache.camel.upgrade.camel421.upgradeGrokDependency");
    }

    @DocumentExample
    @Test
    void testGrokDependencyMigration() {
        //language=xml
        rewriteRun(
            pomXml(
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <dependency>
                            <groupId>io.krakens</groupId>
                            <artifactId>java-grok</artifactId>
                            <version>0.1.9</version>
                        </dependency>
                    </dependencies>
                </project>
                """,
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <!--~~(Unable to download POM: io.github.whatap:java-grok:0.1.9. Tried repositories:
                https://repo.maven.apache.org/maven2: HTTP 404)~~>--><dependency>
                            <groupId>io.github.whatap</groupId>
                            <artifactId>java-grok</artifactId>
                            <version>0.1.9</version>
                        </dependency>
                    </dependencies>
                </project>
                """
            )
        );
    }

    @Test
    void testGrokDependencyWithScope() {
        //language=xml
        rewriteRun(
            pomXml(
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <dependency>
                            <groupId>io.krakens</groupId>
                            <artifactId>java-grok</artifactId>
                            <version>0.1.9</version>
                            <scope>compile</scope>
                        </dependency>
                    </dependencies>
                </project>
                """,
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <!--~~(Unable to download POM: io.github.whatap:java-grok:0.1.9. Tried repositories:
                https://repo.maven.apache.org/maven2: HTTP 404)~~>--><dependency>
                            <groupId>io.github.whatap</groupId>
                            <artifactId>java-grok</artifactId>
                            <version>0.1.9</version>
                            <scope>compile</scope>
                        </dependency>
                    </dependencies>
                </project>
                """
            )
        );
    }

    @Test
    void testGrokDependencyInDependencyManagement() {
        //language=xml
        rewriteRun(
            pomXml(
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>io.krakens</groupId>
                                <artifactId>java-grok</artifactId>
                                <version>0.1.9</version>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                </project>
                """,
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>io.github.whatap</groupId>
                                <artifactId>java-grok</artifactId>
                                <version>0.1.9</version>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                </project>
                """
            )
        );
    }
}
