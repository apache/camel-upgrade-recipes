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
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Tests for migrating from Camel 4.22.0 to 4.22.1 (CAMEL-24539, camel-openai).
 */
public class CamelUpdate422_1Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_22_1)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_22, "camel-api",
                        "camel-core-model", "camel-support", "camel-openai"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void javaDslMigratesEligibleOperationsAndLeavesOthersUnchanged() {
        //language=java
        rewriteRun(
                mavenProject("test-openai",
                        CamelTestUtil.pomXmlSpec("camel-openai", CamelTestUtil.CamelVersion.v4_22),
                        java(
                        """
                        import org.apache.camel.builder.RouteBuilder;
                        import org.apache.camel.component.openai.OpenAIConstants;

                        class Test extends RouteBuilder {
                            public void configure() {
                                // Single embeddings operation: renamed, lookalike names left alone
                                from("direct:embeddings")
                                    .setProperty("CamelOpenAIResponseModel", constant("keep-model"))
                                    .setProperty("CamelOpenAIResponsesResponse", constant("keep-responses"))
                                    .to("openai:embeddings?storeFullResponse=true")
                                    .process(exchange -> {
                                        Object response = exchange.getProperty("CamelOpenAIResponse", Object.class);
                                        exchange.setProperty("CamelOpenAIResponse", response);
                                    })
                                    .setProperty(OpenAIConstants.RESPONSE, exchangeProperty("CamelOpenAIResponse"))
                                    .setBody(simple("${exchangeProperty.CamelOpenAIResponse.data}"))
                                    .removeProperty("CamelOpenAIResponseId")
                                    .removeProperty("CamelOpenAIResponse");

                                // Single audio-transcription operation: renamed to its own property
                                from("direct:audio-transcription")
                                    .to("openai:audio-transcription")
                                    .setProperty("CamelOpenAIResponse", exchangeProperty("CamelOpenAIResponse"));

                                // Single audio-translation operation: renamed to its own property
                                from("direct:audio-translation")
                                    .to("openai:audio-translation")
                                    .setProperty("CamelOpenAIResponse", exchangeProperty("CamelOpenAIResponse"));

                                // chat-completion still uses CamelOpenAIResponse: unchanged
                                from("direct:chat")
                                    .to("openai:chat-completion")
                                    .setProperty("CamelOpenAIResponse", exchangeProperty("CamelOpenAIResponse"));

                                // Mixed operations: unchanged
                                from("direct:mixed-chat-embeddings")
                                    .to("openai:chat-completion")
                                    .to("openai:embeddings")
                                    .setProperty("CamelOpenAIResponse", exchangeProperty("CamelOpenAIResponse"));

                                from("direct:mixed-chat-tool")
                                    .to("openai:chat-completion")
                                    .to("openai:tool-execution")
                                    .setProperty("CamelOpenAIResponse", exchangeProperty("CamelOpenAIResponse"));
                            }
                        }
                        """,
                        """
                        import org.apache.camel.builder.RouteBuilder;
                        import org.apache.camel.component.openai.OpenAIConstants;

                        class Test extends RouteBuilder {
                            public void configure() {
                                // Single embeddings operation: renamed, lookalike names left alone
                                from("direct:embeddings")
                                    .setProperty("CamelOpenAIResponseModel", constant("keep-model"))
                                    .setProperty("CamelOpenAIResponsesResponse", constant("keep-responses"))
                                    .to("openai:embeddings?storeFullResponse=true")
                                    .process(exchange -> {
                                        Object response = exchange.getProperty("CamelOpenAIEmbeddingsResponse", Object.class);
                                        exchange.setProperty("CamelOpenAIEmbeddingsResponse", response);
                                    })
                                    .setProperty(OpenAIConstants.EMBEDDINGS_RESPONSE, exchangeProperty("CamelOpenAIEmbeddingsResponse"))
                                    .setBody(simple("${exchangeProperty.CamelOpenAIEmbeddingsResponse.data}"))
                                    .removeProperty("CamelOpenAIResponseId")
                                    .removeProperty("CamelOpenAIEmbeddingsResponse");

                                // Single audio-transcription operation: renamed to its own property
                                from("direct:audio-transcription")
                                    .to("openai:audio-transcription")
                                    .setProperty("CamelOpenAIAudioTranscriptionResponse", exchangeProperty("CamelOpenAIAudioTranscriptionResponse"));

                                // Single audio-translation operation: renamed to its own property
                                from("direct:audio-translation")
                                    .to("openai:audio-translation")
                                    .setProperty("CamelOpenAIAudioTranslationResponse", exchangeProperty("CamelOpenAIAudioTranslationResponse"));

                                // chat-completion still uses CamelOpenAIResponse: unchanged
                                from("direct:chat")
                                    .to("openai:chat-completion")
                                    .setProperty("CamelOpenAIResponse", exchangeProperty("CamelOpenAIResponse"));

                                // Mixed operations: unchanged
                                from("direct:mixed-chat-embeddings")
                                    .to("openai:chat-completion")
                                    .to("openai:embeddings")
                                    .setProperty("CamelOpenAIResponse", exchangeProperty("CamelOpenAIResponse"));

                                from("direct:mixed-chat-tool")
                                    .to("openai:chat-completion")
                                    .to("openai:tool-execution")
                                    .setProperty("CamelOpenAIResponse", exchangeProperty("CamelOpenAIResponse"));
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void javaDslWithoutCamelOpenAIDependencyIsUnchanged() {
        //language=java
        rewriteRun(
                mavenProject("test-no-openai",
                        CamelTestUtil.pomXmlSpec("camel-core", CamelTestUtil.CamelVersion.v4_22),
                        java(
                        """
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:embeddings")
                                    .to("openai:embeddings?storeFullResponse=true")
                                    .setProperty("CamelOpenAIResponse", exchangeProperty("CamelOpenAIResponse"));
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void javaDslIsIdempotent() {
        //language=java
        rewriteRun(
                mavenProject("test-openai-idempotent",
                        CamelTestUtil.pomXmlSpec("camel-openai", CamelTestUtil.CamelVersion.v4_22),
                        java(
                        """
                        import org.apache.camel.builder.RouteBuilder;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:embeddings")
                                    .to("openai:embeddings?storeFullResponse=true")
                                    .setProperty("CamelOpenAIEmbeddingsResponse", exchangeProperty("CamelOpenAIEmbeddingsResponse"));
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void yamlDslMigratesEmbeddingsRouteAndLeavesChatRouteUnchanged() {
        //language=yaml
        rewriteRun(
                mavenProject("test-openai-yaml",
                        CamelTestUtil.pomXmlSpec("camel-openai", CamelTestUtil.CamelVersion.v4_22),
                        yaml(
                        """
                        - route:
                            from:
                              uri: "direct:embeddings"
                            steps:
                              - to:
                                  uri: "openai:embeddings?storeFullResponse=true"
                              - setProperty:
                                  name: CamelOpenAIResponse
                                  simple: "${exchangeProperty.CamelOpenAIResponse}"
                              - setBody:
                                  simple: "${exchangeProperty.CamelOpenAIResponse.data}"
                        - route:
                            from:
                              uri: "direct:chat"
                            steps:
                              - to:
                                  uri: "openai:chat-completion"
                              - setProperty:
                                  name: CamelOpenAIResponse
                                  simple: "${exchangeProperty.CamelOpenAIResponse}"
                        """,
                        """
                        - route:
                            from:
                              uri: "direct:embeddings"
                            steps:
                              - to:
                                  uri: "openai:embeddings?storeFullResponse=true"
                              - setProperty:
                                  name: CamelOpenAIEmbeddingsResponse
                                  simple: "${exchangeProperty.CamelOpenAIEmbeddingsResponse}"
                              - setBody:
                                  simple: "${exchangeProperty.CamelOpenAIEmbeddingsResponse.data}"
                        - route:
                            from:
                              uri: "direct:chat"
                            steps:
                              - to:
                                  uri: "openai:chat-completion"
                              - setProperty:
                                  name: CamelOpenAIResponse
                                  simple: "${exchangeProperty.CamelOpenAIResponse}"
                        """
                )
                )
        );
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void xmlDslMigratesEmbeddingsRouteAndLeavesChatRouteUnchanged() {
        //language=xml
        rewriteRun(
                mavenProject("test-openai-xml",
                        CamelTestUtil.pomXmlSpec("camel-openai", CamelTestUtil.CamelVersion.v4_22),
                        xml(
                        """
                        <route xmlns="http://camel.apache.org/schema/spring">
                            <from uri="direct:embeddings"/>
                            <to uri="openai:embeddings?storeFullResponse=true"/>
                            <setProperty name="CamelOpenAIResponse">
                                <simple>${exchangeProperty.CamelOpenAIResponse}</simple>
                            </setProperty>
                        </route>
                        """,
                        """
                        <route xmlns="http://camel.apache.org/schema/spring">
                            <from uri="direct:embeddings"/>
                            <to uri="openai:embeddings?storeFullResponse=true"/>
                            <setProperty name="CamelOpenAIEmbeddingsResponse">
                                <simple>${exchangeProperty.CamelOpenAIEmbeddingsResponse}</simple>
                            </setProperty>
                        </route>
                        """
                )
                )
        );

        //language=xml
        rewriteRun(
                mavenProject("test-openai-xml-chat",
                        CamelTestUtil.pomXmlSpec("camel-openai", CamelTestUtil.CamelVersion.v4_22),
                        xml(
                        """
                        <route xmlns="http://camel.apache.org/schema/spring">
                            <from uri="direct:chat"/>
                            <to uri="openai:chat-completion"/>
                            <setProperty name="CamelOpenAIResponse">
                                <simple>${exchangeProperty.CamelOpenAIResponse}</simple>
                            </setProperty>
                        </route>
                        """
                )
                )
        );
    }
}
