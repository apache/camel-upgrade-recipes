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

import org.apache.camel.upgrade.camel419.Pom419TestInfraRecipe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate419Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_19)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_18,
                        "camel-core-model", "camel-api", "camel-qdrant", "camel-tahu", "tahu-host"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_18.html#_camel_bom">camel-bom</a>
     *
     * This test verifies both the removal of camel-test dependency (4.19 specific)
     * and version property upgrades. It combines the 4.19 migration recipe with
     * the internal CamelToTheLatestRecipe for version upgrades.
     */
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    @DocumentExample
    @Test
    void camelBom() {
        //language=xml
        rewriteRun(
            pomXml(
                """
                  <project>
                     <modelVersion>4.0.0</modelVersion>
      
                     <artifactId>test</artifactId>
                     <groupId>org.apache.camel.test</groupId>
                     <version>1.0.0</version>
      
                     <properties>
                         <camel.version>3.21.0</camel.version>
                     </properties>
      
                     <dependencies>
                         <dependency>
                             <groupId>org.apache.camel</groupId>
                             <artifactId>camel-api</artifactId>
                             <version>${camel.version}</version>
                         </dependency>
                         <dependency>
                             <groupId>org.apache.camel</groupId>
                             <artifactId>camel-test</artifactId>
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
                         <camel.version>3.21.0</camel.version>
                     </properties>
      
                     <dependencies>
                         <dependency>
                             <groupId>org.apache.camel</groupId>
                             <artifactId>camel-api</artifactId>
                             <version>${camel.version}</version>
                         </dependency>
                      </dependencies>
      
                  </project>
                  """
            ));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_saga_eip">Saga EIP</a>
     */
    @Test
    void sagaEipXml() {
        //language=xml
        rewriteRun(xml(
          """
            <route>
                <from uri="direct:start"/>
                <saga sagaService="mySagaService">
                    <compensation uri="mock:compensation"/>
                    <completion uri="mock:completion"/>
                    <option key="myOptionKey">
                        <constant>myOptionValue</constant>
                    </option>
                    <option key="myOptionKey2">
                        <constant>myOptionValue2</constant>
                    </option>
                </saga>
                <choice>
                    <when>
                        <simple>${body} == 'fail'</simple>
                        <throwException exceptionType="java.lang.RuntimeException" message="fail"/>
                    </when>
                </choice>
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
                    <option key="myOptionKey2">
                        <constant>myOptionValue2</constant>
                    </option>
                </saga>
                <choice>
                    <when>
                        <simple>${body} == 'fail'</simple>
                        <throwException exceptionType="java.lang.RuntimeException" message="fail"/>
                    </when>
                </choice>
                <to uri="mock:end"/>
            </route>
            """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_saga_eip">Saga EIP</a>
     */
    @Test
    void sagaEipYaml() {
        //language=yaml
        rewriteRun(yaml(
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
            """));
    }


    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_camel_test_infra">camel-test-infra</a>
     */
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    @Test
    void infraTestJarRemoval() {
        //language=xml
        rewriteRun(spec -> spec.recipe(new Pom419TestInfraRecipe()),
                pomXml(
                        """
                          <project>
                              <groupId>com.example</groupId>
                              <artifactId>test-project</artifactId>
                              <version>1.0</version>
                              <dependencies>
                                  <dependency>
                                      <groupId>org.apache.camel</groupId>
                                      <artifactId>camel-test-infra-kafka</artifactId>
                                      <version>4.18.0</version>
                                      <type>test-jar</type>
                                      <scope>test</scope>
                                  </dependency>
                                  <dependency>
                                      <groupId>org.apache.camel</groupId>
                                      <artifactId>camel-test-infra-common</artifactId>
                                      <version>4.18.0</version>
                                      <type>test-jar</type>
                                      <scope>test</scope>
                                  </dependency>
                                  <dependency>
                                      <groupId>org.apache.camel</groupId>
                                      <artifactId>camel-core</artifactId>
                                      <version>4.18.0</version>
                                  </dependency>
                              </dependencies>
                          </project>
                          """,
                        """
                          <project>
                              <groupId>com.example</groupId>
                              <artifactId>test-project</artifactId>
                              <version>1.0</version>
                              <dependencies>
                                  <dependency>
                                      <groupId>org.apache.camel</groupId>
                                      <artifactId>camel-test-infra-kafka</artifactId>
                                      <version>4.18.0</version>
                                      <scope>test</scope>
                                  </dependency>
                                  <dependency>
                                      <groupId>org.apache.camel</groupId>
                                      <artifactId>camel-test-infra-common</artifactId>
                                      <version>4.18.0</version>
                                      <scope>test</scope>
                                  </dependency>
                                  <dependency>
                                      <groupId>org.apache.camel</groupId>
                                      <artifactId>camel-core</artifactId>
                                      <version>4.18.0</version>
                                  </dependency>
                              </dependencies>
                          </project>
                          """
                ));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_camel_yaml_io_camel_xml_io">YAML DSL</a>
     */
    @Test
    void yamlDslRoutePolicyRename() {
        //language=yaml
        rewriteRun(yaml(
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
            """));
    }
    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_18.html#_camel_bom">camel-bom</a>
     */
    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    @Test
    void removedComponents() {
        //language=xml
        rewriteRun(pomXml(
                """
                  <project>
                     <modelVersion>4.0.0</modelVersion>
      
                     <artifactId>test</artifactId>
                     <groupId>org.apache.camel.test</groupId>
                     <version>1.0.0</version>
      
                     <properties>
                         <camel.version>4.18.0</camel.version>
                     </properties>
      
                     <dependencies>
                         <dependency>
                             <groupId>org.apache.camel</groupId>
                             <artifactId>camel-api</artifactId>
                             <version>${camel.version}</version>
                         </dependency>
                         <dependency>
                             <groupId>org.apache.camel</groupId>
                             <artifactId>camel-cloud</artifactId>
                             <version>${camel.version}</version>
                         </dependency>
                         <dependency>
                             <groupId>org.apache.camel</groupId>
                             <artifactId>camel-service</artifactId>
                             <version>${camel.version}</version>
                         </dependency>
                         <dependency>
                             <groupId>org.apache.camel</groupId>
                             <artifactId>camel-nitrite</artifactId>
                             <version>${camel.version}</version>
                         </dependency>
                         <dependency>
                             <groupId>org.apache.camel</groupId>
                             <artifactId>camel-torchserve</artifactId>
                             <version>${camel.version}</version>
                         </dependency>
                         <dependency>
                             <groupId>org.apache.camel</groupId>
                             <artifactId>camel-test-infra-torchserve</artifactId>
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
                         <camel.version>4.18.0</camel.version>
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
