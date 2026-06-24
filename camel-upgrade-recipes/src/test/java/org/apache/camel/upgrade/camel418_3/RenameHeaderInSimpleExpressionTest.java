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
package org.apache.camel.upgrade.camel418_3;

import org.apache.camel.upgrade.CamelTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class RenameHeaderInSimpleExpressionTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameHeaderInSimpleExpression("kafka.TOPIC", "CamelKafkaTopic"))
            .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_20,
                    "camel-core-model", "camel-api"))
            .typeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void simpleExpressionHeaderMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${header.kafka.TOPIC}"));
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${header.CamelKafkaTopic}"));
                    }
                }
                """
            )
        );
    }

    @Test
    void simpleExpressionHeadersVariantMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${headers.kafka.TOPIC}"));
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${headers.CamelKafkaTopic}"));
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
                            .setHeader("myHeader", simple("${header.kafka.TOPIC}"));
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setHeader("myHeader", simple("${header.CamelKafkaTopic}"));
                    }
                }
                """
            )
        );
    }

    @Test
    void simpleExpressionWithOtherText() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("Topic: ${header.kafka.TOPIC}, Partition: ${header.kafka.PARTITION}"));
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("Topic: ${header.CamelKafkaTopic}, Partition: ${header.kafka.PARTITION}"));
                    }
                }
                """
            )
        );
    }

    @Test
    void multipleSimpleExpressions() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${header.kafka.TOPIC}"))
                            .setHeader("copy", simple("${headers.kafka.TOPIC}"));
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${header.CamelKafkaTopic}"))
                            .setHeader("copy", simple("${headers.CamelKafkaTopic}"));
                    }
                }
                """
            )
        );
    }

    @Test
    void doesNotMigrateNonSimpleStrings() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        String expression = "${header.kafka.TOPIC}"; // Not inside simple() call
                        System.out.println("Expression: ${header.kafka.TOPIC}");
                    }
                }
                """
                // No change expected
            )
        );
    }

    @Test
    void doesNotMigrateDifferentHeader() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${header.kafka.PARTITION}")); // Different header
                    }
                }
                """
                // No change expected
            )
        );
    }

    @Test
    void doesNotMigratePartialMatch() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("${header.kafka.TOPIC.name}")); // Has extra suffix
                    }
                }
                """
                // No change expected - doesn't match exact pattern
            )
        );
    }
}
