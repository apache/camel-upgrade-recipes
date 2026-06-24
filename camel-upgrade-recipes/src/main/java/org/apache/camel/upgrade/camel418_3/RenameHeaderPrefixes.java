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
package org.apache.camel.upgrade.camel418_3;

import org.openrewrite.Option;
import org.openrewrite.Recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Recipe that renames Camel header prefixes across all DSL types (Java, Simple expressions, XML, YAML).
 * This is different from {@link RenameHeaders} which does exact matching - this recipe replaces prefixes.
 *
 * Example: For prefix mapping "SolrField." → "CamelSolrField."
 * - "SolrField.id" becomes "CamelSolrField.id"
 * - "SolrField.name" becomes "CamelSolrField.name"
 * - "SolrParam.commit" remains unchanged (different prefix)
 *
 * Example YAML configuration:
 * <pre>
 * - org.apache.camel.upgrade.camel418_3.RenameHeaderPrefixes:
 *     prefixMappings:
 *       SolrField.: CamelSolrField.
 *       SolrParam.: CamelSolrParam.
 * </pre>
 */
public class RenameHeaderPrefixes extends Recipe {

    @Option(displayName = "Prefix mappings",
            description = "Map of old header prefixes to new header prefixes",
            example = "SolrField.: CamelSolrField.",
            required = true)
    Map<String, String> prefixMappings;

    public RenameHeaderPrefixes() {
    }

    public RenameHeaderPrefixes(Map<String, String> prefixMappings) {
        this.prefixMappings = prefixMappings;
    }

    public void setPrefixMappings(Map<String, String> prefixMappings) {
        this.prefixMappings = prefixMappings;
    }

    @Override
    public String getDisplayName() {
        return "Rename Camel header prefixes across all DSLs";
    }

    @Override
    public String getDescription() {
        return "Renames Camel header prefixes across all DSL types: " +
               "Java method calls (.setHeader, .getHeader), Simple expressions (${header.name}), " +
               "XML DSL (<setHeader name=\"...\">), and YAML DSL (setHeader.name). " +
               "Any header starting with an old prefix gets that prefix replaced with the new prefix. " +
               "Only migrates safe contexts to avoid false positives.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        List<Recipe> recipes = new ArrayList<>();

        if (prefixMappings != null) {
            for (Map.Entry<String, String> entry : prefixMappings.entrySet()) {
                recipes.add(createPrefixRenameRecipe(entry.getKey(), entry.getValue()));
            }
        }

        return recipes;
    }

    private Recipe createPrefixRenameRecipe(String oldPrefix, String newPrefix) {
        return new Recipe() {
            @Override
            public String getDisplayName() {
                return "Rename header prefix: " + oldPrefix + " → " + newPrefix;
            }

            @Override
            public String getDescription() {
                return "Renames header prefix '" + oldPrefix + "' to '" + newPrefix + "' across all DSLs";
            }

            @Override
            public List<Recipe> getRecipeList() {
                return Arrays.asList(
                    new RenameHeaderPrefixInJavaMethod(oldPrefix, newPrefix),
                    new RenameHeaderPrefixInSimpleExpression(oldPrefix, newPrefix),
                    new RenameHeaderPrefixInXmlDsl(oldPrefix, newPrefix),
                    new RenameHeaderPrefixInYamlDsl(oldPrefix, newPrefix)
                );
            }
        };
    }
}
