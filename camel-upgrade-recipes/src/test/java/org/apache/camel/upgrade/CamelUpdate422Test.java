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
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;

public class CamelUpdate422Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_22, true)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_21,
                        "camel-core-model", "camel-api", "camel-support", "camel-azure-storage-blob", "minio",
                        "openai-java-core", "jakarta.xml.bind-api"))
                .typeValidationOptions(TypeValidation.none())
                .expectedCyclesThatMakeChanges(1);
    }

    @DocumentExample
    @Test
    void migrateAzureStorageBlobCredentialType() {
        //language=java
        rewriteRun(
                java(
                        """
                        import org.apache.camel.component.azure.storage.blob.CredentialType;

                        public class BlobExample {
                            public void example() {
                                CredentialType type = CredentialType.SHARED_KEY_CREDENTIAL;
                            }
                        }
                        """,
                        """
                        import org.apache.camel.component.azure.common.CredentialType;

                        public class BlobExample {
                            public void example() {
                                CredentialType type = CredentialType.SHARED_KEY_CREDENTIAL;
                            }
                        }
                        """
                )
        );
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void migrateAwsApacheClient() {
        //language=xml
        rewriteRun(
                mavenProject("test-aws",
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
                                    <artifactId>camel-aws2-sqs</artifactId>
                                    <version>4.21.0</version>
                                </dependency>
                                <dependency>
                                    <groupId>software.amazon.awssdk</groupId>
                                    <artifactId>apache-client</artifactId>
                                    <version>2.20.0</version>
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
                            <dependencies>
                                <dependency>
                                    <groupId>org.apache.camel</groupId>
                                    <artifactId>camel-aws2-sqs</artifactId>
                                    <version>4.21.0</version>
                                </dependency>
                                <dependency>
                                    <groupId>software.amazon.awssdk</groupId>
                                    <artifactId>apache5-client</artifactId>
                                    <version>2.46.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """
                )
                )
        );
    }

    @Test
    void preconditionBlocksAwsApacheClientWithoutCamelAws() {
        //language=xml
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(0),
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
                                    <groupId>software.amazon.awssdk</groupId>
                                    <artifactId>apache-client</artifactId>
                                    <version>2.20.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """
                )
        );
    }

    @Test
    void migrateMinioCopySource() {
        //language=java
        rewriteRun(
                mavenProject("test-minio",
                        CamelTestUtil.pomXmlSpec("camel-minio", CamelTestUtil.CamelVersion.v4_21),
                        java(
                                """
                                import io.minio.CopySource;

                                public class MinioExample {
                                    public void example() {
                                        CopySource src = null;
                                    }
                                }
                                """,
                                """
                                import io.minio.SourceObject;

                                public class MinioExample {
                                    public void example() {
                                        SourceObject src = null;
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void removeCamelReactiveExecutorTomcatDependency() {
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
                                    <artifactId>camel-reactive-executor-tomcat</artifactId>
                                    <version>4.21.0</version>
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

    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    @Test
    void migrateSpringAiToolsDependency() {
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
                                    <artifactId>camel-spring-ai-tools</artifactId>
                                    <version>4.21.0</version>
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
                            <dependencies>
                                <dependency>
                                    <groupId>org.apache.camel</groupId>
                                    <artifactId>camel-ai-tool</artifactId>
                                    <version>4.22.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """
                )
        );
    }

    @Test
    void migrateOpenAiChatCompletion() {
        //language=java
        rewriteRun(
                mavenProject("test-openai",
                        CamelTestUtil.pomXmlSpec("camel-openai", CamelTestUtil.CamelVersion.v4_21),
                        java(
                                """
                                import com.openai.models.ChatCompletion;

                                public class OpenAiExample {
                                    public void example() {
                                        ChatCompletion completion = null;
                                    }
                                }
                                """,
                                """
                                import com.openai.models.chat.completions.ChatCompletion;

                                public class OpenAiExample {
                                    public void example() {
                                        ChatCompletion completion = null;
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void migrateLangchain4jToolsUris() {
        rewriteRun(
                mavenProject("test-langchain4j-tools",
                        //language=xml
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
                                            <artifactId>camel-langchain4j-tools</artifactId>
                                            <version>4.21.0</version>
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
                                    <dependencies>
                                        <dependency>
                                            <groupId>org.apache.camel</groupId>
                                            <artifactId>camel-ai-tool</artifactId>
                                            <version>4.22.0</version>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel</groupId>
                                            <artifactId>camel-langchain4j-tools</artifactId>
                                            <version>4.21.0</version>
                                        </dependency>
                                    </dependencies>
                                </project>
                                """
                        ),
                        //language=java
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class ToolRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("langchain4j-tools:userDb?tags=users&description=Query user database&parameter.userId=string")
                                            .to("mock:result");
                                        from("direct:invoke")
                                            .to("langchain4j-tools:userDb?tags=users");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class ToolRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("ai-tool:userDb?tags=users&description=Query user database&parameter.userId=string")
                                            .to("mock:result");
                                        from("direct:invoke")
                                            .to("langchain4j-tools:userDb?tags=users");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    @Test
    void migrateSpringAiToolsUris() {
        rewriteRun(
                mavenProject("test-spring-ai-tools",
                        //language=xml
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
                                            <artifactId>camel-spring-ai-tools</artifactId>
                                            <version>4.21.0</version>
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
                                    <dependencies>
                                        <dependency>
                                            <groupId>org.apache.camel</groupId>
                                            <artifactId>camel-ai-tool</artifactId>
                                            <version>4.22.0</version>
                                        </dependency>
                                    </dependencies>
                                </project>
                                """
                        ),
                        //language=java
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class WeatherToolRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("spring-ai-tools:weather?tags=weather&description=Get current weather&parameter.city=string")
                                            .to("mock:result");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class WeatherToolRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("ai-tool:weather?tags=weather&description=Get current weather&parameter.city=string")
                                            .to("mock:result");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void migrateAtmosphereWebsocketHeaders() {
        //language=java
        rewriteRun(
                mavenProject("test-atmosphere-websocket",
                        CamelTestUtil.pomXmlSpec("camel-atmosphere-websocket", CamelTestUtil.CamelVersion.v4_21),
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

    @Test
    void preconditionBlocksMinioWithoutDependency() {
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(CamelTestUtil.isRecipeOverridden() ? 1 : 0),
                mavenProject("test-negative",
                        CamelTestUtil.pomXmlSpec("camel-core", CamelTestUtil.CamelVersion.v4_21),
                        java(
                                """
                                import io.minio.CopySource;

                                public class NegativeMinioExample {
                                    public void example() {
                                        CopySource src = null;
                                    }
                                }
                                """
                        )
                )
        );
    }

}
