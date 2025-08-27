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
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate413Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_13)
          .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_12, "camel-http", "camel-fury",
            "camel-core-model", "camel-api"))
          .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_13.html#_camel_yaml_dsl">camel-yaml-dsl</a>
     */
    @DocumentExample
    @Test
    void yamlDsl() {
        //language=yaml
        rewriteRun(yaml(
          """
            - route:
                id: "yaml-routeConfiguration"
                on-exception:
                  - on-exception:
                      handled:
                        constant: "true"
                      exception:
                        - "org.apache.camel.core.it.routeconfigurations.RouteConfigurationsException"
                      steps:
                        - set-body:
                            constant:
                              expression: "onException has been triggered in yamlRouteConfiguration"
            """,
          """
              - route:
                  id: "yaml-routeConfiguration"
                  onException:
                    - onException:
                        handled:
                          constant: "true"
                        exception:
                          - "org.apache.camel.core.it.routeconfigurations.RouteConfigurationsException"
                        steps:
                          - setBody:
                              constant:
                                expression: "onException has been triggered in yamlRouteConfiguration"
            """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_13.html#_camel_http">camel-http</a>
     */
    @Test
    void camelHttp() {
        //language=java
        rewriteRun(java(
          """
            import org.apache.camel.component.http.BasicAuthenticationHttpClientConfigurer;

            public class HttpTest {
                public void test() {
                       BasicAuthenticationHttpClientConfigurer configurer = null;
                }
            }
            """,
          """
            import org.apache.camel.component.http.DefaultAuthenticationHttpClientConfigurer;

            public class HttpTest {
                public void test() {
                       DefaultAuthenticationHttpClientConfigurer configurer = null;
                }
            }
            """));
    }


    /**
     * Renamed camel-fury to camel-fory.
     *
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_13.html#_camel_fury">camel-fury</a>
     */
    @DisabledIfSystemProperty(named =CamelTestUtil.SYSTEM_PROPERTY_LATEST_RECIPE, matches = "true")
    @Test
    void furyDependency() {
        //language=xml
        rewriteRun(pomXml(
          """
            <project>
               <modelVersion>4.0.0</modelVersion>

               <artifactId>test</artifactId>
               <groupId>org.apache.camel.test</groupId>
               <version>1.0.0</version>

               <properties>
                   <camel.version>4.10.0</camel.version>
               </properties>

               <dependencies>
                   <dependency>
                       <groupId>org.apache.camel</groupId>
                       <artifactId>camel-api</artifactId>
                       <version>${camel.version}</version>
                   </dependency>
                   <dependency>
                       <groupId>org.apache.camel</groupId>
                       <artifactId>camel-fury</artifactId>
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
                   <camel.version>4.10.0</camel.version>
               </properties>

               <dependencies>
                   <dependency>
                       <groupId>org.apache.camel</groupId>
                       <artifactId>camel-api</artifactId>
                       <version>${camel.version}</version>
                   </dependency>
                   <!--~~(org.apache.camel:camel-fory:4.10.0 failed. Unable to download POM: org.apache.camel:camel-fory:4.10.0. Tried repositories:
            https://repo.maven.apache.org/maven2: HTTP 404)~~>--><dependency>
                       <groupId>org.apache.camel</groupId>
                       <artifactId>camel-fory</artifactId>
                       <version>${camel.version}</version>
                   </dependency>
                </dependencies>

            </project>
            """));
    }

    /**
     * Change of fury to fory in Java file.
     *
     * <a href="#https://camel.apache.org/manual/camel-4x-upgrade-guide-4_13.html#_camel_fury">camel-fury</a>
     */
    @Test
    void furyJavaType() {
        //language=java
        rewriteRun(java(
          """
            import org.apache.camel.component.fury.FuryDataFormat;

            public class FuryTest {
                public void test() {
                       FuryDataFormat fury = new FuryDataFormat();
                }
            }
            """,
          """
            import org.apache.camel.component.fory.ForyDataFormat;

            public class FuryTest {
                public void test() {
                       ForyDataFormat fury = new ForyDataFormat();
                }
            }
            """));
    }

    /**
     * Change of fury to fory in yaml dsl
     *
     * <a href="#https://camel.apache.org/manual/camel-4x-upgrade-guide-4_13.html#_camel_fury">camel-fury</a>
     */
    @Test
    void furyYamlDsl() {
        //language=yaml
        rewriteRun(yaml(
          """
            - from:
                uri: file:inbox/xml
                steps:
                  - unmarshal:
                      fury: {}
                  - to:
                      uri: bean:validateOrder
                  - marshal:
                      fury: {}
                  - to:
                      uri: jms:queue:order
            """,
          """
            - from:
                uri: file:inbox/xml
                steps:
                  - unmarshal:
                      fory: {}
                  - to:
                      uri: bean:validateOrder
                  - marshal:
                      fory: {}
                  - to:
                      uri: jms:queue:order
            """));
    }


    /**
     * Change of fury to fory in xml dsl
     *
     * <a href="#https://camel.apache.org/manual/camel-4x-upgrade-guide-4_13.html#_camel_fury">camel-fury</a>
     */
    @Test
    void furyXmlDsl() {
        //language=xml
        rewriteRun(xml(
          """
            <camelContext>

              <!-- Define the Fury data format -->
              <dataFormats>
                <fury id="fury"/>
              </dataFormats>

              <route>
                <from uri="direct:marshalFury"/>
                <marshal ref="fury"/>
                <to uri="file:data/output?fileName=data.fury"/>
              </route>

              <route>
                <from uri="file:inbox/xml"/>
                <unmarshal><fury/></unmarshal>
                <to uri="bean:validateOrder"/>
                <marshal><fury/></marshal>
                <to uri="jms:queue:order"/>
              </route>

            </camelContext>
            """,
          """
            <camelContext>

              <!-- Define the Fury data format -->
              <dataFormats>
                <fory id="fury"/>
              </dataFormats>

              <route>
                <from uri="direct:marshalFury"/>
                <marshal ref="fury"/>
                <to uri="file:data/output?fileName=data.fury"/>
              </route>

              <route>
                <from uri="file:inbox/xml"/>
                <unmarshal><fory/></unmarshal>
                <to uri="bean:validateOrder"/>
                <marshal><fory/></marshal>
                <to uri="jms:queue:order"/>
              </route>

            </camelContext>
            """));
    }

    /**
     * Change of fury to fory in java dsl
     *
     * <a href="#https://camel.apache.org/manual/camel-4x-upgrade-guide-4_13.html#_camel_fury">camel-fury</a>
     */
    @Test
    void furyJavaDsl() {
        //language=java
        rewriteRun(java(
          """
            import org.apache.camel.builder.RouteBuilder;

            public class FuryRoute extends RouteBuilder {
                @Override
                public void configure() throws Exception {
                    from("file:inbox/xml")
                        .unmarshal().fury()
                        .to("bean:validateOrder")
                        .marshal().fury()
                        .to("jms:queue:order");

                    from("file:inbox/xml")
                        .unmarshal().fury(String.class)
                        .to("bean:validateOrder")
                        .marshal().fury(String.class)
                        .to("jms:queue:order");
                }
            }
            """,
          """
            import org.apache.camel.builder.RouteBuilder;

            public class FuryRoute extends RouteBuilder {
                @Override
                public void configure() throws Exception {
                    from("file:inbox/xml")
                        .unmarshal().fory()
                        .to("bean:validateOrder")
                        .marshal().fory()
                        .to("jms:queue:order");

                    from("file:inbox/xml")
                        .unmarshal().fory(String.class)
                        .to("bean:validateOrder")
                        .marshal().fory(String.class)
                        .to("jms:queue:order");
                }
            }
            """));
    }
}
