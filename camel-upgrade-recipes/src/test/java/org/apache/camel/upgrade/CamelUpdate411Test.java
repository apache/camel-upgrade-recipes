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

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;

class CamelUpdate411Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_11)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_10, "camel-platform-http"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://github.com/apache/camel/blob/main/docs/user-manual/modules/ROOT/pages/camel-4x-upgrade-guide-4_10.adoc#camel-smb">camel-smb</a>
     */
    @DocumentExample
    @Test
    void platformHttpFilterStrategy() {
        //language=java
        rewriteRun(java(
                """
                        import org.apache.camel.component.platform.http.PlatformHttpHeaderFilterStrategy;
                        
                        public class SmbTest {
                            public void test() {
                                   PlatformHttpHeaderFilterStrategy strategy = new PlatformHttpHeaderFilterStrategy();
                            }
                        }
                        """,
                """
                        import org.apache.camel.http.base.HttpHeaderFilterStrategy;
                        
                        public class SmbTest {
                            public void test() {
                                   HttpHeaderFilterStrategy strategy = new HttpHeaderFilterStrategy();
                            }
                        }
                        """));
    }

    /**
     * <a href="https://github.com/apache/camel/blob/main/docs/user-manual/modules/ROOT/pages/camel-4x-upgrade-guide-4_10.adoc#camel-smb">camel-smb</a>
     */
    @Test
    void lightweightRemoval() {
        //language=java
        rewriteRun(
                properties(
                        """
                          camel.main.lightweight=true
                          camel.main.other_properties=true
                          """,
                        """
                          camel.main.other_properties=true
                          """
                )
        );
    }


    /**
     * Removed camel-etcd3.
     *
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_11.html#_camel_etcd3">Removed deprecated components</a>
     */
    @Test
    void removedDependencies() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                           <modelVersion>4.0.0</modelVersion>

                           <artifactId>test</artifactId>
                           <groupId>org.apache.camel.test</groupId>
                           <version>1.0.0</version>

                           <properties>
                               <camel.version>4.10.0</camel.version>
                           </properties>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-api</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-etcd3</artifactId>
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
                               <camel.version>4.10.0</camel.version>
                           </properties>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-api</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                            </dependencies>

                        </project>
                        """));
    }
}
