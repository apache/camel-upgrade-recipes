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
import static org.openrewrite.maven.Assertions.pomXml;

public class CamelUpdate47Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_7)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_6,
                        "camel-base-engine", "camel-api", "http-common", "camel-undertow", "jakarta.servlet-api-6.0.0"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_7.html#_api_changes">API CHANGES</a>
     */
    @Test
    public void testApiChanges() {
        //language=java
        rewriteRun(java(
                """
                             import org.apache.camel.impl.engine.TransformerKey;
                             import org.apache.camel.impl.engine.ValidatorKey;
                             
                             
                             public class ApisTest {
                                 public void test() {
                                     TransformerKey transformerKey;
                                     ValidatorKey validatorKey;
                                 }
                             }
                        """,
                """
                       
                           import org.apache.camel.spi.TransformerKey;
                           import org.apache.camel.spi.ValidatorKey;
                      
                         
                           public class ApisTest {
                               public void test() {
                                   TransformerKey transformerKey;
                                   ValidatorKey validatorKey;
                               }
                           }
                        """));
    }


    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_7.html#_dsl">xml DSL loadbalancer failover</a>
     */
    @Test
    public void testXmlDslLoadBalanceFailover() {
        //language=xml
        rewriteRun(xml("""
                <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
                    <jmxAgent id="agent" disabled="true"/>
            
                    <route>
                        <from uri="direct:start"/>
                        <loadBalance>
                            <failover/>
                            <to uri="jms:queue:foo?transferException=true"/>
                            <to uri="jms:queue:bar?transferException=true"/>
                        </loadBalance>
                        <to uri="mock:result"/>
                    </route>
            
            
                </camelContext>
                """, """
                <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
                    <jmxAgent id="agent" disabled="true"/>
            
                    <route>
                        <from uri="direct:start"/>
                        <loadBalance>
                            <failoverLoadBalancer/>
                            <to uri="jms:queue:foo?transferException=true"/>
                            <to uri="jms:queue:bar?transferException=true"/>
                        </loadBalance>
                        <to uri="mock:result"/>
                    </route>
            
            
                </camelContext>
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_7.html#_dsl">xml DSL loadbalancer random</a>
     */
    @Test
    public void testXmlDslLoadBalanceRandom() {
        //language=xml
        rewriteRun(xml("""
            <route>
              <from uri="direct:start"/>
              <loadBalance>
                  <random/>
                  <to uri="mock:x"/>
                  <to uri="mock:y"/>
                  <to uri="mock:z"/>
              </loadBalance>
            </route>
                """, """
            <route>
              <from uri="direct:start"/>
              <loadBalance>
                  <randomLoadBalancer/>
                  <to uri="mock:x"/>
                  <to uri="mock:y"/>
                  <to uri="mock:z"/>
              </loadBalance>
            </route>
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_7.html#_dsl">xml DSL loadbalancer random</a>
     */
    @Test
    public void testXmlDslLoadBalanceRandomKeep() {
        //language=xml
        rewriteRun(xml("""
                  <random/>
                """));
    }


    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_7.html#_dsl">xml DSL loadbalancer the rest</a>
     */
    @Test
    public void testXmlDslLoadBalanceTheRest() {
        //language=xml
        rewriteRun(xml("""
              <camelContext xmlns="http://camel.apache.org/schema/spring">
                <jmxAgent id="jmx" disabled="true"/>
                <route>
                  <from uri="direct:start"/>
                  <loadBalance>
                      <roundRobinLoadBalancer/>
                      <sticky>
                          <correlationExpression><header>foo</header></correlationExpression>
                      </sticky>
                      <topic/>
                      <weighted roundRobin="false" distributionRatio="4, 2, 1" />
                  </loadBalance>
                </route>
              </camelContext>
                """, """
              <camelContext xmlns="http://camel.apache.org/schema/spring">
                <jmxAgent id="jmx" disabled="true"/>
                <route>
                  <from uri="direct:start"/>
                  <loadBalance>
                      <roundRobinLoadBalancer/>
                      <stickyLoadBalancer>
                          <correlationExpression><header>foo</header></correlationExpression>
                      </stickyLoadBalancer>
                      <topicLoadBalancer/>
                      <weightedLoadBalancer roundRobin="false" distributionRatio="4, 2, 1" />
                  </loadBalance>
                </route>
              </camelContext>
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_7.html#_dsl">xml DSL loadbalancer failover</a>
     */
    @Test
    public void testYamlLoadBalanceFailover() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                - route:
                    id: loadbalance-failover-route
                    from:
                      uri: "direct://loadbalance-failover"
                      steps:
                        - log:
                            message: "Processing message start: ${body}"
                        - loadBalance:
                            failover:
                              exception:
                                - "org.apache.camel.quarkus.main.MyException"
                            steps:
                              - to:
                                  id: to1
                                  uri: "direct:failover-1"
                              - to:
                                  id: to2
                                  uri: "direct:failover-2"
                """, """
                - route:
                    id: loadbalance-failover-route
                    from:
                      uri: "direct://loadbalance-failover"
                      steps:
                        - log:
                            message: "Processing message start: ${body}"
                        - loadBalance:
                            failoverLoadBalancer:
                              exception:
                                - "org.apache.camel.quarkus.main.MyException"
                            steps:
                              - to:
                                  id: to1
                                  uri: "direct:failover-1"
                              - to:
                                  id: to2
                                  uri: "direct:failover-2"
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_7.html#_dsl">xml DSL loadbalancer the rest</a>
     */
    @Test
    public void testYamlLoadBalanceTheRest() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                - route:
                    id: loadbalance-failover-route
                    from:
                      uri: "direct://loadbalance-failover"
                      steps:
                        - log:
                            message: "Processing message start: ${body}"
                        - loadBalance:
                            failover: aaa
                            roundRobinLoadBalancer: bbb 
                            sticky: ccc
                            topic: ddd
                            weighted: eee
                """, """
                - route:
                    id: loadbalance-failover-route
                    from:
                      uri: "direct://loadbalance-failover"
                      steps:
                        - log:
                            message: "Processing message start: ${body}"
                        - loadBalance:
                            failoverLoadBalancer: aaa
                            roundRobinLoadBalancer: bbb
                            stickyLoadBalancer: ccc
                            topicLoadBalancer: ddd
                            weightedLoadBalancer: eee
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_7.html#_api_changes">API CHANGES</a>
     */
    @Test
    public void testHttp() {
        //language=java
        rewriteRun(java(
                """
                        import jakarta.servlet.http.HttpServletRequest;
                        import jakarta.servlet.http.HttpServletResponse;
                        import org.apache.camel.Exchange;
                        
                        public class HttpTest {
                        
                            public void test(Exchange exchange) {
                                HttpServletRequest request = exchange.getIn().getHeader(Exchange.HTTP_SERVLET_REQUEST, HttpServletRequest.class);
                                HttpServletResponse response = exchange.getIn().getHeader(Exchange.HTTP_SERVLET_RESPONSE, HttpServletResponse.class);
                            }
                        }
                        """,
                """
                        import jakarta.servlet.http.HttpServletRequest;
                        import jakarta.servlet.http.HttpServletResponse;
                        import org.apache.camel.Exchange;
                        import org.apache.camel.http.common.HttpMessage;
                        
                        public class HttpTest {
                        
                            public void test(Exchange exchange) {
                                HttpServletRequest request = exchange.getMessage(HttpMessage.class).getRequest();
                                HttpServletResponse response = exchange.getMessage(HttpMessage.class).getResponse();
                            }
                        }
                        """));
    }


    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_7.html#_camel_cloudevents">CAMEL-CLOUDEVENTS</a>
     */
    @Test
    public void testCloudEvents() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                           <modelVersion>4.0.0</modelVersion>

                           <artifactId>test</artifactId>
                           <groupId>org.apache.camel.test</groupId>
                           <version>1.0.0</version>

                           <properties>
                               <camel.version>4.6.0</camel.version>
                           </properties>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-api</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-cloudevents</artifactId>
                                   <version>${camel.version}</version>
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

                           <properties>
                               <camel.version>4.6.0</camel.version>
                           </properties>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-api</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                            </dependencies>

                        </project>
                        """));
    }




}
