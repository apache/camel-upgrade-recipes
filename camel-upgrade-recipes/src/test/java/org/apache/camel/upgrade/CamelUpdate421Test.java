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

import org.apache.camel.upgrade.camel421.RenameHeaders;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate421Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        // Note: We load the full 4.21 recipe here, but each component recipe has
        // ModuleHasDependency preconditions that ensure only relevant recipes run
        // based on which dependencies are present in the test's pom.xml
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_21)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_20,
                        "camel-core-model", "camel-api"))
                .typeValidationOptions(TypeValidation.none())
                // Explicitly set expected cycles to 1 to prevent other recipes from running
                .expectedCyclesThatMakeChanges(1);
    }

    @DocumentExample
    @Test
    void testKafkaHeadersMigration() {
        // Test camel-kafka header migration in Java
        //language=java
        rewriteRun(
                mavenProject("test-kafka",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-kafka", CamelTestUtil.CamelVersion.v4_20)),
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
    void testJGroupsHeadersMigration() {
        // Test camel-jgroups header migration in Java
        //language=java
        rewriteRun(
                mavenProject("test-jgroups",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-jgroups", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("JGROUPS_CHANNEL_ADDRESS", "addr1");
                                        exchange.getIn().setHeader("JGROUPS_DEST", "destination");
                                    })
                                    .to("jgroups:clusterName");
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
                                        exchange.getIn().setHeader("CamelJGroupsChannelAddress", "addr1");
                                        exchange.getIn().setHeader("CamelJGroupsDest", "destination");
                                    })
                                    .to("jgroups:clusterName");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testDnsHeadersMigrationJava() {
        // Test camel-dns header migration in Java - Message API and Simple expressions
        //language=java
        rewriteRun(
                mavenProject("test-dns",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-dns", CamelTestUtil.CamelVersion.v4_20)),
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
                                    })
                                    .setBody(simple("${header.dns.name}"))
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
                                    })
                                    .setBody(simple("${header.CamelDnsName}"))
                                    .to("dns:lookup");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testJiraHeadersMigrationJava() {
        // Test camel-jira header migration in Java
        //language=java
        rewriteRun(
                mavenProject("test-jira",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-jira", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("IssueKey", "CAMEL-12345");
                                        exchange.getIn().setHeader("IssueSummary", "Bug fix");
                                        exchange.getIn().setHeader("ProjectKey", "CAMEL");
                                    })
                                    .to("jira:addIssue");
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
                                        exchange.getIn().setHeader("CamelJiraIssueKey", "CAMEL-12345");
                                        exchange.getIn().setHeader("CamelJiraIssueSummary", "Bug fix");
                                        exchange.getIn().setHeader("CamelJiraIssueProjectKey", "CAMEL");
                                    })
                                    .to("jira:addIssue");
                            }
                        }
                        """
                )
                )
        );
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
        //language=java
        rewriteRun(
                mavenProject("test-jgroups-raft",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-jgroups-raft", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("JGROUPSRAFT_SET_OFFSET", 10);
                                        exchange.getIn().setHeader("JGROUPSRAFT_SET_TIMEOUT", 5000);
                                    })
                                    .to("jgroups-raft:cluster");
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
                                        exchange.getIn().setHeader("CamelJGroupsRaftSetOffset", 10);
                                        exchange.getIn().setHeader("CamelJGroupsRaftSetTimeout", 5000);
                                    })
                                    .to("jgroups-raft:cluster");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testShiroHeadersMigration() {
        //language=java
        rewriteRun(
                mavenProject("test-shiro",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-shiro", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("SHIRO_SECURITY_USERNAME", "admin");
                                        exchange.getIn().setHeader("SHIRO_SECURITY_PASSWORD", "secret");
                                    })
                                    .to("shiro:login");
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
                                        exchange.getIn().setHeader("CamelShiroSecurityUsername", "admin");
                                        exchange.getIn().setHeader("CamelShiroSecurityPassword", "secret");
                                    })
                                    .to("shiro:login");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testSolrHeadersMigration() {
        // Test camel-solr header migration in Java
        //language=java
        rewriteRun(
                mavenProject("test-solr",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-solr", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("SolrOperation", "INSERT");
                                        exchange.getIn().setHeader("SolrCollection", "myCollection");
                                        exchange.getIn().setHeader("SolrQueryString", "id:123");
                                        exchange.getIn().setHeader("SolrField.id", "doc123");
                                        exchange.getIn().setHeader("SolrParam.commit", true);
                                    })
                                    .to("solr:http://localhost:8983/solr");
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
                                        exchange.getIn().setHeader("CamelSolrOperation", "INSERT");
                                        exchange.getIn().setHeader("CamelSolrCollection", "myCollection");
                                        exchange.getIn().setHeader("CamelSolrQueryString", "id:123");
                                        exchange.getIn().setHeader("CamelSolrField.id", "doc123");
                                        exchange.getIn().setHeader("CamelSolrParam.commit", true);
                                    })
                                    .to("solr:http://localhost:8983/solr");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testElasticsearchRestClientHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-elasticsearch",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-elasticsearch-rest-client", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("SEARCH_QUERY", "test");
                                    })
                                    .to("elasticsearch-rest-client:cluster");
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
                                        exchange.getIn().setHeader("CamelElasticsearchSearchQuery", "test");
                                    })
                                    .to("elasticsearch-rest-client:cluster");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testGitHub2HeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-github",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-github", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("GitHubIssueTitle", "Bug Report");
                                    })
                                    .to("github2:pullRequests");
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
                                        exchange.getIn().setHeader("CamelGitHubIssueTitle", "Bug Report");
                                    })
                                    .to("github2:pullRequests");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testGoogleCloudHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-google-cloud",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-google-functions", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("GoogleCloudFunctionsOperation", "createFunction");
                                    })
                                    .to("google-functions:project");
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
                                        exchange.getIn().setHeader("CamelGoogleCloudFunctionsOperation", "createFunction");
                                    })
                                    .to("google-functions:project");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testMongoDbGridFsHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-mongodb",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-mongodb-gridfs", CamelTestUtil.CamelVersion.v4_20)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("gridfs.operation", "create");
                                    })
                                    .to("mongodb-gridfs:mydb");
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
                                        exchange.getIn().setHeader("CamelGridFsOperation", "create");
                                    })
                                    .to("mongodb-gridfs:mydb");
                            }
                        }
                        """
                )
                )
        );
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

}
