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
package org.apache.camel.upgrade.customRecipes;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.camel.upgrade.customRecipes.internal.ChangeJavaComponentUriRecipe;
import org.apache.camel.upgrade.customRecipes.internal.ChangeXmlComponentUriRecipe;
import org.apache.camel.upgrade.customRecipes.internal.ChangeYamlComponentUriRecipe;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite recipe that transforms component URIs across all DSL types (Java, XML, YAML).
 * <p>
 * This recipe wraps the three DSL-specific recipes so users only need to specify
 * the URI pattern and replacement once, and all DSL types are handled automatically.
 * <p>
 * Example usage in YAML:
 * <pre>
 * - org.apache.camel.upgrade.customRecipes.ChangeComponentUriRecipe:
 *     uriPattern: "^pulsar:((persistent|non-persistent)://([^/]+)/([^/]+)/([^/]+)/(.+))$"
 *     replacement: "pulsar:${2}://${3}/${5}/${6}"
 * </pre>
 * <p>
 * This will transform Pulsar URIs in Java code, XML DSL, and YAML DSL all at once.
 */
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@AllArgsConstructor
public class ChangeComponentUriRecipe extends Recipe {

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

    @Override
    public String getDisplayName() {
        return "Change Camel component URI across all DSLs";
    }

    @Override
    public String getDescription() {
        return "Transforms component URIs using regular expressions with capturing groups. " +
               "Automatically handles Java, XML DSL, and YAML DSL.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        List<Recipe> recipes = new ArrayList<>();

        // Apply transformation to all three DSL types
        recipes.add(new ChangeJavaComponentUriRecipe(uriPattern, replacement));
        recipes.add(new ChangeXmlComponentUriRecipe(uriPattern, replacement));
        recipes.add(new ChangeYamlComponentUriRecipe(uriPattern, replacement));

        return recipes;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        // This is a composite recipe - the actual work is done by the sub-recipes
        return TreeVisitor.noop();
    }
}
