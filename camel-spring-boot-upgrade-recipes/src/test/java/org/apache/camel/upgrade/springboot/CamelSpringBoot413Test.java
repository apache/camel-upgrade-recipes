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
package org.apache.camel.upgrade.springboot;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.config.Environment;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class CamelSpringBoot413Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(Environment.builder()
                .scanYamlResources()
                .build()
                .activateRecipes("org.apache.camel.upgrade.camel413.CamelSpringBootMigrationRecipe"));
    }

    @DocumentExample
    @Test
    void testProperties() {
        rewriteRun(
                //both org.apache.camel.upgrade.camel413.CamelSpringBootMigrationRecipe and
                // org.apache.camel.upgrade.UpdateCamelSpringBootPropertiesAndYamlKeys go modifies this properties
                spec -> spec.expectedCyclesThatMakeChanges(2),
            properties(
                    """
                      camel.springboot.main-run-controller=something
                      camel.springboot.include-non-singletons=something
                      camel.springboot.warn-on-early-shutdown=something
                      """,
                    """
                      camel.main.run-controller=something
                      camel.main.include-non-singletons=something
                      camel.main.warn-on-early-shutdown=something
                      """
            )
        );
    }

    @Test
    void yamlFile() {
        rewriteRun(
                //both org.apache.camel.upgrade.camel413.CamelSpringBootMigrationRecipe and
                // org.apache.camel.upgrade.UpdateCamelSpringBootPropertiesAndYamlKeys modifies this properties
                spec -> spec.expectedCyclesThatMakeChanges(2),
            yaml(
                    """
                      camel:
                        springboot:
                          main-run-controller: something
                          name: 'Foo'
                          routeControllerBackOffDelay: true
                        main:
                          routeControllerBackOffMultiplier: 5
                      another:
                        ignored:
                          property: true
                      """,
                    """
                        camel:
                          main:
                            name: 'Foo'
                          routecontroller.backOffMultiplier: 5
                          routecontroller.backOffDelay: true
                          main.runController: something
                        another:
                          ignored:
                            property: true
                      """
            )
        );
    }

}
