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

import org.openrewrite.Recipe;
import org.openrewrite.properties.ChangePropertyKey;
import org.openrewrite.yaml.ChangeKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recipe for updating Apache Camel configuration properties and YAML keys.
 * <p>
 * This recipe handles the migration of Apache Camel configuration keys according to updated
 * naming conventions while preserving their values. It focuses on three main types of changes:
 * <ul>
 *   <li>Converting 'camel.springboot.*' prefixes to 'camel.main.*'</li>
 *   <li>Converting 'camel.routeController.*' to lowercase 'camel.routecontroller.*'</li>
 *   <li>Moving tracing-related properties from 'camel.main' to 'camel.trace'</li>
 *   <li>Preserving specific properties that should keep the 'springboot' prefix</li>
 * </ul>
 *
 */
public class CamelPropertiesAndYamlUpdate extends Recipe {

    @Override
    public String getDisplayName() {
        return "Change Apache Camel properties key";
    }

    @Override
    public String getDescription() {
        return "Change Apache Camel properties and YAML keys leaving the value intact.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        List<Recipe> recipes = new ArrayList<>();
        // camel.springboot.* to camel.main.* updates
        UpdateSpringBootToMain(recipes);

        // camel.routeController.* to camel.routecontroller.* updates
        routeController(recipes);
        // camel.main to camel.trace
        trace(recipes);

        // Some properties need the springboot prefix, let's fix those
        FixMainToSpringBoot(recipes);

        return recipes;
    }

    private void trace(List<Recipe> recipes) {
        fullConfigurationUpdate(recipes, Map.of(
                "camel.main.backlogTracing", "camel.trace.enabled",
                "camel.main.backlogTracingStandby", "camel.trace.backlogTracingStandby",
                "camel.main.backlogTracingTemplates", "camel.trace.backlogTracingTemplates"));
    }

    private void routeController(List<Recipe> recipes) {
        Map<String, String> oldNewProperties = new HashMap<>();
        // Special configurations
        oldNewProperties.putAll(Map.of(
                "camel.main.routeControllerSuperviseEnabled", "camel.routecontroller.enabled",
                "camel.springboot.routeControllerSuperviseEnabled", "camel.routecontroller.enabled",
                "camel.main.backlogTracing", "camel.trace.enabled",
                "camel.main.backlogTracingStandby", "camel.trace.backlogTracingStandby",
                "camel.main.backlogTracingTemplates", "camel.trace.backlogTracingTemplates"
        ));
        // Common configurations
        oldNewProperties.putAll(Map.of(
                "camel.main.routeControllerInitialDelay", "camel.routecontroller.initialDelay",
                "camel.main.routeControllerBackOffDelay", "camel.routecontroller.backOffDelay",
                "camel.main.routeControllerBackOffMaxAttempts", "camel.routecontroller.backOffMaxAttempts",
                "camel.main.routeControllerBackOffMaxDelay", "camel.routecontroller.backOffMaxDelay",
                "camel.main.routeControllerBackOffMaxElapsedTime", "camel.routecontroller.backOffMaxElapsedTime",
                "camel.main.routeControllerBackOffMultiplier", "camel.routecontroller.backOffMultiplier",
                "camel.main.routeControllerIncludeRoutes", "camel.routecontroller.includeRoutes",
                "camel.main.routeControllerExcludeRoutes", "camel.routecontroller.excludeRoutes",
                "camel.main.routeControllerThreadPoolSize", "camel.routecontroller.threadPoolSize"
                )
        );

        // sometimes, backoff is mispelled.....
        oldNewProperties.putAll(Map.of(
                "camel.main.routeControllerBackoffDelay", "camel.routecontroller.backOffDelay",
                "camel.main.routeControllerBackoffMaxAttempts", "camel.routecontroller.backOffMaxAttempts",
                "camel.main.routeControllerBackoffMaxDelay", "camel.routecontroller.backOffMaxDelay",
                "camel.main.routeControllerBackoffMaxElapsedTime", "camel.routecontroller.backOffMaxElapsedTime",
                "camel.main.routeControllerBackoffMultiplier", "camel.routecontroller.backOffMultiplier"
        ));

        fullConfigurationUpdate(recipes, oldNewProperties);
    }

    private static void fullConfigurationUpdate(List<Recipe> recipes, Map<String, String> oldNewProperties) {
        for (Map.Entry<String, String> entry : oldNewProperties.entrySet()) {
            recipes.add(
                    new ChangePropertyKey(entry.getKey(), entry.getValue(),
                            null, null)
            );
            recipes.add(
                    new org.openrewrite.yaml.ChangePropertyKey(
                            entry.getKey(), entry.getValue(), null, null, null)
            );
        }
    }

    private void FixMainToSpringBoot(List<Recipe> recipes) {
        fullConfigurationUpdate(recipes, Map.of(
                "camel.main.main-run-controller", "camel.springboot.main-run-controller",
                "camel.main.include-non-singletons", "camel.springboot.include-non-singletons",
                "camel.main.warn-on-early-shutdown", "camel.springboot.warn-on-early-shutdown",
                "camel.main.mainRunController", "camel.springboot.main-run-controller",
                "camel.main.includeNonSingletons", "camel.springboot.include-non-singletons",
                "camel.main.warnOnEarlyShutdown", "camel.springboot.warn-on-early-shutdown"
        ));
    }

    private static void UpdateSpringBootToMain(List<Recipe> recipes) {
        recipes.add(
                new PatternMatcherChangePropertyKey(
                        "camel.springboot", "camel.main", List.of(
                        "camel.springboot.main-run-controller",
                        "camel.springboot.include-non-singletons",
                        "camel.springboot.warn-on-early-shutdown"
                ))
        );

        recipes.add(
                new ChangeKey("$.camel.springboot", "main")
        );
    }
}
