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

import java.util.Map;

import static org.openrewrite.java.Assertions.java;

/**
 * Tests for the bulk RenameHeaders recipe that renames multiple headers at once using a map.
 */
public class RenameHeadersTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameHeaders(Map.of(
                    "kafka.TOPIC", "CamelKafkaTopic",
                    "kafka.PARTITION", "CamelKafkaPartition",
                    "kafka.KEY", "CamelKafkaKey"
                )))
            .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_20,
                    "camel-core-model", "camel-api"))
            .typeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void bulkHeaderRename() {
        // Demonstrates that a single RenameHeaders recipe with a map can rename multiple headers
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.Exchange;
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .process(exchange -> {
                                exchange.getIn().setHeader("kafka.TOPIC", "topic1");
                                exchange.getIn().setHeader("kafka.PARTITION", "0");
                                exchange.getIn().setHeader("kafka.KEY", "key1");
                            })
                            .setBody(simple("Topic: ${header.kafka.TOPIC}, Partition: ${header.kafka.PARTITION}"));
                    }
                }
                """,
                """
                import org.apache.camel.Exchange;
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .process(exchange -> {
                                exchange.getIn().setHeader("CamelKafkaTopic", "topic1");
                                exchange.getIn().setHeader("CamelKafkaPartition", "0");
                                exchange.getIn().setHeader("CamelKafkaKey", "key1");
                            })
                            .setBody(simple("Topic: ${header.CamelKafkaTopic}, Partition: ${header.CamelKafkaPartition}"));
                    }
                }
                """
            )
        );
    }
}
