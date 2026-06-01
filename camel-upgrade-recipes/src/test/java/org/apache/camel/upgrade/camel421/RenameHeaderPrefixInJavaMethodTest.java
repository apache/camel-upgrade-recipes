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

public class RenameHeaderPrefixInJavaMethodTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameHeaderPrefixInJavaMethod("SolrField.", "CamelSolrField."))
            .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_20,
                    "camel-core-model", "camel-api"))
            .typeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void setHeaderPrefixMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("SolrField.id", "doc123");
                        exchange.getIn().setHeader("SolrField.name", "Test Document");
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("CamelSolrField.id", "doc123");
                        exchange.getIn().setHeader("CamelSolrField.name", "Test Document");
                    }
                }
                """
            )
        );
    }

    @Test
    void getHeaderWithClassPrefixMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        String id = exchange.getIn().getHeader("SolrField.id", String.class);
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        String id = exchange.getIn().getHeader("CamelSolrField.id", String.class);
                    }
                }
                """
            )
        );
    }

    @Test
    void getHeaderNoClassPrefixMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        Object id = exchange.getIn().getHeader("SolrField.id");
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        Object id = exchange.getIn().getHeader("CamelSolrField.id");
                    }
                }
                """
            )
        );
    }

    @Test
    void getHeaderWithDefaultPrefixMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        String id = exchange.getIn().getHeader("SolrField.id", "default", String.class);
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        String id = exchange.getIn().getHeader("CamelSolrField.id", "default", String.class);
                    }
                }
                """
            )
        );
    }

    @Test
    void multipleHeadersPrefixMigration() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("SolrField.id", "doc123");
                        String id = exchange.getIn().getHeader("SolrField.id");
                        exchange.getIn().setHeader("SolrField.title", "My Document");
                        exchange.getIn().setHeader("other.header", "value");
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("CamelSolrField.id", "doc123");
                        String id = exchange.getIn().getHeader("CamelSolrField.id");
                        exchange.getIn().setHeader("CamelSolrField.title", "My Document");
                        exchange.getIn().setHeader("other.header", "value");
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
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("SolrParam.commit", true); // Different prefix, should NOT be changed
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
                import org.apache.camel.Exchange;

                class Test {
                    void someMethod(Exchange exchange) {
                        String headerName = "SolrField.id"; // Should NOT be changed
                        System.out.println("Setting header: SolrField.id"); // Should NOT be changed
                    }
                }
                """
                // No change expected
            )
        );
    }

    @Test
    void doesNotMigrateDynamicAccess() {
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
    void doesNotMigrateMapGet() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;

                class Test {
                    void someMethod(Exchange exchange) {
                        Object id = exchange.getIn().getHeaders().get("SolrField.id"); // Should NOT be changed
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
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("SolrField.id", "123");
                        exchange.getIn().setHeader("SolrField.name", "Test");
                        exchange.getIn().setHeader("SolrField.description", "Desc");
                        exchange.getIn().setHeader("SolrField.custom_field_123", "Custom");
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;

                class Test {
                    void configure(Exchange exchange) {
                        exchange.getIn().setHeader("CamelSolrField.id", "123");
                        exchange.getIn().setHeader("CamelSolrField.name", "Test");
                        exchange.getIn().setHeader("CamelSolrField.description", "Desc");
                        exchange.getIn().setHeader("CamelSolrField.custom_field_123", "Custom");
                    }
                }
                """
            )
        );
    }
}
