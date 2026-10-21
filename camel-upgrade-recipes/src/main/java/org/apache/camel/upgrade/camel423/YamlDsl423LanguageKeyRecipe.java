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
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.tree.Yaml;

/**
 * Renames an expression language key in YAML DSL documents, for a language that was removed in
 * Camel 4.23 in favour of another one ({@code csimple} -> {@code simple}, {@code joor} -> {@code java}).
 * Scoped to documents that belong to the Camel YAML DSL, so an unrelated YAML file that happens to
 * contain such a key is left untouched.
 */
public class YamlDsl423LanguageKeyRecipe extends Recipe {

    @Option(example = "csimple", displayName = "Old language key",
            description = "The expression language key to rename.")
    String oldKey;

    @Option(example = "simple", displayName = "New language key",
            description = "The expression language key to be replaced with.")
    String newKey;

    public YamlDsl423LanguageKeyRecipe() {
    }

    public YamlDsl423LanguageKeyRecipe(String oldKey, String newKey) {
        this.oldKey = oldKey;
        this.newKey = newKey;
    }

    public void setOldKey(String oldKey) {
        this.oldKey = oldKey;
    }

    public void setNewKey(String newKey) {
        this.newKey = newKey;
    }

    @Override
    public String getDisplayName() {
        return "Migrate a removed YAML DSL expression language key";
    }

    @Override
    public String getDescription() {
        return "Renames a removed expression language key to its replacement in Camel YAML DSL documents.";
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

                if (oldKey.equals(e.getKey().getValue())) {
                    return e.withKey(((Yaml.Scalar) e.getKey().copyPaste()).withValue(newKey));
                }

                return e;
            }
        });
    }
}
