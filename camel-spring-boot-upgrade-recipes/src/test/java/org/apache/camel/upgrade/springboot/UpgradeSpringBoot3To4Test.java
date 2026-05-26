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
import org.openrewrite.config.Environment;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.maven.Assertions.pomXml;

class UpgradeSpringBoot3To4Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(Environment.builder()
                .scanYamlResources()
                .build()
                .activateRecipes("org.apache.camel.upgrade.spring.UpgradeSpringBoot3To4"));
    }

    @Test
    void javaVersionNotDowngraded() {
        rewriteRun(
            //language=xml
            pomXml(
                """
                    <project>
                        <modelVersion>4.0.0</modelVersion>
                        <groupId>com.example</groupId>
                        <artifactId>test</artifactId>
                        <version>1.0.0</version>
                        <properties>
                            <java.version>21</java.version>
                            <maven.compiler.source>21</maven.compiler.source>
                            <maven.compiler.target>21</maven.compiler.target>
                            <maven.compiler.release>21</maven.compiler.release>
                        </properties>
                    </project>
                    """
            )
        );
    }

    @Test
    void javaVersionUpgradedFromOlder() {
        rewriteRun(
            //language=xml
            pomXml(
                """
                    <project>
                        <modelVersion>4.0.0</modelVersion>
                        <groupId>com.example</groupId>
                        <artifactId>test</artifactId>
                        <version>1.0.0</version>
                        <properties>
                            <java.version>11</java.version>
                        </properties>
                    </project>
                    """,
                """
                    <project>
                        <modelVersion>4.0.0</modelVersion>
                        <groupId>com.example</groupId>
                        <artifactId>test</artifactId>
                        <version>1.0.0</version>
                        <properties>
                            <java.version>17</java.version>
                        </properties>
                    </project>
                    """
            )
        );
    }
}
