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
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

public class CamelUpdate423Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_23, true)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_22,
                        "camel-core-model", "camel-api", "camel-support", "camel-zeebe", "jakarta.xml.bind-api"))
                .typeValidationOptions(TypeValidation.none())
                .expectedCyclesThatMakeChanges(1);
    }

    @DocumentExample
    @Test
    void migrateCSimpleJavaApi() {
        //language=java
        rewriteRun(
                java(
                        """
                        import org.apache.camel.builder.Builder;
                        import org.apache.camel.builder.RouteBuilder;
                        import org.apache.camel.model.language.CSimpleExpression;

                        public class CSimpleRoute extends RouteBuilder {
                            private CSimpleExpression expression = new CSimpleExpression("${body}");

                            @Override
                            public void configure() {
                                from("direct:in")
                                    .filter(Builder.csimple("${body} != null"))
                                    .setBody().csimple("${body.toUpperCase()}")
                                    .to("mock:result");
                            }
                        }
                        """,
                        """
                        import org.apache.camel.builder.Builder;
                        import org.apache.camel.builder.RouteBuilder;
                        import org.apache.camel.model.language.SimpleExpression;

                        public class CSimpleRoute extends RouteBuilder {
                            private SimpleExpression expression = new SimpleExpression("${body}");

                            @Override
                            public void configure() {
                                from("direct:in")
                                    .filter(Builder.simple("${body} != null"))
                                    .setBody().simple("${body.toUpperCase()}")
                                    .to("mock:result");
                            }
                        }
                        """
                )
        );
    }

    @Test
    void preconditionLeavesPlainSimpleUsageUntouched() {
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(0),
                java(
                        """
                        import org.apache.camel.builder.RouteBuilder;

                        import static org.apache.camel.builder.Builder.simple;

                        public class SimpleRoute extends RouteBuilder {
                            @Override
                            public void configure() {
                                from("direct:in")
                                    .filter(simple("${body} != null"))
                                    .to("mock:result");
                            }
                        }
                        """
                )
        );
    }

    @Test
    void migrateCSimpleXmlDsl() {
        //language=xml
        rewriteRun(
                xml(
                        """
                        <routes xmlns="http://camel.apache.org/schema/spring">
                            <route id="csimpleRoute">
                                <from uri="direct:in"/>
                                <filter>
                                    <csimple>${body} != null</csimple>
                                    <to uri="mock:result"/>
                                </filter>
                            </route>
                        </routes>
                        """,
                        """
                        <routes xmlns="http://camel.apache.org/schema/spring">
                            <route id="csimpleRoute">
                                <from uri="direct:in"/>
                                <filter>
                                    <simple>${body} != null</simple>
                                    <to uri="mock:result"/>
                                </filter>
                            </route>
                        </routes>
                        """
                )
        );
    }

    @Test
    void preconditionLeavesNonCamelXmlCSimpleTagUntouched() {
        //language=xml
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(0),
                xml(
                        """
                        <config>
                            <csimple>not a Camel document</csimple>
                        </config>
                        """
                )
        );
    }

    @Test
    void migrateCSimpleYamlDsl() {
        //language=yaml
        rewriteRun(
                yaml(
                        """
                        - route:
                            id: csimpleRoute
                            from:
                              uri: direct:in
                              steps:
                                - filter:
                                    csimple: "${body} != null"
                                - to:
                                    uri: mock:result
                        """,
                        """
                        - route:
                            id: csimpleRoute
                            from:
                              uri: direct:in
                              steps:
                                - filter:
                                    simple: "${body} != null"
                                - to:
                                    uri: mock:result
                        """
                )
        );
    }

    @Test
    void preconditionLeavesNonCamelYamlCSimpleKeyUntouched() {
        //language=yaml
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(0),
                yaml(
                        """
                        config:
                          csimple: "not a Camel document"
                        """
                )
        );
    }

    @Test
    void removeCSimpleDependencies() {
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
                                    <artifactId>camel-csimple-joor</artifactId>
                                    <version>4.22.0</version>
                                </dependency>
                            </dependencies>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.camel</groupId>
                                        <artifactId>camel-csimple-maven-plugin</artifactId>
                                        <version>4.22.0</version>
                                    </plugin>
                                </plugins>
                            </build>
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
    void migrateZeebeToCamunda() {
        rewriteRun(
                mavenProject("test-zeebe",
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
                                            <artifactId>camel-zeebe</artifactId>
                                            <version>4.22.0</version>
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
                                            <artifactId>camel-camunda</artifactId>
                                            <version>4.23.0</version>
                                        </dependency>
                                    </dependencies>
                                </project>
                                """
                        ),
                        //language=java
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;
                                import org.apache.camel.component.zeebe.ZeebeConstants;
                                import org.apache.camel.component.zeebe.model.JobRequest;
                                import org.apache.camel.component.zeebe.model.ZeebeMessage;

                                public class ZeebeRoute extends RouteBuilder {
                                    private JobRequest request;
                                    private ZeebeMessage message;

                                    @Override
                                    public void configure() {
                                        from("zeebe:worker?jobKey=myJob")
                                            .setHeader(ZeebeConstants.HEADER_PREFIX, constant("x"))
                                            .setHeader("CamelZeebeJobKey", constant("123"))
                                            .to("zeebe:deployment?resource=classpath:process.bpmn");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.builder.RouteBuilder;
                                import org.apache.camel.component.camunda.CamundaConstants;
                                import org.apache.camel.component.camunda.model.CamundaMessage;
                                import org.apache.camel.component.camunda.model.JobRequest;

                                public class ZeebeRoute extends RouteBuilder {
                                    private JobRequest request;
                                    private CamundaMessage message;

                                    @Override
                                    public void configure() {
                                        from("camunda:worker?jobType=myJob")
                                            .setHeader(CamundaConstants.HEADER_PREFIX, constant("x"))
                                            .setHeader("CamelCamundaJobKey", constant("123"))
                                            .to("camunda:deployment?resource=classpath:process.bpmn");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void preconditionBlocksZeebeWithoutDependency() {
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(CamelTestUtil.isRecipeOverridden() ? 1 : 0),
                mavenProject("test-negative",
                        CamelTestUtil.pomXmlSpec("camel-core", CamelTestUtil.CamelVersion.v4_22),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class NegativeZeebeRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .to("zeebe:worker?jobKey=myJob");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void migrateIbmCosCacheControlHeader() {
        //language=java
        rewriteRun(
                mavenProject("test-ibm-cos",
                        CamelTestUtil.pomXmlSpec("camel-ibm-cos", CamelTestUtil.CamelVersion.v4_22),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class IbmCosRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:next")
                                            .setHeader("CamelIBMCOSContentControl", constant("no-cache"))
                                            .to("ibm-cos:mybucket");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class IbmCosRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:next")
                                            .setHeader("CamelIBMCOSCacheControl", constant("no-cache"))
                                            .to("ibm-cos:mybucket");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void preconditionBlocksIbmCosWithoutDependency() {
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(CamelTestUtil.isRecipeOverridden() ? 1 : 0),
                mavenProject("test-negative-ibm-cos",
                        CamelTestUtil.pomXmlSpec("camel-core", CamelTestUtil.CamelVersion.v4_22),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class NegativeIbmCosRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:next")
                                            .setHeader("CamelIBMCOSContentControl", constant("no-cache"))
                                            .to("mock:result");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void removeHeadersmapDependency() {
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
                                    <artifactId>camel-headersmap</artifactId>
                                    <version>4.22.0</version>
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
    void removeReactiveExecutorVertxDependency() {
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
                                    <artifactId>camel-reactive-executor-vertx</artifactId>
                                    <version>4.22.0</version>
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
    void removeThreadPoolFactoryVertxDependency() {
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
                                    <artifactId>camel-threadpoolfactory-vertx</artifactId>
                                    <version>4.22.0</version>
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
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void removeLangchain4jToolsRequiresAiToolPresent() {
        //language=xml
        rewriteRun(
                spec -> CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_23, true,
                                "org.apache.camel.upgrade.camel423.removeLangchain4jToolsModule")
                        .expectedCyclesThatMakeChanges(0),
                mavenProject("test-negative-langchain4j-tools",
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
                                            <version>4.22.0</version>
                                        </dependency>
                                    </dependencies>
                                </project>
                                """
                        )
                )
        );
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void removeLangchain4jToolsWhenAiToolAlreadyPresent() {
        //language=xml
        rewriteRun(
                spec -> CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_23, true,
                        "org.apache.camel.upgrade.camel423.removeLangchain4jToolsModule"),
                mavenProject("test-langchain4j-tools",
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
                                            <version>4.22.0</version>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel</groupId>
                                            <artifactId>camel-ai-tool</artifactId>
                                            <version>4.22.0</version>
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
                )
        );
    }

    /**
     * The full chain in latest.yaml runs camel423 before camel422. camel423 removes
     * camel-langchain4j-tools only once camel-ai-tool is present, so on a project that has never been
     * migrated, camel422.migrateLangchain4jToolsUris must still see the camel-langchain4j-tools
     * dependency, migrate the consumer URI and add camel-ai-tool in the first cycle; camel423 then
     * removes the now-redundant camel-langchain4j-tools dependency on the following cycle.
     */
    @Test
    void fullChainMigratesLangchain4jToolsUriBeforeRemovingDependency() {
        rewriteRun(
                spec -> spec.recipeFromResources("org.apache.camel.upgrade.CamelMigrationRecipe")
                        .expectedCyclesThatMakeChanges(2),
                mavenProject("test-langchain4j-tools-chain",
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
                                            <version>4.23.0</version>
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
                                    }
                                }
                                """
                        )
                )
        );
    }

    /**
     * Per RECIPE-CONVENTIONS.md, camel422_1.CamelMigrationRecipe (the CAMEL-24539 camel-openai flag
     * recipe) is not wired into latest.yaml directly; the next main-version recipe references it
     * instead. This guards that wiring, so it can't be silently dropped in a future edit.
     */
    @Test
    void camel423ChainIncludesCamel422_1PatchRecipe() {
        //language=java
        rewriteRun(
                mavenProject("test-openai-patch",
                        CamelTestUtil.pomXmlSpec("camel-openai", CamelTestUtil.CamelVersion.v4_22),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class OpenAiRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:openai")
                                            .removeProperty("CamelOpenAIResponse");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class OpenAiRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:openai")
                                            .removeProperty(/* CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update the property name manually if needed.*/"CamelOpenAIResponse");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void migrateJoorJavaApi() {
        //language=java
        rewriteRun(
                java(
                        """
                        import org.apache.camel.builder.Builder;
                        import org.apache.camel.builder.RouteBuilder;
                        import org.apache.camel.model.language.JoorExpression;

                        public class JoorRoute extends RouteBuilder {
                            private JoorExpression expression = new JoorExpression("${body}");

                            @Override
                            public void configure() {
                                from("direct:in")
                                    .filter(Builder.joor("${body} != null"))
                                    .setBody().joor("${body.toUpperCase()}")
                                    .to("mock:result");
                            }
                        }
                        """,
                        """
                        import org.apache.camel.builder.Builder;
                        import org.apache.camel.builder.RouteBuilder;
                        import org.apache.camel.model.language.JavaExpression;

                        public class JoorRoute extends RouteBuilder {
                            private JavaExpression expression = new JavaExpression("${body}");

                            @Override
                            public void configure() {
                                from("direct:in")
                                    .filter(Builder.java("${body} != null"))
                                    .setBody().java("${body.toUpperCase()}")
                                    .to("mock:result");
                            }
                        }
                        """
                )
        );
    }

    /**
     * BeanFactoryDefinition.joor(String) still exists in 4.23, unlike the builder methods, so it must be
     * left alone.
     */
    @Test
    void leavesBeanFactoryDefinitionJoorUntouched() {
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(0),
                java(
                        """
                        import org.apache.camel.builder.RouteBuilder;

                        public class BeanJoorRoute extends RouteBuilder {
                            @Override
                            public void configure() {
                                bean("myBean").joor("new com.example.MyBean()");
                            }
                        }
                        """
                )
        );
    }

    @Test
    void migrateJoorXmlDsl() {
        //language=xml
        rewriteRun(
                xml(
                        """
                        <routes xmlns="http://camel.apache.org/schema/spring">
                            <route id="joorRoute">
                                <from uri="direct:in"/>
                                <filter>
                                    <joor>${body} != null</joor>
                                    <to uri="mock:result"/>
                                </filter>
                            </route>
                        </routes>
                        """,
                        """
                        <routes xmlns="http://camel.apache.org/schema/spring">
                            <route id="joorRoute">
                                <from uri="direct:in"/>
                                <filter>
                                    <java>${body} != null</java>
                                    <to uri="mock:result"/>
                                </filter>
                            </route>
                        </routes>
                        """
                )
        );
    }

    @Test
    void migrateJoorYamlDsl() {
        //language=yaml
        rewriteRun(
                yaml(
                        """
                        - route:
                            id: joorRoute
                            from:
                              uri: direct:in
                              steps:
                                - filter:
                                    joor: "${body} != null"
                                - to:
                                    uri: mock:result
                        """,
                        """
                        - route:
                            id: joorRoute
                            from:
                              uri: direct:in
                              steps:
                                - filter:
                                    java: "${body} != null"
                                - to:
                                    uri: mock:result
                        """
                )
        );
    }

    @Test
    void preconditionLeavesNonCamelYamlJoorKeyUntouched() {
        //language=yaml
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(0),
                yaml(
                        """
                        config:
                          joor: "not a Camel document"
                        """
                )
        );
    }

    @Test
    void migrateRemovedLanguageUris() {
        //language=java
        rewriteRun(
                java(
                        """
                        import org.apache.camel.builder.RouteBuilder;

                        public class LanguageRoute extends RouteBuilder {
                            @Override
                            public void configure() {
                                from("direct:in")
                                    .to("language:csimple:${body} != null")
                                    .to("language:joor:return body;")
                                    .to("language:simple:${body}");
                            }
                        }
                        """,
                        """
                        import org.apache.camel.builder.RouteBuilder;

                        public class LanguageRoute extends RouteBuilder {
                            @Override
                            public void configure() {
                                from("direct:in")
                                    .to("language:simple:${body} != null")
                                    .to("language:java:return body;")
                                    .to("language:simple:${body}");
                            }
                        }
                        """
                )
        );
    }

    @Test
    void migrateMustacheHeaders() {
        //language=java
        rewriteRun(
                mavenProject("test-mustache",
                        CamelTestUtil.pomXmlSpec("camel-mustache", CamelTestUtil.CamelVersion.v4_22),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class MustacheRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .setHeader("MustacheResourceUri", constant("other.mustache"))
                                            .setHeader("MustacheTemplate", constant("Hello {{name}}"))
                                            .to("mustache:template.mustache");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class MustacheRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .setHeader("CamelMustacheResourceUri", constant("other.mustache"))
                                            .setHeader("CamelMustacheTemplate", constant("Hello {{name}}"))
                                            .to("mustache:template.mustache");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void preconditionBlocksMustacheHeadersWithoutDependency() {
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(CamelTestUtil.isRecipeOverridden() ? 1 : 0),
                mavenProject("test-no-mustache",
                        CamelTestUtil.pomXmlSpec("camel-core", CamelTestUtil.CamelVersion.v4_22),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class NoMustacheRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .setHeader("MustacheResourceUri", constant("other.mustache"))
                                            .to("mock:result");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void migrateChunkHeaders() {
        //language=java
        rewriteRun(
                mavenProject("test-chunk",
                        CamelTestUtil.pomXmlSpec("camel-chunk", CamelTestUtil.CamelVersion.v4_22),
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class ChunkRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .setHeader("ChunkResourceUri", constant("other"))
                                            .setHeader("ChunkTemplate", constant("Hello {$name}"))
                                            .to("chunk:template");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class ChunkRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("direct:in")
                                            .setHeader("CamelChunkResourceUri", constant("other"))
                                            .setHeader("CamelChunkTemplate", constant("Hello {$name}"))
                                            .to("chunk:template");
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void migrateDebeziumOracleOutServerName() {
        rewriteRun(
                mavenProject("test-debezium-oracle",
                        CamelTestUtil.pomXmlSpec("camel-debezium-oracle", CamelTestUtil.CamelVersion.v4_22),
                        //language=java
                        java(
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class DebeziumRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("debezium-oracle:myOracle?databaseOutServerName=dbzxout&databaseUser=scott")
                                            .to("mock:result");
                                    }
                                }
                                """,
                                """
                                import org.apache.camel.builder.RouteBuilder;

                                public class DebeziumRoute extends RouteBuilder {
                                    @Override
                                    public void configure() {
                                        from("debezium-oracle:myOracle?xstreamOutServerName=dbzxout&databaseUser=scott")
                                            .to("mock:result");
                                    }
                                }
                                """
                        ),
                        properties(
                                "camel.component.debezium-oracle.database-out-server-name=dbzxout",
                                "camel.component.debezium-oracle.xstream-out-server-name=dbzxout",
                                spec -> spec.path("application.properties")
                        )
                )
        );
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void migrateJoltCommunityFork() {
        rewriteRun(
                mavenProject("test-jolt",
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
                                            <artifactId>camel-jolt</artifactId>
                                            <version>4.22.0</version>
                                        </dependency>
                                        <dependency>
                                            <groupId>com.bazaarvoice.jolt</groupId>
                                            <artifactId>jolt-core</artifactId>
                                            <version>0.1.8</version>
                                        </dependency>
                                        <dependency>
                                            <groupId>com.bazaarvoice.jolt</groupId>
                                            <artifactId>json-utils</artifactId>
                                            <version>0.1.8</version>
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
                                            <artifactId>camel-jolt</artifactId>
                                            <version>4.22.0</version>
                                        </dependency>
                                        <dependency>
                                            <groupId>io.github.jolt-community.jolt</groupId>
                                            <artifactId>jolt-community-core</artifactId>
                                            <version>1.3.0</version>
                                        </dependency>
                                        <dependency>
                                            <groupId>io.github.jolt-community.jolt</groupId>
                                            <artifactId>json-community-utils</artifactId>
                                            <version>1.3.0</version>
                                        </dependency>
                                    </dependencies>
                                </project>
                                """
                        )
                )
        );
    }

    @Test
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    void removeReactiveExecutorTomcatDependency() {
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
                                    <version>4.22.0</version>
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
}
