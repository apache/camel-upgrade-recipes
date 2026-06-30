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
 * Tests for Elasticsearch REST client header constant renames
 */
public class CamelElasticsearchHeadersTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameHeaders(Map.of(
                    "ID", "CamelElasticsearchId",
                    "SEARCH_QUERY", "CamelElasticsearchSearchQuery",
                    "INDEX_SETTINGS", "CamelElasticsearchIndexSettings",
                    "INDEX_NAME", "CamelElasticsearchIndexName",
                    "OPERATION", "CamelElasticsearchOperation"
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
                                exchange.getIn().setHeader("ID", "doc123");
                                exchange.getIn().setHeader("SEARCH_QUERY", "name:John");
                                exchange.getIn().setHeader("INDEX_SETTINGS", "{}");
                                exchange.getIn().setHeader("INDEX_NAME", "users");
                                exchange.getIn().setHeader("OPERATION", "INDEX");
                                String id = exchange.getIn().getHeader("ID", String.class);
                            })
                            .to("elasticsearch-rest-client:clusterName");
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
                                exchange.getIn().setHeader("CamelElasticsearchId", "doc123");
                                exchange.getIn().setHeader("CamelElasticsearchSearchQuery", "name:John");
                                exchange.getIn().setHeader("CamelElasticsearchIndexSettings", "{}");
                                exchange.getIn().setHeader("CamelElasticsearchIndexName", "users");
                                exchange.getIn().setHeader("CamelElasticsearchOperation", "INDEX");
                                String id = exchange.getIn().getHeader("CamelElasticsearchId", String.class);
                            })
                            .to("elasticsearch-rest-client:clusterName");
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
                            .setBody(simple("ID: ${header.ID}, Query: ${header.SEARCH_QUERY}, Index: ${header.INDEX_NAME}"))
                            .to("elasticsearch-rest-client:clusterName");
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start")
                            .setBody(simple("ID: ${header.CamelElasticsearchId}, Query: ${header.CamelElasticsearchSearchQuery}, Index: ${header.CamelElasticsearchIndexName}"))
                            .to("elasticsearch-rest-client:clusterName");
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
                    <route id="elasticsearchRoute">
                        <from uri="direct:start"/>
                        <setHeader name="ID">
                            <constant>doc123</constant>
                        </setHeader>
                        <setHeader name="SEARCH_QUERY">
                            <constant>name:John</constant>
                        </setHeader>
                        <setHeader name="INDEX_NAME">
                            <constant>users</constant>
                        </setHeader>
                        <setHeader name="OPERATION">
                            <constant>INDEX</constant>
                        </setHeader>
                        <to uri="elasticsearch-rest-client:clusterName"/>
                    </route>
                </routes>
                """,
                """
                <routes xmlns="http://camel.apache.org/schema/spring">
                    <route id="elasticsearchRoute">
                        <from uri="direct:start"/>
                        <setHeader name="CamelElasticsearchId">
                            <constant>doc123</constant>
                        </setHeader>
                        <setHeader name="CamelElasticsearchSearchQuery">
                            <constant>name:John</constant>
                        </setHeader>
                        <setHeader name="CamelElasticsearchIndexName">
                            <constant>users</constant>
                        </setHeader>
                        <setHeader name="CamelElasticsearchOperation">
                            <constant>INDEX</constant>
                        </setHeader>
                        <to uri="elasticsearch-rest-client:clusterName"/>
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
                    id: elasticsearchRoute
                    from:
                      uri: direct:start
                      steps:
                        - setHeader:
                            name: ID
                            constant: doc123
                        - setHeader:
                            name: SEARCH_QUERY
                            constant: name:John
                        - setHeader:
                            name: INDEX_NAME
                            constant: users
                        - setHeader:
                            name: OPERATION
                            constant: INDEX
                        - to:
                            uri: elasticsearch-rest-client:clusterName
                """,
                """
                - route:
                    id: elasticsearchRoute
                    from:
                      uri: direct:start
                      steps:
                        - setHeader:
                            name: CamelElasticsearchId
                            constant: doc123
                        - setHeader:
                            name: CamelElasticsearchSearchQuery
                            constant: name:John
                        - setHeader:
                            name: CamelElasticsearchIndexName
                            constant: users
                        - setHeader:
                            name: CamelElasticsearchOperation
                            constant: INDEX
                        - to:
                            uri: elasticsearch-rest-client:clusterName
                """
            )
        );
    }
}
