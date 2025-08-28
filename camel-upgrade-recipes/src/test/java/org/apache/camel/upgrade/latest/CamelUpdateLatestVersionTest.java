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
package org.apache.camel.upgrade.latest;

import org.apache.camel.upgrade.CamelTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.maven.Assertions.pomXml;

@EnabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = "org.apache.camel.upgrade.CamelMigrationRecipe")
//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdateLatestVersionTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_9)
          .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_10, "camel-core-model", "camel-api"))
          .typeValidationOptions(TypeValidation.none());
    }

     /**
     * Check that the version is pom is upgraded to the latest 4.10lts version
     */
    @Test
    void finalVersion() {

        String result =
                """
            <project>
               <modelVersion>4.0.0</modelVersion>

               <artifactId>test</artifactId>
               <groupId>org.apache.camel.test</groupId>
               <version>1.0.0</version>

               <properties>
                  <camel.version>%s</camel.version>
                  <maven.compiler.release>17</maven.compiler.release>
               </properties>
      
               <dependencies>
                   <dependency>
                       <groupId>org.apache.camel</groupId>
                       <artifactId>camel-api</artifactId>
                       <version>${camel.version}</version>
                   </dependency>
                 </dependencies>
            </project>
            """.formatted(System.getProperty("camel-version"));
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
                 </dependencies>
            </project>
            """,
                result));
    }
}
