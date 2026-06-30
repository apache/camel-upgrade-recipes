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

import java.util.Map;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Tests for CXF header constant renames (operationName → CamelCxfOperationName, etc.)
 */
public class CamelCxfHeadersTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameHeaders(Map.of(
                    "operationName", "CamelCxfOperationName",
                    "operationNamespace", "CamelCxfOperationNamespace"
                )))
            .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_20,
                    "camel-core-model", "camel-api"))
            .typeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void testJavaMethodRename() {
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
                                exchange.getIn().setHeader("operationName", "myOperation");
                                exchange.getIn().setHeader("operationNamespace", "http://example.com");
                                String op = exchange.getIn().getHeader("operationName", String.class);
                            })
                            .to("cxf:bean:serviceEndpoint");
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
                                exchange.getIn().setHeader("CamelCxfOperationName", "myOperation");
                                exchange.getIn().setHeader("CamelCxfOperationNamespace", "http://example.com");
                                String op = exchange.getIn().getHeader("CamelCxfOperationName", String.class);
                            })
                            .to("cxf:bean:serviceEndpoint");
                    }
                }
                """
            )
        );
    }

    @Test
    void testSimpleExpressionRename() {
        //language=java
        rewriteRun(
            java(
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("Operation: ${header.operationName}, Namespace: ${header.operationNamespace}"))
                            .to("cxf:bean:serviceEndpoint");
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("Operation: ${header.CamelCxfOperationName}, Namespace: ${header.CamelCxfOperationNamespace}"))
                            .to("cxf:bean:serviceEndpoint");
                    }
                }
                """
            )
        );
    }

    @Test
    void testXmlDslRename() {
        //language=xml
        rewriteRun(
            xml(
                """
                <routes xmlns="http://camel.apache.org/schema/spring">
                    <route id="cxfRoute">
                        <from uri="direct:start"/>
                        <setHeader name="operationName">
                            <constant>myOperation</constant>
                        </setHeader>
                        <setHeader name="operationNamespace">
                            <constant>http://example.com</constant>
                        </setHeader>
                        <to uri="cxf:bean:serviceEndpoint"/>
                    </route>
                </routes>
                """,
                """
                <routes xmlns="http://camel.apache.org/schema/spring">
                    <route id="cxfRoute">
                        <from uri="direct:start"/>
                        <setHeader name="CamelCxfOperationName">
                            <constant>myOperation</constant>
                        </setHeader>
                        <setHeader name="CamelCxfOperationNamespace">
                            <constant>http://example.com</constant>
                        </setHeader>
                        <to uri="cxf:bean:serviceEndpoint"/>
                    </route>
                </routes>
                """
            )
        );
    }

    @Test
    void testYamlDslRename() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    id: cxfRoute
                    from:
                      uri: direct:start
                      steps:
                        - setHeader:
                            name: operationName
                            constant: myOperation
                        - setHeader:
                            name: operationNamespace
                            constant: http://example.com
                        - to:
                            uri: cxf:bean:serviceEndpoint
                """,
                """
                - route:
                    id: cxfRoute
                    from:
                      uri: direct:start
                      steps:
                        - setHeader:
                            name: CamelCxfOperationName
                            constant: myOperation
                        - setHeader:
                            name: CamelCxfOperationNamespace
                            constant: http://example.com
                        - to:
                            uri: cxf:bean:serviceEndpoint
                """
            )
        );
    }
}
