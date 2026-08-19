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
package org.apache.camel.upgrade.customRecipes.internal;

import org.apache.camel.upgrade.AbstractCamelYamlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.tree.Yaml;

import java.util.regex.Pattern;

/**
 * Transform component URIs in YAML DSL using regexp with capturing groups.
 */
public class ChangeYamlComponentUriRecipe extends Recipe {

    private static final JsonPathMatcher YAML_URI_MATCHER = new JsonPathMatcher("$..uri");
    private static final JsonPathMatcher YAML_FROM_URI_MATCHER = new JsonPathMatcher("$..from.uri");

    @Option(
        displayName = "URI pattern",
        description = "Regular expression to match the component URI. Use capturing groups for parts to preserve.",
        example = "^pulsar:((persistent|non-persistent)://([^/]+)/([^/]+)/([^/]+)/(.+))$"
    )
    public String uriPattern;

    @Option(
        displayName = "Replacement",
        description = "Replacement string using ${1}, ${2}, etc. to reference capturing groups from the pattern.",
        example = "pulsar:${2}://${3}/${5}/${6}"
    )
    public String replacement;

    @Option(
        displayName = "Consumer only",
        description = "When true, only from.uri endpoints are transformed; other uri fields are left unchanged.",
        example = "true",
        required = false
    )
    public Boolean consumerOnly;

    public ChangeYamlComponentUriRecipe() {
    }

    public ChangeYamlComponentUriRecipe(String uriPattern, String replacement) {
        this(uriPattern, replacement, null);
    }

    public ChangeYamlComponentUriRecipe(String uriPattern, String replacement, Boolean consumerOnly) {
        this.uriPattern = uriPattern;
        this.replacement = replacement;
        this.consumerOnly = consumerOnly;
    }

    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public void setConsumerOnly(Boolean consumerOnly) {
        this.consumerOnly = consumerOnly;
    }

    @Override
    public String getDisplayName() {
        return "Change Camel component URI in YAML DSL";
    }

    @Override
    public String getDescription() {
        return "Transforms component URIs in YAML DSL using regular expressions with capturing groups.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        Pattern pattern = Pattern.compile(uriPattern);

        return Preconditions.check(RecipesUtil.camelYamlDslPrecondition(), new AbstractCamelYamlVisitor() {
            @Override
            protected void clearLocalCache() {
                // Nothing to clear
            }

            @Override
            public Yaml.Mapping.Entry doVisitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
                Yaml.Mapping.Entry e = super.doVisitMappingEntry(entry, ctx);

                // Check if this is a uri field
                JsonPathMatcher uriMatcher = Boolean.TRUE.equals(consumerOnly) ? YAML_FROM_URI_MATCHER : YAML_URI_MATCHER;
                if (uriMatcher.matches(getCursor()) && e.getValue() instanceof Yaml.Scalar) {
                    Yaml.Scalar scalar = (Yaml.Scalar) e.getValue();
                    return RecipesUtil.transform(scalar.getValue(), pattern, replacement)
                            .map(newValue -> e.withValue(scalar.withValue(newValue)))
                            .orElse(e);
                }

                return e;
            }
        });
    }
}
