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
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate419Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("org.apache.camel.upgrade.camel419.CamelMigrationRecipe")
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_18,
                        "camel-core-model", "camel-api"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_mdc_older_logic_deprecation">MDC Logging migration</a>
     */
    @DocumentExample
    @Test
    void migrateMdcLogging() {
        //language=xml
        rewriteRun(
                pomXml(
                        """
                        <project>
                           <modelVersion>4.0.0</modelVersion>

                           <artifactId>test</artifactId>
                           <groupId>org.apache.camel.test</groupId>
                           <version>1.0.0</version>

                           <properties>
                              <camel.version>4.18.0</camel.version>
                           </properties>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-core</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                             </dependencies>
                        </project>
                        """,
                        """
                        <project>
                           <modelVersion>4.0.0</modelVersion>

                           <artifactId>test</artifactId>
                           <groupId>org.apache.camel.test</groupId>
                           <version>1.0.0</version>

                           <properties>
                              <camel.version>4.18.0</camel.version>
                           </properties>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-core</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                              <dependency>
                                 <groupId>org.apache.camel</groupId>
                                 <artifactId>camel-mdc</artifactId>
                                 <version>${camel.version}</version>
                              </dependency>
                             </dependencies>
                        </project>
                        """
                ),
                properties(
                        """
                        camel.main.useMdcLogging=true
                        """,
                        """
                        """,
                        spec -> spec.path("application.properties")
                )
        );
    }
}
