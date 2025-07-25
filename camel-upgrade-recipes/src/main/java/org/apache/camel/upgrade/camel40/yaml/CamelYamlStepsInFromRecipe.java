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
package org.apache.camel.upgrade.camel40.yaml;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.camel.upgrade.AbstractCamelYamlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.format.IndentsVisitor;
import org.openrewrite.yaml.style.IndentsStyle;
import org.openrewrite.yaml.tree.Yaml;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixes following yaml change.
 *
 * The backwards compatible mode Camel 3.14 or older, which allowed to have steps as child to route has been removed.
 *
 * The old syntax:
 *
 * <pre>
 * - route:
 *     from:
 *       uri: "direct:info"
 *     steps:
 *     - log: "message"
 * </pre>
 *
 * should be changed to:
 *
 * <pre>
 * - route:
 *     from:
 *       uri: "direct:info"
 *       steps:
 *       - log: "message"
 * </pre>
 */
@EqualsAndHashCode(callSuper = false)
@Value
public class CamelYamlStepsInFromRecipe extends Recipe {

    private static final String[] PATHS_TO_PRE_CHECK = new String[] { "route.from" };
    private static final JsonPathMatcher MATCHER_WITHOUT_ROUTE = new JsonPathMatcher("$.steps");
    private static final JsonPathMatcher MATCHER_WITH_ROUTE = new JsonPathMatcher("$.route.steps");

    @Override
    public String getDisplayName() {
        return "Camel Yaml steps not allowed as route child";
    }

    @Override
    public String getDescription() {
        return "The YAML DSL backwards compatible mode in Camel 3.14 or older, which allowed 'steps' to be defined as a child of 'route' has been removed.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new AbstractCamelYamlVisitor() {
            //both variables has to be set to null, to mark the migration done
            Yaml.Mapping from = null;
            Yaml.Mapping.Entry steps = null;

            @Override
            protected void clearLocalCache() {
                //do nothing
            }

            @Override
            public  Yaml.Mapping.@Nullable Entry doVisitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
                Yaml.Mapping.Entry e = super.doVisitMappingEntry(entry, ctx);

                if (steps == null && (MATCHER_WITH_ROUTE.matches(getCursor()) || MATCHER_WITHOUT_ROUTE.matches(getCursor()))) {
                    steps = e;
                    if (from != null) {
                        moveSteps();
                    }
                    return null;
                }
                return e;

            }

            @Override
            public Yaml.Mapping doVisitMapping(Yaml.Mapping mapping, ExecutionContext ctx) {
                Yaml.Mapping m = super.doVisitMapping(mapping, ctx);

                String prop = RecipesUtil.getProperty(getCursor());
                if (("route.from".equals(prop) || "from".equals(prop)) && from == null) {
                    from = m;
                    if (steps != null) {
                        moveSteps();
                    }
                }

                return m;
            }

            private void moveSteps() {
                doAfterVisit(new YamlIsoVisitor<ExecutionContext>() {

                    @Override
                    public Yaml.Mapping visitMapping(Yaml.Mapping mapping, ExecutionContext ctx) {
                        Yaml.Mapping m = super.visitMapping(mapping, ctx);

                        if (m == from) {
                            List<Yaml.Mapping.Entry> entries = new ArrayList<>(m.getEntries());
                            entries.add(steps.copyPaste().withPrefix("\n"));
                            m = m.withEntries(entries);
                        }

                        return m;
                    }
                });

                //TODO might probably change indent in original file, may this happen?
                doAfterVisit(new IndentsVisitor(new IndentsStyle(2), null));
            }
        };
    }

}
