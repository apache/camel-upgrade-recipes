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

import static org.openrewrite.maven.Assertions.pomXml;

/**
 * Camel Spring Boot applications commonly inherit from the
 * {@code org.apache.camel.springboot:spring-boot} parent, as Apache's own camel-spring-boot-examples
 * do. {@code UpgradeParentVersion} was only scoped to {@code org.apache.camel}, which OpenRewrite
 * matches as a glob without a wildcard, so such a parent was left on its original version and the
 * project kept resolving the Camel release it started from.
 * <p>
 * The LTS recipe is used because its target is a fixed released version, unlike the latest recipe
 * whose target follows {@code project.version}.
 */
class CamelSpringBootParentVersionTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("org.apache.camel.upgrade.CamelSpringBoot418LTSMigrationRecipe");
    }

    @DocumentExample
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
                                        <version>4.10.0</version>
                                    </parent>
                                    <groupId>com.example</groupId>
                                    <artifactId>test</artifactId>
                                    <version>1.0.0</version>
                                </project>
                                """,
                        """
                                <project>
                                    <modelVersion>4.0.0</modelVersion>
                                    <parent>
                                        <groupId>org.apache.camel.springboot</groupId>
                                        <artifactId>spring-boot</artifactId>
                                        <version>4.18.3</version>
                                    </parent>
                                    <groupId>com.example</groupId>
                                    <artifactId>test</artifactId>
                                    <version>1.0.0</version>
                                </project>
                                """));
    }
}
