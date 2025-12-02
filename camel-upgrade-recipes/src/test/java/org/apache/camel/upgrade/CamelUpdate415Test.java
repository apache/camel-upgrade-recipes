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
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate415Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_15)
          .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_14, "camel-langchain4j-chat",
                  "camel-langchain4j-embeddings", "camel-milvus", "camel-neo4j", "camel-pinecone", "camel-qdrant", "camel-weaviate",
                  "camel-core-model", "camel-spi", "camel-api", "camel-base-engine", "jakarta.xml.bind-api"))
          .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_15.html#_data_formats">Data Formats</a> in Yaml
     */
    @DocumentExample
    @Test
    void dataFormatYamlDsl() {
        //language=yaml
        rewriteRun(yaml(
                """
                       - dataFormats:
                           crypto:
                             algorithmParameterRef: "111"
                             initVectorRef: "222"
                             keyRef: "333"
                           csv:
                             formatRef: "444"
                             formatName: "555"
                           flatpack:
                             parserFactoryRef: "666"
                           jaxb:
                              namespacePrefixRef: "777"
                           soap:
                              namespacePrefixRef: "888"
                              elementNameStrategyRef: "999"
                           swiftMx:
                               readConfigRef: "1111"
                               writeConfigRef: "1222"
                           xmlSecurity:
                               keyOrTrustStoreParametersRef: "1333"
                  """,
                """
                        - dataFormats:
                            crypto:
                              algorithmParameterSpec: "111"
                              initVector: "222"
                              key: "333"
                            csv:
                              format: "444"
                              format: "555"
                            flatpack:
                              parserFactory: "666"
                            jaxb:
                               namespacePrefix: "777"
                            soap:
                               namespacePrefix: "888"
                               elementNameStrategy: "999"
                            swiftMx:
                                readConfig: "1111"
                                writeConfig: "1222"
                            xmlSecurity:
                                keyOrTrustStoreParameters: "1333"
                  """));
    }
    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_15.html#_data_formats">Data Formats</a> in Yaml
     */
    @Test
    void dataFormatUnmarshalYamlDsl() {
        //language=yaml
        rewriteRun(yaml(
                """
                - route:
                    id: csv-processing-route
                    from:
                      uri: "direct://input"
                      steps:
                        - unmarshal:
                            crypto:
                              algorithmParameterRef: "111"
                              initVectorRef: "222"
                              keyRef: "333"
                            csv:
                              formatRef: "444"
                              formatName: "555"
                            flatpack:
                              parserFactoryRef: "666"
                            jaxb:
                              namespacePrefixRef: "777"
                            soap:
                              namespacePrefixRef: "888"
                              elementNameStrategyRef: "999"
                            swiftMx:
                              readConfigRef: "1111"
                              writeConfigRef: "1222"
                            xmlSecurity:
                              keyOrTrustStoreParametersRef: "1333"
                """,
                """
                    - route:
                        id: csv-processing-route
                        from:
                          uri: "direct://input"
                          steps:
                            - unmarshal:
                                crypto:
                                  algorithmParameterSpec: "111"
                                  initVector: "222"
                                  key: "333"
                                csv:
                                  format: "444"
                                  format: "555"
                                flatpack:
                                  parserFactory: "666"
                                jaxb:
                                  namespacePrefix: "777"
                                soap:
                                  namespacePrefix: "888"
                                  elementNameStrategy: "999"
                                swiftMx:
                                  readConfig: "1111"
                                  writeConfig: "1222"
                                xmlSecurity:
                                  keyOrTrustStoreParameters: "1333"
                """));
    }

     /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_15.html#_data_formats">Data Formats</a> in Yaml
     */
    @Test
    void dataFormatMarshalYamlDsl() {
        //language=yaml
        rewriteRun(yaml(
                """
                - route:
                    id: csv-processing-route
                    from:
                      uri: "direct://input"
                      steps:
                        - marshal:
                            crypto:
                              algorithmParameterRef: "111"
                              initVectorRef: "222"
                              keyRef: "333"
                            csv:
                              formatRef: "444"
                              formatName: "555"
                            flatpack:
                              parserFactoryRef: "666"
                            jaxb:
                              namespacePrefixRef: "777"
                            soap:
                              namespacePrefixRef: "888"
                              elementNameStrategyRef: "999"
                            swiftMx:
                              readConfigRef: "1111"
                              writeConfigRef: "1222"
                            xmlSecurity:
                              keyOrTrustStoreParametersRef: "1333"
                """,
                """
                    - route:
                        id: csv-processing-route
                        from:
                          uri: "direct://input"
                          steps:
                            - marshal:
                                crypto:
                                  algorithmParameterSpec: "111"
                                  initVector: "222"
                                  key: "333"
                                csv:
                                  format: "444"
                                  format: "555"
                                flatpack:
                                  parserFactory: "666"
                                jaxb:
                                  namespacePrefix: "777"
                                soap:
                                  namespacePrefix: "888"
                                  elementNameStrategy: "999"
                                swiftMx:
                                  readConfig: "1111"
                                  writeConfig: "1222"
                                xmlSecurity:
                                  keyOrTrustStoreParameters: "1333"
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_15.html#_camel_ai_nested_headers_classes">Camel AI Nested Headers classes</a>
     */
    @Test
    void aiNestedHeadersTest() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.component.langchain4j.chat.LangChain4jChat.Headers;
                  import org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddings.Headers;
                  import org.apache.camel.component.milvus.Milvus.Headers;
                  import org.apache.camel.component.neo4j.Neo4jConstants.Headers;
                  import org.apache.camel.component.qdrant.Qdrant.Headers;
                  import org.apache.camel.component.pinecone.PineconeVectorDb.Headers;
                  import org.apache.camel.component.weaviate.WeaviateVectorDb.Headers;
      
                  public class AiHeaders{
                      public void method() {
                          org.apache.camel.component.langchain4j.chat.LangChain4jChat.Headers h1 = null;
                          org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddings.Headers h2 = null;
                          org.apache.camel.component.milvus.Milvus.Headers h4 = null;
                          org.apache.camel.component.neo4j.Neo4jConstants.Headers h5 = null;
                          org.apache.camel.component.qdrant.Qdrant.Headers h6 = null;
                          org.apache.camel.component.pinecone.PineconeVectorDb.Headers h7 = null;
                          org.apache.camel.component.weaviate.WeaviateVectorDb.Headers h8 = null;
                      }
                  }
                  """,
                """
                import org.apache.camel.component.langchain4j.chat.LangChain4jChatHeaders;
                import org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddingsHeaders;
                import org.apache.camel.component.milvus.MilvusHeaders;
                import org.apache.camel.component.neo4j.Neo4jHeaders;
                import org.apache.camel.component.qdrant.QdrantHeaders;
                import org.apache.camel.component.pinecone.PineconeVectorDbHeaders;
                import org.apache.camel.component.weaviate.WeaviateVectorDbHeaders;
                
                public class AiHeaders{
                    public void method() {
                        LangChain4jChatHeaders h1 = null;
                        LangChain4jEmbeddingsHeaders h2 = null;
                        MilvusHeaders h4 = null;
                        Neo4jHeaders h5 = null;
                        QdrantHeaders h6 = null;
                        PineconeVectorDbHeaders h7 = null;
                        WeaviateVectorDbHeaders h8 = null;
                    }
                }
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_15.html#_data_formats">Data Formats</a>
     */
    @Test
    void dataformatTest() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.model.dataformat.CryptoDataFormat;
                  import org.apache.camel.model.dataformat.CsvDataFormat;
                  import org.apache.camel.model.dataformat.FlatpackCsvDataFormat;
                  import org.apache.camel.model.dataformat.FlatpackDataFormat;
                  import org.apache.camel.model.dataformat.JaxbDataFormat;
                  import org.apache.camel.model.dataformat.SoapDataFormat;
                  import org.apache.camel.model.dataformat.SwiftMxDataFormat;
                  import org.apache.camel.model.dataformat.XmlSecurityDataFormat;
      
                  public class Cr {
                    public void cryptoDataFormat() {
                        CryptoDataFormat cfd = new CryptoDataFormat();
                        cfd.setAlgorithmParameterRef("111");
                        cfd.setKeyRef("222");
                        cfd.setInitVectorRef("333");

                        CsvDataFormat csv = new CsvDataFormat();
                        csv.setFormatRef("444");
                        csv.setFormatName("555");

                        new FlatpackDataFormat().setParserFactoryRef("666");

                        new JaxbDataFormat().setNamespacePrefixRef("777");

                        SoapDataFormat soap = new SoapDataFormat();;

                        soap.setNamespacePrefixRef("888");
                        soap.setElementNameStrategyRef("999");

                        SwiftMxDataFormat swift = new SwiftMxDataFormat();
                        swift.setReadConfigRef("1111");
                        swift.setWriteConfigRef("1222");
                        
//                        new XMLSecurityDataFormat().setKeyOrTrustStoreParametersRef("1333");
                    }
                  }
                  """, //there is missing a reference for proper parsing of XMLSecurityDataFormat
                """

                  import org.apache.camel.model.dataformat.CryptoDataFormat;
                  import org.apache.camel.model.dataformat.CsvDataFormat;
                  import org.apache.camel.model.dataformat.FlatpackCsvDataFormat;
                  import org.apache.camel.model.dataformat.FlatpackDataFormat;
                  import org.apache.camel.model.dataformat.JaxbDataFormat;
                  import org.apache.camel.model.dataformat.SoapDataFormat;
                  import org.apache.camel.model.dataformat.SwiftMxDataFormat;
                  import org.apache.camel.model.dataformat.XmlSecurityDataFormat;
      
                  public class Cr {
                    public void cryptoDataFormat() {
                        CryptoDataFormat cfd = new CryptoDataFormat();
                        cfd.setAlgorithmParameterSpec("111");
                        cfd.setKey("222");
                        cfd.setInitVector("333");
                        
                        CsvDataFormat csv = new CsvDataFormat();
                        csv.setFormat("444");
                        csv.setFormat("555");
                        
                        new FlatpackDataFormat().setParserFactory("666");
                        
                        new JaxbDataFormat().setNamespacePrefix("777");
                        
                        SoapDataFormat soap = new SoapDataFormat();;
                      
                        soap.setNamespacePrefix("888");
                        soap.setElementNameStrategy("999");
                        
                        SwiftMxDataFormat swift = new SwiftMxDataFormat();
                        swift.setReadConfig("1111");
                        swift.setWriteConfig("1222");
                        
//                        new XMLSecurityDataFormat().setKeyOrTrustStoreParametersRef("1333");
                    }
                  }
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_15.html#_data_formats">Data Formats</a> in Java
     */
    @Test
    void dataformatJavaDSLTest() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.builder.RouteBuilder;
                   
                  public class MyRoutes extends RouteBuilder {
                      @Override
                      public void configure() {
                          from("direct:format")
                                  .marshal(dataFormat().crypto().algorithmParameterRef("111").end())
                                  .marshal(dataFormat().crypto().keyRef("222").end())
                                  .marshal(dataFormat().crypto().initVectorRef("333").end())
                                  .marshal(dataFormat().csv().formatName("444").end())
                                  .marshal(dataFormat().csv().formatRef("555").end())
                                  .marshal(dataFormat().flatpack().parserFactoryRef("666").end())
                                  .marshal(dataFormat().jaxb().namespacePrefixRef("777").end())
                                  .marshal(dataFormat().soap().namespacePrefixRef("888").end())
                                  .marshal(dataFormat().soap().elementNameStrategyRef("999").end())
                                  .marshal(dataFormat().swiftMx().readConfigRef("1111").end())
                                  .marshal(dataFormat().swiftMx().writeConfigRef("1222").end())
                                  .marshal(dataFormat().xmlSecurity().keyOrTrustStoreParametersRef("1333").end());
                      }
                  }
                  """, //there is missing a reference for proper parsing of XMLSecurityDataFormat
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoutes extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("direct:format")
                                .marshal(dataFormat().crypto().algorithmParameterSpec("111").end())
                                .marshal(dataFormat().crypto().key("222").end())
                                .marshal(dataFormat().crypto().initVector("333").end())
                                .marshal(dataFormat().csv().format("444").end())
                                .marshal(dataFormat().csv().format("555").end())
                                .marshal(dataFormat().flatpack().parserFactory("666").end())
                                .marshal(dataFormat().jaxb().namespacePrefix("777").end())
                                .marshal(dataFormat().soap().namespacePrefix("888").end())
                                .marshal(dataFormat().soap().elementNameStrategy("999").end())
                                .marshal(dataFormat().swiftMx().readConfig("1111").end())
                                .marshal(dataFormat().swiftMx().writeConfigObject("1222").end())
                                .marshal(dataFormat().xmlSecurity().keyOrTrustStoreParameters("1333").end());
                    }
                }
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_15.html#_data_formats">Data Formats</a> in DSL
     */
    @Test
    void nettyKeyStoreTestYaml() {
        //language=yaml
        rewriteRun(yaml(
                """
                      - route:
                        id: route-3275
                        from:
                          id: from-5260
                          uri: netty
                          parameters:
                            host: localhost
                            keyStoreFile: "/testFile"
                            trustStoreFile: "/testFile"
                            port: 12345
                            protocol: tcp
                          steps:
                            id: log-1598
                            message: ${body}
                      - route:
                        id: route-3276
                        from:
                          id: from-5261
                          uri: netty-http
                          parameters:
                            host: localhost
                            keyStoreFile: "/testFile"
                            trustStoreFile: "/testFile"
                            port: 12345
                            protocol: tcp
                          steps:
                            id: log-1598
                            message: ${body}
                  """,
                """
                      - route:
                        id: route-3275
                        from:
                          id: from-5260
                          uri: netty
                          parameters:
                            host: localhost
                            keyStoreResource: "file:/testFile"
                            trustStoreResource: "file:/testFile"
                            port: 12345
                            protocol: tcp
                          steps:
                            id: log-1598
                            message: ${body}
                      - route:
                        id: route-3276
                        from:
                          id: from-5261
                          uri: netty-http
                          parameters:
                            host: localhost
                            keyStoreResource: "file:/testFile"
                            trustStoreResource: "file:/testFile"
                            port: 12345
                            protocol: tcp
                          steps:
                            id: log-1598
                            message: ${body}
                  """));
    }

    @Test
    void xmlDslLoadBalanceFailover() {
        //language=xml
        rewriteRun(xml(
                """
                  <routes>
                      <route>
                          <from uri="netty:tcp:12345?keyStoreFile=some_file"/>
                          <to uri="mock:result"/>
                      </route>
                      <route>
                          <from uri="netty:tcp:12345?trustStoreFile={{some_file}}"/>
                          <to uri="mock:result"/>
                      </route>
                      <route>
                          <from uri="netty-http:tcp:12345?anotherAttr=1\\&amp;keyStoreFile='some_file'"/>
                          <to uri="mock:result"/>
                      </route>
                      <route>
                          <from uri="netty-http:tcp:12345?trustStoreFile={{some_file}}"/>
                          <to uri="mock:result"/>
                      </route>
                      <route>
                          <from uri="wrong:tcp:12345?trustStoreFile={{some_file}}"/>
                          <to uri="mock:result"/>
                      </route>
                  </routes>
                  """,
                """
                  <routes>
                      <route>
                          <from uri="netty:tcp:12345?keyStoreResource=file:some_file"/>
                          <to uri="mock:result"/>
                      </route>
                      <route>
                          <from uri="netty:tcp:12345?trustStoreResource=file:{{some_file}}"/>
                          <to uri="mock:result"/>
                      </route>
                      <route>
                          <from uri="netty-http:tcp:12345?anotherAttr=1\\&amp;keyStoreResource=file:'some_file'"/>
                          <to uri="mock:result"/>
                      </route>
                      <route>
                          <from uri="netty-http:tcp:12345?trustStoreResource=file:{{some_file}}"/>
                          <to uri="mock:result"/>
                      </route>
                      <route>
                          <from uri="wrong:tcp:12345?trustStoreFile={{some_file}}"/>
                          <to uri="mock:result"/>
                      </route>
                  </routes>
                  """));
    }

}
//<route>
//                      <from uri="direct:start"/>
//                      <to uri="crypto:something" algorithmParameterRef='111' initVectorRef='222' keyRef='333'/>
//                      <to uri="csv" formatRef='444'/>
//                      <to uri="csv" formatName='555'/>
//                      <to uri="flatpack" parserFactoryRef='666'/>
//                      <to uri="jaxb" namespacePrefixRef='777'/>
//                      <to uri="soap" namespacePrefixRef='888' elementNameStrategyRef='999'/>
//                      <to uri="swiftMx" readConfigRef='1111' writeConfigRef='1222'/>
//                      <to uri="xmlSecurity" keyOrTrustStoreParametersRef='1333'/>
//                    </route>
