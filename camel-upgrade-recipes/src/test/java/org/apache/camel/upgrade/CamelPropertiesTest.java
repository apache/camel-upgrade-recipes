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
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

public class CamelPropertiesTest implements RewriteTest {

    @Test
    void propertiesFile() {
        rewriteRun(
                recipeSpec -> recipeSpec.recipes(
                        new CamelPropertiesAndYamlUpdate()),
                properties(
                        """
                          camel.main.routeControllerSuperviseEnabled=true
                          another.ignored.property=true
                          camel.springboot.name = Foo
                          camel.springboot.main-run-controller=Should be ignored!
                          camel.main.routeControllerBackOffMultiplier=5
                          camel.springboot.routeControllerInitialDelay = 5000
                          camel.springboot.routeControllerBackoffDelay = 5000
                          camel.springboot.routeControllerBackoffMaxAttempts = 10
                          """,
                        """
                          camel.routecontroller.enabled=true
                          another.ignored.property=true
                          camel.main.name = Foo
                          camel.springboot.main-run-controller=Should be ignored!
                          camel.routecontroller.backOffMultiplier=5
                          camel.routecontroller.initialDelay = 5000
                          camel.routecontroller.backOffDelay = 5000
                          camel.routecontroller.backOffMaxAttempts = 10
                          """
                )
        );
    }

    @Test
    void yamlFile() {
        rewriteRun(
                recipeSpec -> recipeSpec.recipes(
                        new CamelPropertiesAndYamlUpdate()),
                yaml(
                        """
                          camel:
                            springboot:
                              main-run-controller: 'Should be ignored!'
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
                            springboot.main-run-controller: 'Should be ignored!'
                          another:
                            ignored:
                              property: true
                          """
                )
        );
    }


}
