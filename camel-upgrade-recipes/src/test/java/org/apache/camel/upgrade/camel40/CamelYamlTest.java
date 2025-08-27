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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.yaml.Assertions.yaml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelYamlTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_0)
          .typeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void stepsToFrom1() {
        //language=yaml
        rewriteRun(yaml(
          """
            route:
              from:
                uri: "direct:info"
              steps:
                log: "message"
            """,
          """
            route:
              from:
                uri: "direct:info"
                steps:
                  log: "message"
            """));
    }

    @Test
    void stepsToFrom2() {
        //language=yaml
        rewriteRun(yaml(
          """
            from:
              uri: "direct:info"
            steps:
              log: "message"
            """,
          """
            from:
              uri: "direct:info"
              steps:
                log: "message"
            """));
    }

    @Test
    void stepsToFrom3() {
        //language=yaml
        rewriteRun(yaml(
          """
            - from:
                uri: "direct:start"
              steps:
              - filter:
                  expression:
                    simple: "${in.header.continue} == true"
                  steps:
                    - to:
                        uri: "log:filtered"
              - to:
                  uri: "log:original"
            """,
          """
              - from:
                  uri: "direct:start"
                  steps:
                    - filter:
                        expression:
                          simple: "${in.header.continue} == true"
                        steps:
                          - to:
                              uri: "log:filtered"
                    - to:
                        uri: "log:original"
            """));
    }

    @DisabledIfSystemProperty(named = CamelTestUtil.SYSTEM_PROPERTY_LATEST_RECIPE, matches = "true")
    @Test
    void routeConfigurationWithOnException() {
        //language=yaml
        rewriteRun(yaml(
          """
            - route-configuration:
                - id: "yamlRouteConfiguration"
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
              - route-configuration:
                  id: "yamlRouteConfiguration"
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
            """));
    }

    @DisabledIfSystemProperty(named = CamelTestUtil.SYSTEM_PROPERTY_LATEST_RECIPE, matches = "true")
    @Test
    void routeConfigurationWithoutOnException() {
        //language=yaml
        rewriteRun(yaml(
          """
            - route-configuration:
                - id: "__id"
            """,
          """
              - route-configuration:
                  id: "__id"
            """));
    }

    @DisabledIfSystemProperty(named =CamelTestUtil.SYSTEM_PROPERTY_LATEST_RECIPE, matches = "true")
    @Test
    void doubleDocument() {
        //language=yaml
        rewriteRun(yaml(
          """
            - route-configuration:
                - id: "yamlRouteConfiguration1"
                - on-exception:
                    handled:
                      constant: "true"
                    exception:
                      - "org.apache.camel.core.it.routeconfigurations.RouteConfigurationsException"
                    steps:
                      - set-body:
                          constant:
                              expression: "onException has been triggered in yamlRouteConfiguration"
            ---
            - route-configuration:
                - id: "yamlRouteConfiguration2"
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
              - route-configuration:
                  id: "yamlRouteConfiguration1"
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
              ---
              - route-configuration:
                  id: "yamlRouteConfiguration2"
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
            """));
    }

    @DisabledIfSystemProperty(named =CamelTestUtil.SYSTEM_PROPERTY_LATEST_RECIPE, matches = "true")
    @Test
    void doubleDocumentSimple() {
        //language=yaml
        rewriteRun(yaml(
          """
            - route-configuration:
                - id: "__id1"
            ---
            - route-configuration:
                - id: "__id2"
            """,
          """
              - route-configuration:
                  id: "__id1"
              ---
              - route-configuration:
                  id: "__id2"
            """));
    }

    @DisabledIfSystemProperty(named =CamelTestUtil.SYSTEM_PROPERTY_LATEST_RECIPE, matches = "true")
    @Test
    void routeConfigurationIdempotent() {
        //language=yaml
        rewriteRun(yaml(
          """
              - route-configuration:
                  id: "yamlRouteConfiguration"
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
            """));
    }
}
