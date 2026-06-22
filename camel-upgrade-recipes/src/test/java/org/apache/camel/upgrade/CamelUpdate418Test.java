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
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate418Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_18)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_17,
                        "camel-core-model", "camel-api", "camel-support", "camel-qdrant", "camel-tahu", "tahu-host"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_18.html#_camel_qdrant">camel-qdrant changes</a>
     */
    @DocumentExample
    @Test
    void qdrantHeadersChange() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.component.qdrant.Qdrant.Headers;

                  public class QdrantTest {

                      public void test()  {
                          Headers headers = null;
                      }
                  }
                  """,
                """
                  import org.apache.camel.component.qdrant.QdrantHeaders;

                  public class QdrantTest {

                      public void test()  {
                          QdrantHeaders headers = null;
                      }
                  }
                  """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_18.html#_camel_tahu">camel-tahu changes</a>
     */
    @Test
    void tahuChange() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.component.tahu.handlers.TahuHostApplicationEventHandler;
                  import org.eclipse.tahu.host.api.HostApplicationEventHandler;
                  
                  public class TahuTest {
                  
                      public void test()  {
                          HostApplicationEventHandler h1 = null;
                          TahuHostApplicationEventHandler h2 = null;
                      }
                  }
                  """,
                """
                  import org.apache.camel.component.tahu.handlers.MultiTahuHostApplicationEventHandler;
                  import org.eclipse.tahu.host.api.MultiHostApplicationEventHandler;
                  
                  public class TahuTest {
                  
                      public void test()  {
                          MultiHostApplicationEventHandler h1 = null;
                          MultiTahuHostApplicationEventHandler h2 = null;
                      }
                  }
                  """));
    }

    @Test
    void testKafkaHeadersMigration() {
        //language=java
        rewriteRun(
                mavenProject("test-kafka",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-kafka", CamelTestUtil.CamelVersion.v4_17)),
                        java(
                                """
                                import org.apache.camel.Exchange;
                                import org.apache.camel.builder.RouteBuilder;
        
                                class Test extends RouteBuilder {
                                    public void configure() {
                                        from("direct:start")
                                            .process(exchange -> {
                                                exchange.getIn().setHeader("kafka.TOPIC", "topic1");
                                            })
                                            .setBody(simple("${header.kafka.TOPIC}"));
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
                                            })
                                            .setBody(simple("${header.CamelKafkaTopic}"));
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void testDnsHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-dns",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-dns", CamelTestUtil.CamelVersion.v4_17)),
                        java(
                                """
                                import org.apache.camel.Exchange;
                                import org.apache.camel.builder.RouteBuilder;
        
                                class Test extends RouteBuilder {
                                    public void configure() {
                                        from("direct:start")
                                            .process(exchange -> {
                                                exchange.getIn().setHeader("dns.name", "www.example.com");
                                                exchange.getIn().setHeader("dns.server", "8.8.8.8");
                                                exchange.getIn().setHeader("term", "example");
                                            })
                                            .setBody(simple("${header.term} ${header.dns.name}"))
                                            .to("dns:lookup");
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
                                                exchange.getIn().setHeader("CamelDnsName", "www.example.com");
                                                exchange.getIn().setHeader("CamelDnsServer", "8.8.8.8");
                                                exchange.getIn().setHeader("CamelDnsTerm", "example");
                                            })
                                            .setBody(simple("${header.CamelDnsTerm} ${header.CamelDnsName}"))
                                            .to("dns:lookup");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void testSalesforceHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-salesforce",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-salesforce", CamelTestUtil.CamelVersion.v4_17)),
                        java(
                                """
                                import org.apache.camel.Exchange;

                                class Test {
                                    void configure(Exchange exchange) {
                                        // Only Salesforce-specific headers are migrated
                                        exchange.getIn().setHeader("sObjectName", "Account");
                                        exchange.getIn().setHeader("sObjectQuery", "SELECT Id FROM Account");
                                        exchange.getIn().setHeader("sObjectId", "001xx000003DGYM");
                                        exchange.getIn().setHeader("apexMethod", "POST");
                                        exchange.getIn().setHeader("apexUrl", "/services/apexrest/MyService");
                                        exchange.getIn().setHeader("apexQueryParam.foo", "bar");
                                        exchange.getIn().setHeader("pkChunking", "true");
                                        // Generic headers like jobId, batchId, limit, contentType are NOT migrated
                                        exchange.getIn().setHeader("jobId", "job123");
                                        exchange.getIn().setHeader("limit", "100");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.Exchange;

                                class Test {
                                    void configure(Exchange exchange) {
                                        // Only Salesforce-specific headers are migrated
                                        exchange.getIn().setHeader("CamelSalesforceSObjectName", "Account");
                                        exchange.getIn().setHeader("CamelSalesforceSObjectQuery", "SELECT Id FROM Account");
                                        exchange.getIn().setHeader("CamelSalesforceSObjectId", "001xx000003DGYM");
                                        exchange.getIn().setHeader("CamelSalesforceApexMethod", "POST");
                                        exchange.getIn().setHeader("CamelSalesforceApexUrl", "/services/apexrest/MyService");
                                        exchange.getIn().setHeader("CamelSalesforceApexQueryParam.foo", "bar");
                                        exchange.getIn().setHeader("CamelSalesforcePkChunking", "true");
                                        // Generic headers like jobId, batchId, limit, contentType are NOT migrated
                                        exchange.getIn().setHeader("jobId", "job123");
                                        exchange.getIn().setHeader("limit", "100");
                                    }
                                }
                                """
                        )
                )
        );
    }
}
