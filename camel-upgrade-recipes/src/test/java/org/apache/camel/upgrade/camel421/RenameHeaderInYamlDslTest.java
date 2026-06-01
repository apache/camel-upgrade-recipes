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

public class RenameHeaderInYamlDslTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameHeaderInYamlDsl("kafka.TOPIC", "CamelKafkaTopic"));
    }

    @DocumentExample
    @Test
    void setHeaderMigration() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: kafka.TOPIC
                          constant: my-topic
                """,
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: CamelKafkaTopic
                          constant: my-topic
                """
            )
        );
    }

    @Test
    void headerPredicateMigration() {
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
                                name: kafka.TOPIC
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
                                name: CamelKafkaTopic
                              steps:
                                - to: "mock:found"
                """
            )
        );
    }

    @Test
    void removeHeaderMigration() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - removeHeader:
                          name: kafka.TOPIC
                      - to: "mock:result"
                """,
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - removeHeader:
                          name: CamelKafkaTopic
                      - to: "mock:result"
                """
            )
        );
    }

    @Test
    void multipleHeadersMigration() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: kafka.TOPIC
                          constant: topic1
                      - setHeader:
                          name: other.header
                          constant: value
                      - removeHeader:
                          name: kafka.TOPIC
                """,
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: CamelKafkaTopic
                          constant: topic1
                      - setHeader:
                          name: other.header
                          constant: value
                      - removeHeader:
                          name: CamelKafkaTopic
                """
            )
        );
    }

    @Test
    void setHeaderWithSimpleExpression() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: kafka.TOPIC
                          simple: "${header.myHeader}"
                """,
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: CamelKafkaTopic
                          simple: "${header.myHeader}"
                """
            )
        );
    }

    @Test
    void doesNotMigrateDifferentHeader() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "direct:start"
                    steps:
                      - setHeader:
                          name: kafka.PARTITION
                          constant: 0
                """
                // No change expected - different header name
            )
        );
    }

    @Test
    void doesNotMigrateNonHeaderName() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: "kafka:kafka.TOPIC"
                    steps:
                      - to: "mock:result"
                """
                // No change expected - "kafka.TOPIC" is URI, not a header name
            )
        );
    }

    @Test
    void doesNotMigrateNonCamelYaml() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                app:
                  headers:
                    kafka.TOPIC: my-topic
                """
                // No change expected - not Camel YAML DSL
            )
        );
    }
}
