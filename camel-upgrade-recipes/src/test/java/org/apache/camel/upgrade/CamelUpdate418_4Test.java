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
 * Tests for migrating from Camel 4.18.3 to 4.18.4: the camel-atmosphere-websocket header renames, which
 * also ship in 4.22.0 and are referenced from the 4.22 chain.
 */
public class CamelUpdate418_4Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_18_4)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_18, "camel-api", "camel-core-model", "camel-support"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void migrateAtmosphereWebsocketHeaders() {
        //language=java
        rewriteRun(
                mavenProject("test-atmosphere-websocket",
                        CamelTestUtil.pomXmlSpec("camel-atmosphere-websocket", CamelTestUtil.CamelVersion.v4_18_3),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class WebsocketRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:next")
                                            .setHeader("websocket.connectionKey", header("myKey"))
                                            .to("atmosphere-websocket:///servicepath");
                                        from("direct:broadcast")
                                            .process(exchange -> {
                                                exchange.getIn().setHeader("websocket.connectionKey.list", "keys");
                                                exchange.getIn().setHeader("websocket.sendToAll", "true");
                                                exchange.getIn().setHeader("websocket.eventType", "1");
                                            })
                                            .to("atmosphere-websocket:///servicepath");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class WebsocketRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:next")
                                            .setHeader("CamelAtmosphereWebsocketConnectionKey", header("myKey"))
                                            .to("atmosphere-websocket:///servicepath");
                                        from("direct:broadcast")
                                            .process(exchange -> {
                                                exchange.getIn().setHeader("CamelAtmosphereWebsocketConnectionKeyList", "keys");
                                                exchange.getIn().setHeader("CamelAtmosphereWebsocketSendToAll", "true");
                                                exchange.getIn().setHeader("CamelAtmosphereWebsocketEventType", "1");
                                            })
                                            .to("atmosphere-websocket:///servicepath");
                                    }
                                }
                                """
                        )
                )
        );
    }

}
