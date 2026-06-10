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
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Tests for migrating from Camel 4.18.1 to 4.18.3.
 * Most changes are header renames that were introduced in 4.18.x and reused from 4.21 recipes.
 */
public class CamelUpdate418_3Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_18_3)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_18, "camel-api",
                        "camel-core-model", "camel-support"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testLuceneHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-lucene",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-lucene", CamelTestUtil.CamelVersion.v4_18)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("QUERY", "lucene query");
                                        exchange.getIn().setHeader("RETURN_LUCENE_DOCS", true);
                                    })
                                    .to("lucene:insert");
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
                                        exchange.getIn().setHeader("CamelLuceneQuery", "lucene query");
                                        exchange.getIn().setHeader("CamelLuceneReturnLuceneDocs", true);
                                    })
                                    .to("lucene:insert");
                            }
                        }
                        """
                )
                )
        );
    }
    @Test
    void testPdfHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-pdf",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-pdf", CamelTestUtil.CamelVersion.v4_18)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("pdf-document", "document");
                                        exchange.getIn().setHeader("protection-policy", "policy");
                                        exchange.getIn().setHeader("decryption-material", "material");
                                        exchange.getIn().setHeader("files-to-merge", "files");
                                    })
                                    .to("pdf:create");
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
                                        exchange.getIn().setHeader("CamelPdfDocument", "document");
                                        exchange.getIn().setHeader("CamelPdfProtectionPolicy", "policy");
                                        exchange.getIn().setHeader("CamelPdfDecryptionMaterial", "material");
                                        exchange.getIn().setHeader("CamelPdfFilesToMerge", "files");
                                    })
                                    .to("pdf:create");
                            }
                        }
                        """
                )
                )
        );
    }
    @Test
    void testArangoDbHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-arangodb",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-arangodb", CamelTestUtil.CamelVersion.v4_18)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("key", "myKey");
                                        exchange.getIn().setHeader("ResultClassType", String.class);
                                    })
                                    .to("arangodb:myDb");
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
                                        exchange.getIn().setHeader("CamelArangoDbKey", "myKey");
                                        exchange.getIn().setHeader("CamelArangoDbResultClassType", String.class);
                                    })
                                    .to("arangodb:myDb");
                            }
                        }
                        """
                )
                )
        );
    }
    @Test
    void testJt400HeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-jt400",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-jt400", CamelTestUtil.CamelVersion.v4_18)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("KEY", "myKey");
                                        exchange.getIn().setHeader("SENDER_INFORMATION", "sender");
                                    })
                                    .to("jt400:program");
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
                                        exchange.getIn().setHeader("CamelJt400Key", "myKey");
                                        exchange.getIn().setHeader("CamelJt400SenderInformation", "sender");
                                    })
                                    .to("jt400:program");
                            }
                        }
                        """
                )
                )
        );
    }
    @Test
    void testMailHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-mail",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-mail", CamelTestUtil.CamelVersion.v4_18)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("imap://server")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("copyTo", "Archive");
                                        exchange.getIn().setHeader("moveTo", "Processed");
                                        exchange.getIn().setHeader("delete", true);
                                    })
                                    .to("direct:process");
                            }
                        }
                        """,
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("imap://server")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("CamelMailCopyTo", "Archive");
                                        exchange.getIn().setHeader("CamelMailMoveTo", "Processed");
                                        exchange.getIn().setHeader("CamelMailDelete", true);
                                    })
                                    .to("direct:process");
                            }
                        }
                        """
                )
                )
        );
    }
    @Test
    void testMiloHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-milo",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-milo", CamelTestUtil.CamelVersion.v4_18)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("await", true);
                                    })
                                    .to("milo-client:opc.tcp://localhost");
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
                                        exchange.getIn().setHeader("CamelMiloAwait", true);
                                    })
                                    .to("milo-client:opc.tcp://localhost");
                            }
                        }
                        """
                )
                )
        );
    }
    @Test
    void testElasticsearchHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-elasticsearch",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-elasticsearch", CamelTestUtil.CamelVersion.v4_18)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("operation", "INDEX");
                                        exchange.getIn().setHeader("indexId", "123");
                                        exchange.getIn().setHeader("indexName", "my-index");
                                        exchange.getIn().setHeader("documentClass", String.class);
                                    })
                                    .to("elasticsearch:myCluster");
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
                                        exchange.getIn().setHeader("CamelElasticsearchOperation", "INDEX");
                                        exchange.getIn().setHeader("CamelElasticsearchIndexId", "123");
                                        exchange.getIn().setHeader("CamelElasticsearchIndexName", "my-index");
                                        exchange.getIn().setHeader("CamelElasticsearchDocumentClass", String.class);
                                    })
                                    .to("elasticsearch:myCluster");
                            }
                        }
                        """
                )
                )
        );
    }
    @Test
    void testOpensearchHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-opensearch",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-opensearch", CamelTestUtil.CamelVersion.v4_18)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("operation", "INDEX");
                                        exchange.getIn().setHeader("indexId", "123");
                                        exchange.getIn().setHeader("indexName", "my-index");
                                        exchange.getIn().setHeader("documentClass", String.class);
                                    })
                                    .to("opensearch:myCluster");
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
                                        exchange.getIn().setHeader("CamelOpensearchOperation", "INDEX");
                                        exchange.getIn().setHeader("CamelOpensearchIndexId", "123");
                                        exchange.getIn().setHeader("CamelOpensearchIndexName", "my-index");
                                        exchange.getIn().setHeader("CamelOpensearchDocumentClass", String.class);
                                    })
                                    .to("opensearch:myCluster");
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    void testJGroupsHeadersMigration() {
        //language=java
        rewriteRun(
                mavenProject("test-jgroups",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-jgroups", CamelTestUtil.CamelVersion.v4_18)),
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
    void testJiraHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-jira",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-jira", CamelTestUtil.CamelVersion.v4_18)),
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
                                        exchange.getIn().setHeader("linkType", "Relates");
                                        exchange.getIn().setHeader("minutesSpent", 30);
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
                                        exchange.getIn().setHeader("CamelJiraLinkType", "Relates");
                                        exchange.getIn().setHeader("CamelJiraMinutesSpent", 30);
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
    void testJGroupsRaftHeadersMigration() {
        //language=java
        rewriteRun(
                mavenProject("test-jgroups-raft",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-jgroups-raft", CamelTestUtil.CamelVersion.v4_18)),
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
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-shiro", CamelTestUtil.CamelVersion.v4_18)),
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
        //language=java
        rewriteRun(
                mavenProject("test-solr",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-solr", CamelTestUtil.CamelVersion.v4_18)),
                        java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("SolrField.id", "doc123");
                                        exchange.getIn().setHeader("SolrField.name", "Test Document");
                                        exchange.getIn().setHeader("SolrParam.commit", true);
                                        exchange.getIn().setHeader("SolrParam.commitWithin", 1000);
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
                                        exchange.getIn().setHeader("CamelSolrField.id", "doc123");
                                        exchange.getIn().setHeader("CamelSolrField.name", "Test Document");
                                        exchange.getIn().setHeader("CamelSolrParam.commit", true);
                                        exchange.getIn().setHeader("CamelSolrParam.commitWithin", 1000);
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
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-elasticsearch-rest-client", CamelTestUtil.CamelVersion.v4_18)),
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
                                    <artifactId>camel-github2</artifactId>
                                    <version>4.18.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """
                        ),
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
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-google-functions", CamelTestUtil.CamelVersion.v4_18)),
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
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-mongodb-gridfs", CamelTestUtil.CamelVersion.v4_18)),
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
    void testOpenstackHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-openstack",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-openstack", CamelTestUtil.CamelVersion.v4_18)),
                        java(
                                """
                                import org.apache.camel.Exchange;
                                import org.apache.camel.builder.RouteBuilder;

                                class Test extends RouteBuilder {
                                    public void configure() {
                                        from("direct:start")
                                            .process(exchange -> {
                                                exchange.getIn().setHeader("domainId", "default");
                                                exchange.getIn().setHeader("FlavorId", "m1.small");
                                                exchange.getIn().setHeader("containerName", "images");
                                            })
                                            .to("openstack-nova://host");
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
                                                exchange.getIn().setHeader("CamelOpenstackKeystoneDomainId", "default");
                                                exchange.getIn().setHeader("CamelOpenstackNovaFlavorId", "m1.small");
                                                exchange.getIn().setHeader("CamelOpenstackSwiftContainerName", "images");
                                            })
                                            .to("openstack-nova://host");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void testWeb3jHeadersMigrationJava() {
        //language=java
        rewriteRun(
                mavenProject("test-web3j",
                        pomXml(CamelTestUtil.pomXmlWithDependency("camel-web3j", CamelTestUtil.CamelVersion.v4_18)),
                        java(
                                """
                                import org.apache.camel.Exchange;
                                import org.apache.camel.builder.RouteBuilder;

                                class Test extends RouteBuilder {
                                    public void configure() {
                                        from("direct:start")
                                            .process(exchange -> {
                                                exchange.getIn().setHeader("FROM_ADDRESS", "0x123");
                                                exchange.getIn().setHeader("TO_ADDRESS", "0x456");
                                                exchange.getIn().setHeader("AT_BLOCK", "latest");
                                            })
                                            .to("web3j://http://localhost:8545");
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
                                                exchange.getIn().setHeader("CamelWeb3jFromAddress", "0x123");
                                                exchange.getIn().setHeader("CamelWeb3jToAddress", "0x456");
                                                exchange.getIn().setHeader("CamelWeb3jAtBlock", "latest");
                                            })
                                            .to("web3j://http://localhost:8545");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void testYamlDslRoutePolicyRename() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    id: myRoute
                    routePolicy: myPolicy
                    from:
                      uri: direct:start
                      steps:
                        - to:
                            uri: mock:result
                """,
                """
                - route:
                    id: myRoute
                    routePolicyRef: myPolicy
                    from:
                      uri: direct:start
                      steps:
                        - to:
                            uri: mock:result
                """
            )
        );
    }

    @Test
    void testSagaEipXml() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route>
                    <from uri="direct:start"/>
                    <saga sagaService="mySagaService">
                        <compensation uri="mock:compensation"/>
                        <completion uri="mock:completion"/>
                        <option key="myOptionKey">
                            <constant>myOptionValue</constant>
                        </option>
                    </saga>
                    <to uri="mock:end"/>
                </route>
                """,
                """
                <route>
                    <from uri="direct:start"/>
                    <saga sagaService="mySagaService" compensation="mock:compensation" completion="mock:completion">
                        <option key="myOptionKey">
                            <constant>myOptionValue</constant>
                        </option>
                    </saga>
                    <to uri="mock:end"/>
                </route>
                """
            )
        );
    }

    @Test
    void testSagaEipYaml() {
        //language=yaml
        rewriteRun(
            yaml(
                """
                - route:
                    from:
                      uri: direct:start
                      steps:
                        - saga:
                            compensation:
                              uri: direct:compensate
                            completion:
                              uri: direct:complete
                            steps:
                              - to:
                                  uri: direct:action
                """,
                """
                - route:
                    from:
                      uri: direct:start
                      steps:
                        - saga:
                            compensation: direct:compensate
                            completion: direct:complete
                            steps:
                              - to:
                                  uri: direct:action
                """
            )
        );
    }
}
