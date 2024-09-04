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
package org.apache.camel.updates;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;
import org.openrewrite.yaml.Assertions;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.xml.Assertions.xml;

public class CamelUpdate46Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_6)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_5,
                        "camel-api", "camel-base-engine", "camel-spring-redis", "camel-opensearch",
                        "camel-elasticsearch", "camel-langchain-chat", "camel-langchain-embeddings"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_6.html#_camel_elasticsearch">CAMEL-ELASTICSEARCH</a>
     */
    @Test
    public void testSearch() {
        //language=java
        rewriteRun(java(
                """
                            import org.apache.camel.component.es.aggregation.ElastichsearchBulkRequestAggregationStrategy;
                            
                            public class SearchTest {
                                public void test() {
                                    ElastichsearchBulkRequestAggregationStrategy str = null;
                                }
                            }
                        """,
                """
                            import org.apache.camel.component.es.aggregation.ElasticsearchBulkRequestAggregationStrategy;
                            
                            public class SearchTest {
                                public void test() {
                                    ElasticsearchBulkRequestAggregationStrategy str = null;
                                }
                            }
                        """));
    }


    @Test
    public void testBeanPropertyToProperties() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                beans:
                  - name: "myProcessor"
                    type: "#class:com.foo.MyClass"
                    property:
                      - key: "payload"
                        value: "test-payload"
                """, """
                beans:
                  - name: "myProcessor"
                    type: "#class:com.foo.MyClass"
                    properties:
                      payload: "test-payload"
                """));
    }

    @Test
    public void testYamlStreamCaching() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
            route:
              streamCaching: false
              from:
                uri: "direct:foo"
                steps:
                  - to: "mock:bar"
                """, """
            route:
              streamCache: false
              from:
                uri: "direct:foo"
                steps:
                  - to: "mock:bar"
                """));
    }

    @Test
    public void testYamlBeanPropertyToProperties2() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                - beans:
                  - name: beanFromMap
                    type: com.acme.MyBean
                    property:
                      - key: foo
                        value: bar
                      - key: foo2
                        value: bar2
                """, """
                - beans:
                  - name: beanFromMap
                    type: com.acme.MyBean
                    properties:
                      foo: bar
                      foo2: bar2
                """));
    }

    @Test
    public void testXmlProperties() {
        //language=xml
        rewriteRun(xml("""
                <bean name="beanFromProps" type="com.acme.MyBean(true, 'Hello World')">
                   <property name="msg1" value="messageString1"/>
                   <property name="msg2" value="messageString2"/>
                </bean>
                """, """
                <bean name="beanFromProps" type="com.acme.MyBean(true, 'Hello World')">
                   <properties>
                     <property name="msg1" value="messageString1"/>
                     <property name="msg2" value="messageString2"/>
                   </properties>
                </bean>
                """));
    }


    @Test
    public void testRenamedDependencies() {
        //language=xml
        rewriteRun(org.openrewrite.maven.Assertions.pomXml(
                """
                        <project>
                           <modelVersion>4.0.0</modelVersion>

                           <artifactId>test</artifactId>
                           <groupId>org.apache.camel.test</groupId>
                           <version>1.0.0</version>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-langchain-chat</artifactId>
                                   <version>4.5.0</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-langchain-embeddings</artifactId>
                                   <version>4.5.0</version>
                               </dependency>
                           </dependencies>

                        </project>
                        """,
                """
                        <project>
                           <modelVersion>4.0.0</modelVersion>

                           <artifactId>test</artifactId>
                           <groupId>org.apache.camel.test</groupId>
                           <version>1.0.0</version>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-langchain4j-chat</artifactId>
                                   <version>4.6.0</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-langchain4j-embeddings</artifactId>
                                   <version>4.6.0</version>
                               </dependency>
                           </dependencies>

                        </project>
                        """));
    }


    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_6.html#_camel_langchain4j_chat">CAMEL-LANGCHAIN4J-CHAT</a>
     */
    @Test
    public void testLangchainChat() {
        //language=java
        rewriteRun(java(
                """
                        import org.apache.camel.component.chat.LangChainChat;
                        import org.apache.camel.component.chat.LangChainChatOperations;
                        import org.apache.camel.component.chat.LangChainChatProducer;
                        
                        public class LangchainTest {
                            public void test() {
                                org.apache.camel.component.chat.LangChainChatComponent langChainChatComponent;
                                org.apache.camel.component.chat.LangChainChatConfiguration  langChainChatConfiguration;
                                org.apache.camel.component.chat.LangChainChatEndpoint langChainChatEndpoint;
                                LangChainChat langChainChat;
                                LangChainChatOperations langChainChatOperations;
                                LangChainChatProducer langChainChatProducer;
                            }
                        }
                        """,
                """
                        import org.apache.camel.component.langchain4j.chat.LangChain4jChat;
                        import org.apache.camel.component.langchain4j.chat.LangChain4jChatOperations;
                        import org.apache.camel.component.langchain4j.chat.LangChain4jChatProducer;
                        
                        public class LangchainTest {
                            public void test() {
                                org.apache.camel.component.langchain4j.chat.LangChain4jChatComponent langChainChatComponent;
                                org.apache.camel.component.langchain4j.chat.LangChain4jChatConfiguration  langChainChatConfiguration;
                                org.apache.camel.component.langchain4j.chat.LangChain4jChatEndpoint langChainChatEndpoint;
                                LangChain4jChat langChainChat;
                                LangChain4jChatOperations langChainChatOperations;
                                LangChain4jChatProducer langChainChatProducer;
                            }
                        }
                        """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_6.html#_camel_langchain4j_embeddingsat">CAMEL-LANGCHAIN4J-EMBEDDINGS</a>
     */
    @Test
    public void testLangchainEmbeddings() {
        //language=java
        rewriteRun(java(
                """
                        import org.apache.camel.component.langchain.embeddings.LangChainEmbeddingsComponent;
                        import org.apache.camel.component.langchain.embeddings.LangChainEmbeddingsConfiguration;
                        import org.apache.camel.component.langchain.embeddings.LangChainEmbeddingsConverter;
                        
                        public class LangchainTest {
                            public void test() {
                                LangChainEmbeddingsComponent langChainEmbeddingsComponent;
                                LangChainEmbeddingsConfiguration langChainEmbeddingsConfiguration;
                                LangChainEmbeddingsConverter langChainEmbeddingsConverter;
                                org.apache.camel.component.langchain.embeddings.LangChainEmbeddingsEndpoint langChainEmbeddingsEndpoint;
                                org.apache.camel.component.langchain.embeddings.LangChainEmbeddings langChainEmbeddings;
                                org.apache.camel.component.langchain.embeddings.LangChainEmbeddingsProducer langChainEmbeddingsProducer;
                            }
                        }
                        """,
                """
                        import org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddingsComponent;
                        import org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddingsConfiguration;
                        import org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddingsConverter;
                        
                        public class LangchainTest {
                            public void test() {
                                LangChain4jEmbeddingsComponent langChainEmbeddingsComponent;
                                LangChain4jEmbeddingsConfiguration langChainEmbeddingsConfiguration;
                                LangChain4jEmbeddingsConverter langChainEmbeddingsConverter;
                                org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddingsEndpoint langChainEmbeddingsEndpoint;
                                org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddings langChainEmbeddings;
                                org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddingsProducer langChainEmbeddingsProducer;
                            }
                        }
                        """));
    }
}
