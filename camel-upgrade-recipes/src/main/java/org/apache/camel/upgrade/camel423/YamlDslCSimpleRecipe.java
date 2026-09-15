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
package org.apache.camel.upgrade.camel423;

import org.apache.camel.upgrade.AbstractCamelYamlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.tree.Yaml;

/**
 * Renames the removed {@code csimple} expression language key to {@code simple} in YAML DSL documents.
 * Scoped to documents that belong to the Camel YAML DSL, so an unrelated YAML file that happens to
 * contain a {@code csimple} key is left untouched.
 */
public class YamlDslCSimpleRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate csimple YAML DSL expression key to simple";
    }

    @Override
    public String getDescription() {
        return "Renames the removed csimple expression key to simple in Camel YAML DSL documents.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(RecipesUtil.camelYamlDslPrecondition(), new AbstractCamelYamlVisitor() {

            @Override
            protected void clearLocalCache() {
                // Nothing to cache
            }

            @Override
            public Yaml.Mapping.Entry doVisitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
                Yaml.Mapping.Entry e = super.doVisitMappingEntry(entry, ctx);

                if ("csimple".equals(e.getKey().getValue())) {
                    return e.withKey(((Yaml.Scalar) e.getKey().copyPaste()).withValue("simple"));
                }

                return e;
            }
        });
    }
}
