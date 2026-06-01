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

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.yaml.Assertions.yaml;

public class RenameHeaderPrefixInYamlDslTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameHeaderPrefixInYamlDsl("SolrField.", "CamelSolrField."));
    }

    @DocumentExample
    @Test
    void setHeaderPrefixMigration() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: SolrField.id
                          constant: doc123
                """,
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: CamelSolrField.id
                          constant: doc123
                """
            )
        );
    }

    @Test
    void headerPredicatePrefixMigration() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - choice:
                          when:
                            - header:
                                name: SolrField.id
                              steps:
                                - to: "mock:found"
                """,
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - choice:
                          when:
                            - header:
                                name: CamelSolrField.id
                              steps:
                                - to: "mock:found"
                """
            )
        );
    }

    @Test
    void removeHeaderPrefixMigration() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - removeHeader:
                          name: SolrField.id
                """,
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - removeHeader:
                          name: CamelSolrField.id
                """
            )
        );
    }

    @Test
    void multipleHeadersPrefixMigration() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: SolrField.id
                          constant: "123"
                      - setHeader:
                          name: SolrField.name
                          constant: "Test"
                """,
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: CamelSolrField.id
                          constant: "123"
                      - setHeader:
                          name: CamelSolrField.name
                          constant: "Test"
                """
            )
        );
    }

    @Test
    void doesNotMigrateDifferentPrefix() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: SolrParam.commit
                          constant: true
                """
                // No change expected
            )
        );
    }

    @Test
    void doesNotMigrateArbitraryYaml() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - log:
                          message: "Header: SolrField.id"
                """
                // No change expected
            )
        );
    }

    @Test
    void migratesVariousFieldNames() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: SolrField.id
                          constant: "1"
                      - setHeader:
                          name: SolrField.name
                          constant: "Test"
                      - setHeader:
                          name: SolrField.description
                          constant: "Desc"
                      - setHeader:
                          name: SolrField.custom_field_123
                          constant: "Custom"
                """,
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: CamelSolrField.id
                          constant: "1"
                      - setHeader:
                          name: CamelSolrField.name
                          constant: "Test"
                      - setHeader:
                          name: CamelSolrField.description
                          constant: "Desc"
                      - setHeader:
                          name: CamelSolrField.custom_field_123
                          constant: "Custom"
                """
            )
        );
    }
}
