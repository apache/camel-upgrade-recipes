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
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

public class CamelUpdate420Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_20)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_19,
                        "camel-core-model", "camel-api", "camel-qdrant", "camel-tahu", "tahu-host"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_20.html#_camel_pulsar">camel-pulsar</a>
     * Tests all Java DSL transformations
     */
    @DocumentExample
    @Test
    void pulsarJavaDsl() {
        //language=java
        rewriteRun(
                // V1 persistent to V2
                java(
                        """
                        import org.apache.camel.builder.RouteBuilder;

                        public class MyRoute extends RouteBuilder {
                            @Override
                            public void configure() {
                                // V1 persistent to V2 (simple topic name)
                                from("pulsar:persistent://public/cluster1/default/my-topic")
                                    .to("mock:result");
                                // V1 non-persistent to V2
                                from("pulsar:non-persistent://tenant1/cluster2/namespace1/topic1")
                                    .to("mock:result");
                                // V1 with query parameters
                                from("pulsar:persistent://public/cluster1/default/my-topic?numberOfConsumers=5&subscriptionType=Shared")
                                    .to("mock:result");
                                // Topic with slashes - NOT transformed (ambiguous if V1 or V2)
                                from("pulsar:persistent://public/cluster1/default/my-topic/sub-path")
                                    .to("mock:result");
                                // Topic with slashes and params - NOT transformed
                                from("pulsar:persistent://tenant/cluster/ns/topic/path/more?subscriptionName=sub1")
                                    .to("mock:result");
                                // V2 format - unchanged
                                from("pulsar:persistent://public/default/my-topic")
                                    .to("mock:result");
                                // Non-pulsar component - unchanged
                                from("other-component:my-topic")
                                    .to("mock:result");
                            }
                        }
                        """,
                        """
                        import org.apache.camel.builder.RouteBuilder;

                        public class MyRoute extends RouteBuilder {
                            @Override
                            public void configure() {
                                // V1 persistent to V2 (simple topic name)
                                from("pulsar:persistent://public/default/my-topic")
                                    .to("mock:result");
                                // V1 non-persistent to V2
                                from("pulsar:non-persistent://tenant1/namespace1/topic1")
                                    .to("mock:result");
                                // V1 with query parameters
                                from("pulsar:persistent://public/default/my-topic?numberOfConsumers=5&subscriptionType=Shared")
                                    .to("mock:result");
                                // Topic with slashes - NOT transformed (ambiguous if V1 or V2)
                                from("pulsar:persistent://public/cluster1/default/my-topic/sub-path")
                                    .to("mock:result");
                                // Topic with slashes and params - NOT transformed
                                from("pulsar:persistent://tenant/cluster/ns/topic/path/more?subscriptionName=sub1")
                                    .to("mock:result");
                                // V2 format - unchanged
                                from("pulsar:persistent://public/default/my-topic")
                                    .to("mock:result");
                                // Non-pulsar component - unchanged
                                from("other-component:my-topic")
                                    .to("mock:result");
                            }
                        }
                        """
                )
        );
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_20.html#_camel_pulsar">camel-pulsar</a>
     * Tests all XML DSL transformations
     */
    @Test
    void pulsarXmlDsl() {
        //language=xml
        rewriteRun(
                xml(
                        """
                        <routes xmlns="http://camel.apache.org/schema/spring">
                            <!-- V1 to V2 transformation -->
                            <route id="pulsarRoute">
                                <from uri="pulsar:persistent://public/cluster1/default/my-topic"/>
                                <to uri="pulsar:non-persistent://tenant/cluster2/ns/topic1?subscriptionName=sub1"/>
                                <to uri="mock:result"/>
                            </route>
                            <!-- Topic with slashes - NOT transformed (ambiguous) -->
                            <route>
                                <from uri="pulsar:persistent://tenant/cluster/namespace/topic/with/slashes?param=value"/>
                            </route>
                        </routes>
                        """,
                        """
                        <routes xmlns="http://camel.apache.org/schema/spring">
                            <!-- V1 to V2 transformation -->
                            <route id="pulsarRoute">
                                <from uri="pulsar:persistent://public/default/my-topic"/>
                                <to uri="pulsar:non-persistent://tenant/ns/topic1?subscriptionName=sub1"/>
                                <to uri="mock:result"/>
                            </route>
                            <!-- Topic with slashes - NOT transformed (ambiguous) -->
                            <route>
                                <from uri="pulsar:persistent://tenant/cluster/namespace/topic/with/slashes?param=value"/>
                            </route>
                        </routes>
                        """
                )
        );
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_20.html#_camel_pulsar">camel-pulsar</a>
     * Tests all YAML DSL transformations
     */
    @Test
    void pulsarYamlDsl() {
        //language=yaml
        rewriteRun(
                // V1 to V2 transformation
                yaml(
                        """
                        - route:
                            id: pulsarRoute
                            from:
                              uri: pulsar:persistent://public/cluster1/default/my-topic
                              steps:
                                - to:
                                    uri: pulsar:non-persistent://tenant/cluster2/ns/topic1?subscriptionName=sub1
                                - to:
                                    uri: mock:result
                        """,
                        """
                        - route:
                            id: pulsarRoute
                            from:
                              uri: pulsar:persistent://public/default/my-topic
                              steps:
                                - to:
                                    uri: pulsar:non-persistent://tenant/ns/topic1?subscriptionName=sub1
                                - to:
                                    uri: mock:result
                        """
                ),
                // Topic with slashes - NOT transformed (ambiguous)
                yaml(
                        """
                        - route:
                            from:
                              uri: pulsar:persistent://tenant/cluster/namespace/topic/with/slashes
                        """
                )
        );
    }
}
