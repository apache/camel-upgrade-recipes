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
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class RenameHeaderPrefixInSimpleExpressionTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameHeaderPrefixInSimpleExpression("SolrField.", "CamelSolrField."))
            .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_20,
                    "camel-core-model", "camel-api"))
            .typeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void simpleExpressionHeaderPrefixMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${header.SolrField.id}"));
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${header.CamelSolrField.id}"));
                    }
                }
                """
            )
        );
    }

    @Test
    void simpleExpressionHeadersVariantPrefixMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${headers.SolrField.id}"));
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${headers.CamelSolrField.id}"));
                    }
                }
                """
            )
        );
    }

    @Test
    void simpleExpressionInSetHeader() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setHeader("result", simple("Value: ${header.SolrField.name}"));
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setHeader("result", simple("Value: ${header.CamelSolrField.name}"));
                    }
                }
                """
            )
        );
    }

    @Test
    void multipleHeaderReferencesInOneExpression() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("ID: ${header.SolrField.id}, Name: ${header.SolrField.name}"));
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("ID: ${header.CamelSolrField.id}, Name: ${header.CamelSolrField.name}"));
                    }
                }
                """
            )
        );
    }

    @Test
    void doesNotMigrateDifferentPrefix() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${header.SolrParam.commit}")); // Different prefix, should NOT be changed
                    }
                }
                """
                // No change expected
            )
        );
    }

    @Test
    void doesNotMigrateArbitraryStrings() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .log("Header: SolrField.id"); // Should NOT be changed
                    }
                }
                """
                // No change expected
            )
        );
    }

    @Test
    void doesNotMigrateOutsideSimpleMethod() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody("${header.SolrField.id}"); // Not inside simple() call, should NOT be changed
                    }
                }
                """
                // No change expected
            )
        );
    }

    @Test
    void migratesVariousFieldNames() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setHeader("h1", simple("${header.SolrField.id}"))
                            .setHeader("h2", simple("${header.SolrField.name}"))
                            .setHeader("h3", simple("${header.SolrField.description}"))
                            .setHeader("h4", simple("${header.SolrField.custom_field_123}"));
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setHeader("h1", simple("${header.CamelSolrField.id}"))
                            .setHeader("h2", simple("${header.CamelSolrField.name}"))
                            .setHeader("h3", simple("${header.CamelSolrField.description}"))
                            .setHeader("h4", simple("${header.CamelSolrField.custom_field_123}"));
                    }
                }
                """
            )
        );
    }
}
