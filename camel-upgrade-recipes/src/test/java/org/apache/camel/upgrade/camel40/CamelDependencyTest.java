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
package org.apache.camel.upgrade.camel40;

import org.apache.camel.upgrade.CamelTestUtil;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.maven.Assertions.pomXml;

/**
 * <a href="https://camel.apache.org/manual/camel-4-migration-guide.html#_removed_components">Removed components</a>
 */
public class CamelDependencyTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_0)
          .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v3_18,
            "camel-api"));
    }

    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    @ParameterizedTest
    @CsvSource({
      "camel-swagger-java,       camel-openapi-java",
      "camel-rest-swagger,       camel-rest-openapi",
      "camel-directvm,           camel-direct",
      "camel-dozer,              camel-mapstruct",
      "camel-elasticsearch-rest, camel-elasticsearch",
      "camel-rabbitmq,           camel-spring-rabbitmq",
      "camel-websocket,          camel-vertx-websocket",
      "camel-websocket-jsr356,   camel-vertx-websocket",
      "camel-vertx-kafka,        camel-kafka",
      "camel-vm,                 camel-seda",
      "camel-xstream,            camel-jacksonxml"
    })
    void renamedDependencies(String oldArtifact, String newArtifact) {
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
                       <artifactId>%s</artifactId>
                       <version>3.20.0</version>
                       <exclusions>
                           <exclusion>
                               <groupId>*</groupId>
                               <artifactId>*</artifactId>
                           </exclusion>
                       </exclusions>
                   </dependency>
               </dependencies>

            </project>
            """.formatted(oldArtifact),
          """
            <project>
               <modelVersion>4.0.0</modelVersion>

               <artifactId>test</artifactId>
               <groupId>org.apache.camel.test</groupId>
               <version>1.0.0</version>

               <dependencies>
                   <dependency>
                       <groupId>org.apache.camel</groupId>
                       <artifactId>%s</artifactId>
                       <version>3.20.0</version>
                       <exclusions>
                           <exclusion>
                               <groupId>*</groupId>
                               <artifactId>*</artifactId>
                           </exclusion>
                       </exclusions>
                   </dependency>
               </dependencies>

            </project>
            """.formatted(newArtifact)));
    }

    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    @ParameterizedTest
    @ValueSource(strings = {
      "camel-any23",
      "camel-atlasmap",
      "camel-atmos",
      "camel-corda",
      "camel-gora",
      "camel-hbase",
      "camel-hyperledger-aries",
      "camel-iota",
      "camel-ipfs",
      "camel-jbpm",
      "camel-jclouds",
      "camel-spark",
      "camel-spring-integration",
      "camel-weka"
    })
    void removedDependencies(String removedArtifact) {
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
                       <artifactId>camel-core</artifactId>
                       <version>3.20.0</version>
                   </dependency>
                   <dependency>
                       <groupId>org.apache.camel</groupId>
                       <artifactId>%s</artifactId>
                       <version>3.20.0</version>
                       <exclusions>
                           <exclusion>
                               <groupId>*</groupId>
                               <artifactId>*</artifactId>
                           </exclusion>
                       </exclusions>
                   </dependency>
               </dependencies>

            </project>
            """.formatted(removedArtifact),
          """
            <project>
               <modelVersion>4.0.0</modelVersion>

               <artifactId>test</artifactId>
               <groupId>org.apache.camel.test</groupId>
               <version>1.0.0</version>

               <dependencies>
                   <dependency>
                       <groupId>org.apache.camel</groupId>
                       <artifactId>camel-core</artifactId>
                       <version>3.20.0</version>
                   </dependency>
               </dependencies>

            </project>
            """));
    }

}
