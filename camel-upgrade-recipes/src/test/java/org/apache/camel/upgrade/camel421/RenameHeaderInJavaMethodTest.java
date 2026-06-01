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

public class RenameHeaderInJavaMethodTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameHeaderInJavaMethod("kafka.TOPIC", "CamelKafkaTopic"))
            .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_20,
                    "camel-core-model", "camel-api"))
            .typeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void testSetHeaderMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("kafka.TOPIC", "my-topic");
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("CamelKafkaTopic", "my-topic");
                    }
                }
                """
            )
        );
    }

    @Test
    void testGetHeaderWithClassMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        String topic = exchange.getIn().getHeader("kafka.TOPIC", String.class);
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        String topic = exchange.getIn().getHeader("CamelKafkaTopic", String.class);
                    }
                }
                """
            )
        );
    }

    @Test
    void testGetHeaderNoClassMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        Object topic = exchange.getIn().getHeader("kafka.TOPIC");
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        Object topic = exchange.getIn().getHeader("CamelKafkaTopic");
                    }
                }
                """
            )
        );
    }

    @Test
    void testGetHeaderWithDefaultMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        String topic = exchange.getIn().getHeader("kafka.TOPIC", "default", String.class);
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        String topic = exchange.getIn().getHeader("CamelKafkaTopic", "default", String.class);
                    }
                }
                """
            )
        );
    }

    @Test
    void testMultipleHeadersMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("kafka.TOPIC", "my-topic");
                        String topic = exchange.getIn().getHeader("kafka.TOPIC");
                        exchange.getIn().setHeader("other.header", "value");
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("CamelKafkaTopic", "my-topic");
                        String topic = exchange.getIn().getHeader("CamelKafkaTopic");
                        exchange.getIn().setHeader("other.header", "value");
                    }
                }
                """
            )
        );
    }

    @Test
    void testDoesNotMigrateArbitraryStrings() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void someMethod(Exchange exchange) {
                        String headerName = "kafka.TOPIC"; // Should NOT be changed
                        System.out.println("Setting header: kafka.TOPIC"); // Should NOT be changed
                    }
                }
                """
                // No change expected
            )
        );
    }

    @Test
    void testDoesNotMigrateDynamicAccess() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void someMethod(Exchange exchange, String headerName) {
                        exchange.getIn().getHeader(headerName); // Should NOT be changed
                    }
                }
                """
                // No change expected
            )
        );
    }

    @Test
    void testDoesNotMigrateMapGet() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void someMethod(Exchange exchange) {
                        Object topic = exchange.getIn().getHeaders().get("kafka.TOPIC"); // Should NOT be changed
                    }
                }
                """
                // No change expected
            )
        );
    }

    @Test
    void testDoesNotMigrateDifferentHeaderName() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("kafka.PARTITION", "0"); // Different header, should NOT be changed
                    }
                }
                """
                // No change expected
            )
        );
    }
}
