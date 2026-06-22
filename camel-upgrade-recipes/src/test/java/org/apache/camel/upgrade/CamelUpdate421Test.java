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

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate421Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        // Note: We load the full 4.21 recipe here, but each component recipe has
        // ModuleHasDependency preconditions that ensure only relevant recipes run
        // based on which dependencies are present in the test's pom.xml
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_21)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_20,
                        "camel-core-model", "camel-api", "s3"))
                .typeValidationOptions(TypeValidation.none())
                // Explicitly set expected cycles to 1 to prevent other recipes from running
                .expectedCyclesThatMakeChanges(1);
    }

    @DocumentExample
    @Test
    void testKafkaHeadersMigration() {
        new CamelUpdate418Test().testKafkaHeadersMigration();
    }

    @Test
    void testJGroupsHeadersMigration() {
        new CamelUpdate418_3Test().testJGroupsHeadersMigration();
    }

    @Test
    void testDnsHeadersMigrationJava() {
        new CamelUpdate418Test().testDnsHeadersMigrationJava();
    }

    @Test
    void testJiraHeadersMigrationJava() {
        new CamelUpdate418_3Test().testJiraHeadersMigrationJava();
    }

    @Test
    void testOpenstackHeadersMigrationJava() {
        new CamelUpdate418_3Test().testOpenstackHeadersMigrationJava();
    }

    @Test
    void testWeb3jHeadersMigrationJava() {
        new CamelUpdate418_3Test().testWeb3jHeadersMigrationJava();
    }

    @Test
    void testCouchdbHeadersMigration() {
        //language=java
        rewriteRun(
                mavenProject("test-couchdb",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-couchdb", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("CouchDbDatabase", "mydb");
                                        exchange.getIn().setHeader("CouchDbId", "doc1");
                                    })
                                    .to("couchdb:http://localhost:5984");
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
                                        exchange.getIn().setHeader("CamelCouchDbDatabase", "mydb");
                                        exchange.getIn().setHeader("CamelCouchDbId", "doc1");
                                    })
                                    .to("couchdb:http://localhost:5984");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testCouchbaseHeadersMigration() {
        //language=java
        rewriteRun(
                mavenProject("test-couchbase",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-couchbase", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("CCB_KEY", "key1");
                                        exchange.getIn().setHeader("CCB_TTL", 3600);
                                    })
                                    .to("couchbase:http://localhost");
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
                                        exchange.getIn().setHeader("CamelCouchbaseKey", "key1");
                                        exchange.getIn().setHeader("CamelCouchbaseTtl", 3600);
                                    })
                                    .to("couchbase:http://localhost");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testJGroupsRaftHeadersMigration() {
        new CamelUpdate418_3Test().testJGroupsRaftHeadersMigration();
    }

    @Test
    void testShiroHeadersMigration() {
        new CamelUpdate418_3Test().testShiroHeadersMigration();
    }

    @Test
    void testSolrHeadersMigration() {
        new CamelUpdate418_3Test().testSolrHeadersMigration();
    }

    @Test
    void testElasticsearchRestClientHeadersMigrationJava() {
        new CamelUpdate418_3Test().testElasticsearchRestClientHeadersMigrationJava();
    }

    @Test
    void testGitHub2HeadersMigrationJava() {
        new CamelUpdate418_3Test().testGitHub2HeadersMigrationJava();
    }

    @Test
    void testGoogleCloudHeadersMigrationJava() {
        new CamelUpdate418_3Test().testGoogleCloudHeadersMigrationJava();
    }

    @Test
    void testMongoDbGridFsHeadersMigrationJava() {
        new CamelUpdate418_3Test().testMongoDbGridFsHeadersMigrationJava();
    }

    @Test
    void testIrcHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-irc",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-irc", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("irc.sendTo", "#channel");
                                    })
                                    .to("irc:bot@irc.server.org");
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
                                        exchange.getIn().setHeader("CamelIrcSendTo", "#channel");
                                    })
                                    .to("irc:bot@irc.server.org");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testPreconditionBlocksWithoutDependency() {
        // Test that the Kafka recipe does NOT run when camel-kafka dependency is absent
        // This verifies the ModuleHasDependency precondition works correctly
        // Using camel-core instead of camel-kafka means the precondition will block the recipe
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(0),
                mavenProject("test-negative",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-core", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("kafka.TOPIC", "topic1");
                                    });
                            }
                        }
                        """
                )
        ));
    }

    @Test
    void testRemoveReifierStrategyImport() {
        //language=java
        rewriteRun(
                java(
                """
                import org.apache.camel.builder.RouteBuilder;
                import org.apache.camel.spi.ReifierStrategy;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start").to("log:test");
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start").to("log:test");
                    }
                }
                """
                )
        );
    }

    @Test
    void testRemoveZooWordEmbeddingPredictorImport() {
        //language=java
        rewriteRun(
                java(
                """
                import org.apache.camel.builder.RouteBuilder;
                import org.apache.camel.component.djl.model.nlp.ZooWordEmbeddingPredictor;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start").to("log:test");
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                class Test extends RouteBuilder {
                    public void configure() {
                        from("direct:start").to("log:test");
                    }
                }
                """
                )
        );
    }

    @Test
    void testLuceneHeadersMigrationJava() {
        new CamelUpdate418_3Test().testLuceneHeadersMigrationJava();
    }






    @Test
    void testElasticsearchHeadersMigrationJava() {
        new CamelUpdate418_3Test().testElasticsearchHeadersMigrationJava();
    }


    @Test
    void testRemoveCamelStompDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.camel</groupId>
                            <artifactId>camel-stomp</artifactId>
                            <version>4.20.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """,
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                </project>
                """
                )
        );
    }

    @Test
    void testRemoveCamelAwsXrayDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.camel</groupId>
                            <artifactId>camel-aws-xray</artifactId>
                            <version>4.20.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """,
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                </project>
                """
                )
        );
    }

    @Test
    void testRemoveCamelGuavaEventbusDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.camel</groupId>
                            <artifactId>camel-guava-eventbus</artifactId>
                            <version>4.20.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """,
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                </project>
                """
                )
        );
    }

    @Test
    void testRemoveCamelGrapeDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.camel</groupId>
                            <artifactId>camel-grape</artifactId>
                            <version>4.20.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """,
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                </project>
                """
                )
        );
    }

    @Test
    void testRemoveCamelElytronDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.camel</groupId>
                            <artifactId>camel-elytron</artifactId>
                            <version>4.20.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """,
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                </project>
                """
                )
        );
    }

    @Test
    void testMigrateGrokDependency() {
        //language=xml
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(1),
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <dependency>
                            <groupId>io.krakens</groupId>
                            <artifactId>java-grok</artifactId>
                            <version>0.1.9</version>
                        </dependency>
                    </dependencies>
                </project>
                """,
                spec -> spec.after(after -> {
                    assertThat(after).contains("<groupId>io.github.whatap</groupId>");
                    assertThat(after).contains("<artifactId>java-grok</artifactId>");
                    assertThat(after).doesNotContain("<groupId>io.krakens</groupId>");
                    return after;
                })
                )
        );
    }

    @Test
    void testMigrateAws2S3ListObjectsApi() {
        //language=java
        rewriteRun(
                mavenProject("test-aws2-s3",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-aws2-s3", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
                        import software.amazon.awssdk.services.s3.model.ListObjectsResponse;

                        public class S3Example {
                            public void example() {
                                ListObjectsRequest request = ListObjectsRequest.builder().build();
                                ListObjectsResponse response = null;
                            }
                        }
                        """,
                        """
                        import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
                        import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

                        public class S3Example {
                            public void example() {
                                ListObjectsV2Request request = ListObjectsV2Request.builder().build();
                                ListObjectsV2Response response = null;
                            }
                        }
                        """
                        )
                )
        );
    }

    @Test
    void testRemoveCamelGithubDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.camel</groupId>
                            <artifactId>camel-github</artifactId>
                            <version>4.20.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """,
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                </project>
                """
                )
        );
    }

    @Test
    void testErrorRegistryPropertiesFile() {
        //language=properties
        rewriteRun(
                properties(
                """
                camel.main.errorRegistryEnabled=true
                camel.main.errorRegistryMaximumEntries=100
                camel.main.errorRegistryTimeToLiveSeconds=300
                camel.main.errorRegistryStackTraceEnabled=false
                """,
                """
                camel.errorRegistry.enabled=true
                camel.errorRegistry.maximumEntries=100
                camel.errorRegistry.timeToLiveSeconds=300
                """,
                spec -> spec.path("application.properties")
                )
        );
    }

}
