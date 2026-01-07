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
import static org.openrewrite.yaml.Assertions.yaml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate417Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_17)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_16,
                        "camel-core-model", "camel-api"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_17.html#_camel_core">Camel-core transform EIP Yaml DSL</a>
     */
    @DocumentExample
    @Test
    void transformEipYaml() {
        //language=yaml
        rewriteRun(yaml(
                """
                        - route:
                            id: timer-transform-route
                            from:
                              uri: "timer:exampleTimer"
                              parameters:
                                period: 100   # runs every 5 seconds
                              steps:
                                - setBody:
                                    simple: "World"
                                - log:
                                    message: "Original body: ${body}"
                                - transform:
                                    toType: application-octet-stream
                                - log:
                                    message: "Transformed body: ${body}"
                  """,
                """
                        - route:
                            id: timer-transform-route
                            from:
                              uri: "timer:exampleTimer"
                              parameters:
                                period: 100   # runs every 5 seconds
                              steps:
                                - setBody:
                                    simple: "World"
                                - log:
                                    message: "Original body: ${body}"
                                - transformDataType:
                                    toType: application-octet-stream
                                - log:
                                    message: "Transformed body: ${body}"
                  """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_17.html#_camel_core">Camel-core transform EIP Yaml DSL</a>
     */
    @Test
    void transformEipYamlNotDataType() {
        //language=yaml
        rewriteRun(yaml(
                """
                        - route:
                            id: timer-transform-route
                            from:
                              uri: "timer:exampleTimer"
                              parameters:
                                period: 100   # runs every 5 seconds
                              steps:
                                - setBody:
                                    simple: "World"
                                - log:
                                    message: "Original body: ${body}"
                                - transform:
                                    constant: Constant
                                - log:
                                    message: "Transformed body: ${body}"
                  """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_17.html#_camel_core">Camel-core transform EIP</a>
     */
    @Test
    void transformEip() {
        //language=java
        rewriteRun(java(
                """
                import org.apache.camel.builder.RouteBuilder;
                import org.apache.camel.spi.DataType;
    
                public class TransformRoute extends RouteBuilder {
    
                    @Override
                    public void configure() {
                        from("direct:start")
                            .transform(new DataType("myDataType"))
                            .to("mock:result");
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;
                import org.apache.camel.spi.DataType;
    
                public class TransformRoute extends RouteBuilder {
    
                    @Override
                    public void configure() {
                        from("direct:start")
                            .transformDataType(new DataType("myDataType"))
                            .to("mock:result");
                    }
                }
                """));
    }

}
