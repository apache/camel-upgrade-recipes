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

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate414Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_14)
          .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_13, "camel-base-engine", "camel-azure-files"))
          .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_14.html#_camel_main">doc</a>
     */
    @DocumentExample
    @Test
    void httpBusinessVsManagementServicesSeparationPropertiesTest() {
        rewriteRun(properties(
                """
                          camel.server.devConsoleEnabled=true
                          camel.server.healthCheckEnabled=true
                          camel.server.jolokiaEnabled=true
                          camel.server.metricsEnabled=true
                          camel.server.uploadEnabled=true
                          camel.server.uploadSourceDir=aaa
                          camel.server.downloadEnabled=true
                          camel.server.sendEnabled=true
                          camel.server.healthPath=aaa
                          camel.server.jolokiaPath=aaa
                        """,
                """
                            camel.management.devConsoleEnabled=true
                            camel.management.healthCheckEnabled=true
                            camel.management.jolokiaEnabled=true
                            camel.management.metricsEnabled=true
                            camel.management.uploadEnabled=true
                            camel.management.uploadSourceDir=aaa
                            camel.management.downloadEnabled=true
                            camel.management.sendEnabled=true
                            camel.management.healthPath=aaa
                            camel.management.jolokiaPath=aaa
                        """));
    }
}
