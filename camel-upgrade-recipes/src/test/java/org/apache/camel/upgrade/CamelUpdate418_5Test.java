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
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;

/**
 * Tests for migrating from Camel 4.18.4 to 4.18.5: the camel-mustache and camel-chunk header renames
 * (CAMEL-24895), which also ship in 4.22.2 and 4.23.0 and are referenced from those chains.
 */
public class CamelUpdate418_5Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_18_5)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_18, "camel-api", "camel-core-model", "camel-support"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void migrateMustacheHeaders() {
        //language=java
        rewriteRun(
                mavenProject("test-mustache",
                        CamelTestUtil.pomXmlSpec("camel-mustache", CamelTestUtil.CamelVersion.v4_18_4),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class MustacheRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .setHeader("MustacheResourceUri", constant("other.mustache"))
                                            .setHeader("MustacheTemplate", constant("Hello {{name}}"))
                                            .to("mustache:template.mustache");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class MustacheRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .setHeader("CamelMustacheResourceUri", constant("other.mustache"))
                                            .setHeader("CamelMustacheTemplate", constant("Hello {{name}}"))
                                            .to("mustache:template.mustache");
                                    }
                                }
                                """
                        )
                )
        );
    }


    @Test
    void preconditionBlocksMustacheHeadersWithoutDependency() {
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(CamelTestUtil.isRecipeOverridden() ? 1 : 0),
                mavenProject("test-no-mustache",
                        CamelTestUtil.pomXmlSpec("camel-core", CamelTestUtil.CamelVersion.v4_18_4),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class NoMustacheRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .setHeader("MustacheResourceUri", constant("other.mustache"))
                                            .to("mock:result");
                                    }
                                }
                                """
                        )
                )
        );
    }


    @Test
    void migrateChunkHeaders() {
        //language=java
        rewriteRun(
                mavenProject("test-chunk",
                        CamelTestUtil.pomXmlSpec("camel-chunk", CamelTestUtil.CamelVersion.v4_18_4),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class ChunkRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .setHeader("ChunkResourceUri", constant("other"))
                                            .setHeader("ChunkTemplate", constant("Hello {$name}"))
                                            .to("chunk:template");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class ChunkRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .setHeader("CamelChunkResourceUri", constant("other"))
                                            .setHeader("CamelChunkTemplate", constant("Hello {$name}"))
                                            .to("chunk:template");
                                    }
                                }
                                """
                        )
                )
        );
    }


    @Test
    void preconditionBlocksChunkHeadersWithoutDependency() {
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(CamelTestUtil.isRecipeOverridden() ? 1 : 0),
                mavenProject("test-no-chunk",
                        CamelTestUtil.pomXmlSpec("camel-core", CamelTestUtil.CamelVersion.v4_18_4),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class NoChunkRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .setHeader("ChunkResourceUri", constant("other"))
                                            .to("mock:result");
                                    }
                                }
                                """
                        )
                )
        );
    }

}
