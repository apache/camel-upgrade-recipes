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
 * <p>
 * Whether a given CamelOpenAIResponse occurrence should become the embeddings/audio-transcription/
 * audio-translation property, or stay CamelOpenAIResponse (chat-completion), is not mechanically
 * decidable in general -- see {@code org.apache.camel.upgrade.camel422_1.FlagOpenAIFullResponsePropertyInJavaDsl}
 * for why. So the recipe flags every occurrence with a review comment instead of rewriting it.
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
    void javaDslFlagsOccurrencesAndLeavesLookalikesAndUnrelatedConstantAlone() {
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
                                from("direct:openai")
                                    .setProperty("CamelOpenAIResponseModel", constant("keep-model"))
                                    .process(exchange -> {
                                        Object response = exchange.getProperty("CamelOpenAIResponse", Object.class);
                                        exchange.setProperty("CamelOpenAIResponse", response);
                                    })
                                    .setProperty(OpenAIConstants.RESPONSE, exchangeProperty("CamelOpenAIResponse"))
                                    .setBody(simple("${exchangeProperty.CamelOpenAIResponse.data}"))
                                    .removeProperty("CamelOpenAIResponseId")
                                    .removeProperty("CamelOpenAIResponse");
                            }
                        }
                        """,
                        """
                        import org.apache.camel.builder.RouteBuilder;
                        import org.apache.camel.component.openai.OpenAIConstants;

                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:openai")
                                    .setProperty("CamelOpenAIResponseModel", constant("keep-model"))
                                    .process(exchange -> {
                                        Object response = exchange.getProperty(/* CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.*/"CamelOpenAIResponse", Object.class);
                                        exchange.setProperty(/* CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.*/"CamelOpenAIResponse", response);
                                    })
                                    .setProperty(/* CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.*/OpenAIConstants.RESPONSE, exchangeProperty(/* CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.*/"CamelOpenAIResponse"))
                                    .setBody(simple(/* CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.*/"${exchangeProperty.CamelOpenAIResponse.data}"))
                                    .removeProperty("CamelOpenAIResponseId")
                                    .removeProperty(/* CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.*/"CamelOpenAIResponse");
                            }
                        }
                        """
                        ),
                        // A user's own unrelated OpenAIConstants (no camel-openai import) must never be flagged.
                        java(
                        """
                        package com.example;

                        public final class OpenAIConstants {
                            public static final String RESPONSE = "SomeUnrelatedConstant";
                        }
                        """
                        ),
                        java(
                        """
                        package com.example;

                        import org.apache.camel.builder.RouteBuilder;

                        class UnrelatedRoute extends RouteBuilder {
                            public void configure() {
                                from("direct:other")
                                    .setProperty(OpenAIConstants.RESPONSE, constant("x"));
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
                                from("direct:openai")
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
                                from("direct:openai")
                                    .setProperty(/* CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.*/"CamelOpenAIResponse", exchangeProperty(/* CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.*/"CamelOpenAIResponse"));
                            }
                        }
                        """
                )
                )
        );
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void yamlDslFlagsOccurrencesAndLeavesLookalikesAlone() {
        //language=yaml
        rewriteRun(
                mavenProject("test-openai-yaml",
                        CamelTestUtil.pomXmlSpec("camel-openai", CamelTestUtil.CamelVersion.v4_22),
                        yaml(
                        """
                        - route:
                            from:
                              uri: "direct:openai"
                            steps:
                              - setProperty:
                                  name: CamelOpenAIResponseModel
                                  constant: keep-model
                              - setProperty:
                                  name: CamelOpenAIResponse
                                  simple: "${exchangeProperty.CamelOpenAIResponse}"
                              - setBody:
                                  simple: "${exchangeProperty.CamelOpenAIResponse.data}"
                        """,
                        """
                        - route:
                            from:
                              uri: "direct:openai"
                            steps:
                              - setProperty:
                                  name: CamelOpenAIResponseModel
                                  constant: keep-model
                              - setProperty:
                                  # CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.
                                  name: CamelOpenAIResponse
                                  # CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.
                                  simple: "${exchangeProperty.CamelOpenAIResponse}"
                              - setBody:
                                  # CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.
                                  simple: "${exchangeProperty.CamelOpenAIResponse.data}"
                        """
                )
                )
        );
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void xmlDslFlagsOccurrencesAndLeavesLookalikesAlone() {
        //language=xml
        rewriteRun(
                mavenProject("test-openai-xml",
                        CamelTestUtil.pomXmlSpec("camel-openai", CamelTestUtil.CamelVersion.v4_22),
                        xml(
                        """
                        <route xmlns="http://camel.apache.org/schema/spring">
                            <from uri="direct:openai"/>
                            <setProperty name="CamelOpenAIResponseModel">
                                <constant>keep-model</constant>
                            </setProperty>
                            <setProperty name="CamelOpenAIResponse">
                                <simple>${exchangeProperty.CamelOpenAIResponse}</simple>
                            </setProperty>
                        </route>
                        """,
                        """
                        <route xmlns="http://camel.apache.org/schema/spring">
                            <from uri="direct:openai"/>
                            <setProperty name="CamelOpenAIResponseModel">
                                <constant>keep-model</constant>
                            </setProperty>
                            <!-- CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.-->
                            <setProperty name="CamelOpenAIResponse">
                                <!-- CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.-->
                                <simple>${exchangeProperty.CamelOpenAIResponse}</simple>
                            </setProperty>
                        </route>
                        """
                )
                )
        );
    }
}
