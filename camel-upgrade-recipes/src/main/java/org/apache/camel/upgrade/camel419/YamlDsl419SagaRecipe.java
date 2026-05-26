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
package org.apache.camel.upgrade.camel419;

import org.apache.camel.upgrade.AbstractCamelYamlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.tree.Yaml;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_saga_eip">Saga EIP</a>
 * </p>
 * Changed model for configuring completion and compensation URIs in YAML DSL.
 * Flattens nested uri fields to direct attribute values.
 */
public class YamlDsl419SagaRecipe extends Recipe {

    private static final JsonPathMatcher COMPENSATION_MATCHER = new JsonPathMatcher("$..saga.compensation");
    private static final JsonPathMatcher COMPLETION_MATCHER = new JsonPathMatcher("$..saga.completion");

    @Override
    public String getDisplayName() {
        return "Camel YAML DSL Saga EIP restructuring";
    }

    @Override
    public String getDescription() {
        return "Apache Camel YAML DSL migration from version 4.18 to 4.19. Flattens saga compensation and completion nested uri to direct values.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return Preconditions.check(RecipesUtil.camelYamlDslPrecondition(), new AbstractCamelYamlVisitor() {

            @Override
            protected void clearLocalCache() {
                //nothing to do
            }

            @Override
            public Yaml.Mapping.Entry doVisitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
                Yaml.Mapping.Entry e = super.doVisitMappingEntry(entry, ctx);

                // Check if this is compensation or completion with nested uri
                if ((COMPENSATION_MATCHER.matches(getCursor()) || COMPLETION_MATCHER.matches(getCursor())) &&
                    entry.getValue() instanceof Yaml.Mapping) {

                    Yaml.Mapping nestedMapping = (Yaml.Mapping) entry.getValue();
                    // Look for uri field in nested mapping
                    for (Yaml.Mapping.Entry nestedEntry : nestedMapping.getEntries()) {
                        if (nestedEntry.getKey() instanceof Yaml.Scalar &&
                            "uri".equals(((Yaml.Scalar) nestedEntry.getKey()).getValue()) &&
                            nestedEntry.getValue() instanceof Yaml.Scalar) {

                            // Replace the nested mapping with just the uri scalar value
                            return entry.withValue(nestedEntry.getValue());
                        }
                    }
                }

                return e;
            }
        });
    }
}
