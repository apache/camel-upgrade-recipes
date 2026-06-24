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
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate421Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        // Note: We load the full 4.21 recipe here, but each component recipe has
        // ModuleHasDependency preconditions that ensure only relevant recipes run
        // based on which dependencies are present in the test's pom.xml
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_21, true)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_20,
                        "camel-core-model", "camel-api", "s3"))
                .typeValidationOptions(TypeValidation.none())
                // Explicitly set expected cycles to 1 to prevent other recipes from running
                .expectedCyclesThatMakeChanges(1);
    }

    @Test
    void jGroupsHeadersMigration() {
        new CamelUpdate418_3Test().jGroupsHeadersMigration();
    }

    @Test
    void dnsHeadersMigrationJava() {
        new CamelUpdate418Test().dnsHeadersMigrationJava();
    }

    @Test
    void jiraHeadersMigrationJava() {
        new CamelUpdate418_3Test().jiraHeadersMigrationJava();
    }

    @Test
    void openstackHeadersMigrationJava() {
        new CamelUpdate418_3Test().openstackHeadersMigrationJava();
    }

    @Test
    void web3jHeadersMigrationJava() {
        new CamelUpdate418_3Test().web3jHeadersMigrationJava();
    }

    @Test
    void couchdbHeadersMigration() {
        //language=java
        rewriteRun(
                mavenProject("test-couchdb",
                        CamelTestUtil.pomXmlSpec("camel-couchdb", CamelTestUtil.CamelVersion.v4_20),
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
    void couchbaseHeadersMigration() {
        //language=java
        rewriteRun(
                mavenProject("test-couchbase",
                        CamelTestUtil.pomXmlSpec("camel-couchbase", CamelTestUtil.CamelVersion.v4_20),
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
    void jGroupsRaftHeadersMigration() {
        new CamelUpdate418_3Test().jGroupsRaftHeadersMigration();
    }

    @Test
    void shiroHeadersMigration() {
        new CamelUpdate418_3Test().shiroHeadersMigration();
    }

    @Test
    void solrHeadersMigration() {
        new CamelUpdate418_3Test().solrHeadersMigration();
    }

    @Test
    void gitHub2HeadersMigrationJava() {
        new CamelUpdate418_3Test().gitHub2HeadersMigrationJava();
    }

    @Test
    void googleCloudHeadersMigrationJava() {
        new CamelUpdate418_3Test().googleCloudHeadersMigrationJava();
    }

    //todo more google - vision, text-to=speech, speech-to-text

    @Test
    void mongoDbGridFsHeadersMigrationJava() {
        new CamelUpdate418_3Test().mongoDbGridFsHeadersMigrationJava();
    }

    @Test
    void ircHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-irc",
                        CamelTestUtil.pomXmlSpec("camel-irc", CamelTestUtil.CamelVersion.v4_20),
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
    void preconditionBlocksWithoutDependency() {
        // Test that the Kafka recipe does NOT run when camel-kafka dependency is absent
        // This verifies the ModuleHasDependency precondition works correctly
        // Using camel-core instead of camel-kafka means the precondition will block the recipe
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(CamelTestUtil.isRecipeOverridden() ? 1 : 0),
                mavenProject("test-negative",
                        CamelTestUtil.pomXmlSpec("camel-core", CamelTestUtil.CamelVersion.v4_20),
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
    void luceneHeadersMigrationJava() {
        new CamelUpdate418_3Test().luceneHeadersMigrationJava();
    }

    @Test
    void removeCamelStompDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
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
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
                </project>
                """
                )
        );
    }

    @Test
    void removeCamelAwsXrayDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
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
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
                </project>
                """
                )
        );
    }

    @Test
    void removeCamelGuavaEventbusDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
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
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
                </project>
                """
                )
        );
    }

    @Test
    void removeCamelGrapeDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
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
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
                </project>
                """
                )
        );
    }

    @Test
    void removeCamelElytronDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>                    
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
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>                    
                </project>
                """
                )
        );
    }

    @Test
    void migrateAws2S3ListObjectsApi() {
        //language=java
        rewriteRun(
                mavenProject("test-aws2-s3",
                        CamelTestUtil.pomXmlSpec("camel-aws2-s3", CamelTestUtil.CamelVersion.v4_20),
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
    void removeCamelGithubDependency() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
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
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
                </project>
                """
                )
        );
    }

    @Test
    void errorRegistryPropertiesFile() {
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
