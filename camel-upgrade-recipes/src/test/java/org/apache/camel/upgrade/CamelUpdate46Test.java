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
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

class CamelUpdate46Test implements RewriteTest {

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
    @DocumentExample
    @Test
    void search() {
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
    void beanPropertyToProperties() {
        //language=yaml
        rewriteRun(yaml(
          """
            beans:
              - name: "myProcessor"
                type: "#class:com.foo.MyClass"
                property:
                  - key: "payload"
                    value: "test-payload"
            """,
          """
            beans:
              - name: "myProcessor"
                type: "#class:com.foo.MyClass"
                properties:
                  payload: "test-payload"
            """));
    }

    @Test
    void yamlStreamCaching() {
        //language=yaml
        rewriteRun(yaml(
          """
            route:
              streamCaching: false
              from:
                uri: "direct:foo"
                steps:
                  - to: "mock:bar"
            """,
          """
            route:
              streamCache: false
              from:
                uri: "direct:foo"
                steps:
                  - to: "mock:bar"
            """));
    }

    @Test
    void yamlBeanPropertyToProperties2() {
        //language=yaml
        rewriteRun(yaml(
          """
            - beans:
              - name: beanFromMap
                type: com.acme.MyBean
                property:
                  - key: foo
                    value: bar
                  - key: foo2
                    value: bar2
            """,
          """
            - beans:
              - name: beanFromMap
                type: com.acme.MyBean
                properties:
                  foo: bar
                  foo2: bar2
            """));
    }

    @Test
    void xmlProperties() {
        //language=xml
        rewriteRun(xml(
          """
            <bean name="beanFromProps" type="com.acme.MyBean(true, 'Hello World')">
               <property name="msg1" value="messageString1"/>
               <property name="msg2" value="messageString2"/>
            </bean>
            """,
          """
            <bean name="beanFromProps" type="com.acme.MyBean(true, 'Hello World')">
               <properties>
                 <property name="msg1" value="messageString1"/>
                 <property name="msg2" value="messageString2"/>
               </properties>
            </bean>
            """));
    }


    @Test
    void renamedDependencies() {
        //language=xml
        rewriteRun(pomXml(
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
    void langchainChat() {
        //language=java
        rewriteRun(java(
          """
            import org.apache.camel.component.chat.LangChainChat;
            import org.apache.camel.component.chat.LangChainChatOperations;
            import org.apache.camel.component.chat.LangChainChatProducer;

            public class LangchainTest {
                public void test() {
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
                    LangChain4jChat langChainChat;
                    LangChain4jChatOperations langChainChatOperations;
                    LangChain4jChatProducer langChainChatProducer;
                }
            }
            """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_6.html#_camel_langchain4j_chat">CAMEL-LANGCHAIN4J-CHAT</a>
     */
    @Test
    void langchainChat2() {
        //language=java
        rewriteRun(java(
          """
            import org.apache.camel.component.chat.LangChainChatComponent;
            import org.apache.camel.component.chat.LangChainChatConfiguration;
            import org.apache.camel.component.chat.LangChainChatEndpoint;

            public class LangchainTest {
                public void test() {
                    LangChainChatComponent langChainChatComponent;
                    LangChainChatConfiguration  langChainChatConfiguration;
                    LangChainChatEndpoint langChainChatEndpoint;
                }
            }
            """,
          """
            import org.apache.camel.component.langchain4j.chat.LangChain4jChatComponent;
            import org.apache.camel.component.langchain4j.chat.LangChain4jChatConfiguration;
            import org.apache.camel.component.langchain4j.chat.LangChain4jChatEndpoint;

            public class LangchainTest {
                public void test() {
                    LangChain4jChatComponent langChainChatComponent;
                    LangChain4jChatConfiguration  langChainChatConfiguration;
                    LangChain4jChatEndpoint langChainChatEndpoint;
                }
            }
            """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_6.html#_camel_langchain4j_embeddingsat">CAMEL-LANGCHAIN4J-EMBEDDINGS</a>
     */
    @Test
    void langchainEmbeddings() {
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
                }
            }
            """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_6.html#_camel_langchain4j_embeddingsat">CAMEL-LANGCHAIN4J-EMBEDDINGS</a>
     */
    @Test
    void langchainEmbeddings2() {
        //language=java
        rewriteRun(java(
          """
            import org.apache.camel.component.langchain.embeddings.LangChainEmbeddingsEndpoint;
            import org.apache.camel.component.langchain.embeddings.LangChainEmbeddings;
            import org.apache.camel.component.langchain.embeddings.LangChainEmbeddingsProducer;

            public class LangchainTest {
                public void test() {
                    LangChainEmbeddingsEndpoint langChainEmbeddingsEndpoint;
                    LangChainEmbeddings langChainEmbeddings;
                    LangChainEmbeddingsProducer langChainEmbeddingsProducer;
                }
            }
            """,
          """
            import org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddings;
            import org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddingsEndpoint;
            import org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddingsProducer;

            public class LangchainTest {
                public void test() {
                    LangChain4jEmbeddingsEndpoint langChainEmbeddingsEndpoint;
                    LangChain4jEmbeddings langChainEmbeddings;
                    LangChain4jEmbeddingsProducer langChainEmbeddingsProducer;
                }
            }
            """));
    }
}
