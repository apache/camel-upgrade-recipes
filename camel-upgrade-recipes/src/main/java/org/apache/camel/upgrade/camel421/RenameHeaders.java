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
package org.apache.camel.upgrade.camel421;

import org.openrewrite.Option;
import org.openrewrite.Recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Recipe that renames Camel headers across all DSL types (Java, Simple expressions, XML, YAML).
 * Supports both single header rename and bulk rename using a map.
 *
 * Example YAML configuration (bulk rename):
 * <pre>
 * - org.apache.camel.upgrade.camel421.RenameHeaders:
 *     headerMappings:
 *       kafka.TOPIC: CamelKafkaTopic
 *       kafka.PARTITION: CamelKafkaPartition
 *       kafka.KEY: CamelKafkaKey
 * </pre>
 *
 * Example YAML configuration (single rename):
 * <pre>
 * - org.apache.camel.upgrade.camel421.RenameHeaders:
 *     oldHeaderName: kafka.TOPIC
 *     newHeaderName: CamelKafkaTopic
 * </pre>
 */
public class RenameHeaders extends Recipe {

    @Option(displayName = "Header mappings",
            description = "Map of old header names to new header names (for bulk rename)",
            example = "kafka.TOPIC: CamelKafkaTopic",
            required = false)
    Map<String, String> headerMappings;

    @Option(displayName = "Old header name",
            description = "The old header name to replace (for single rename)",
            example = "kafka.TOPIC",
            required = false)
    String oldHeaderName;

    @Option(displayName = "New header name",
            description = "The new header name to use (for single rename)",
            example = "CamelKafkaTopic",
            required = false)
    String newHeaderName;

    public RenameHeaders() {
    }

    public RenameHeaders(Map<String, String> headerMappings) {
        this.headerMappings = headerMappings;
    }

    public RenameHeaders(String oldHeaderName, String newHeaderName) {
        this.oldHeaderName = oldHeaderName;
        this.newHeaderName = newHeaderName;
    }

    public void setHeaderMappings(Map<String, String> headerMappings) {
        this.headerMappings = headerMappings;
    }

    public void setOldHeaderName(String oldHeaderName) {
        this.oldHeaderName = oldHeaderName;
    }

    public void setNewHeaderName(String newHeaderName) {
        this.newHeaderName = newHeaderName;
    }

    @Override
    public String getDisplayName() {
        return "Rename Camel header(s) across all DSLs";
    }

    @Override
    public String getDescription() {
        return "Renames Camel header(s) from old name(s) to new name(s) across all DSL types: " +
               "Java method calls (.setHeader, .getHeader), Simple expressions (${header.name}), " +
               "XML DSL (<setHeader name=\"...\">), and YAML DSL (setHeader.name). " +
               "Supports both single header rename (oldHeaderName/newHeaderName) and bulk rename (headerMappings). " +
               "Only migrates safe contexts to avoid false positives.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        List<Recipe> recipes = new ArrayList<>();

        // Bulk rename using map
        if (headerMappings != null) {
            for (Map.Entry<String, String> entry : headerMappings.entrySet()) {
                recipes.add(createHeaderRenameRecipe(entry.getKey(), entry.getValue()));
            }
        }

        // Single rename using oldHeaderName/newHeaderName
        if (oldHeaderName != null && newHeaderName != null) {
            recipes.add(createHeaderRenameRecipe(oldHeaderName, newHeaderName));
        }

        return recipes;
    }

    private Recipe createHeaderRenameRecipe(String oldName, String newName) {
        return new Recipe() {
            @Override
            public String getDisplayName() {
                return "Rename header: " + oldName + " → " + newName;
            }

            @Override
            public String getDescription() {
                return "Renames header '" + oldName + "' to '" + newName + "' across all DSLs";
            }

            @Override
            public List<Recipe> getRecipeList() {
                return Arrays.asList(
                    new RenameHeaderInJavaMethod(oldName, newName),
                    new RenameHeaderInSimpleExpression(oldName, newName),
                    new RenameHeaderInXmlDsl(oldName, newName),
                    new RenameHeaderInYamlDsl(oldName, newName)
                );
            }
        };
    }
}
