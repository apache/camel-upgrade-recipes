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

import org.apache.camel.upgrade.AbstractCamelYamlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.tree.Yaml;

/**
 * Renames header references in YAML DSL setHeader.name, header.name, and removeHeader.name entries.
 */
public class RenameHeaderInYamlDsl extends Recipe {

    @Option(displayName = "Old header name",
            description = "The old header name",
            example = "kafka.TOPIC")
    String oldHeaderName;

    @Option(displayName = "New header name",
            description = "The new header name",
            example = "CamelKafkaTopic")
    String newHeaderName;

    public RenameHeaderInYamlDsl() {
    }

    public RenameHeaderInYamlDsl(String oldHeaderName, String newHeaderName) {
        this.oldHeaderName = oldHeaderName;
        this.newHeaderName = newHeaderName;
    }

    public void setOldHeaderName(String oldHeaderName) {
        this.oldHeaderName = oldHeaderName;
    }

    public void setNewHeaderName(String newHeaderName) {
        this.newHeaderName = newHeaderName;
    }

    @Override
    public String getDisplayName() {
        return "Rename header in YAML DSL";
    }

    @Override
    public String getDescription() {
        return "Renames header references in YAML DSL setHeader.name, header.name, and removeHeader.name entries.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(RecipesUtil.camelYamlDslPrecondition(),
                                   new YamlHeaderVisitor(oldHeaderName, newHeaderName));
    }

    private static class YamlHeaderVisitor extends AbstractCamelYamlVisitor {
        private final String oldHeaderName;
        private final String newHeaderName;

        YamlHeaderVisitor(String oldHeaderName, String newHeaderName) {
            this.oldHeaderName = oldHeaderName;
            this.newHeaderName = newHeaderName;
        }

        @Override
        protected void clearLocalCache() {
            // Nothing to cache
        }

        @Override
        public Yaml.Mapping.Entry doVisitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
            Yaml.Mapping.Entry e = super.doVisitMappingEntry(entry, ctx);

            // Check if this is a "name" entry with the old header value
            if ("name".equals(e.getKey().getValue()) &&
                e.getValue() instanceof Yaml.Scalar &&
                oldHeaderName.equals(((Yaml.Scalar) e.getValue()).getValue())) {

                // Check if any parent mapping contains setHeader, header, or removeHeader
                if (isHeaderOperation()) {
                    Yaml.Scalar scalarValue = (Yaml.Scalar) e.getValue();
                    // Replace with new header name
                    return e.withValue(scalarValue.withValue(newHeaderName));
                }
            }

            return e;
        }

        /**
         * Check if this entry is within a setHeader, header, or removeHeader mapping.
         */
        private boolean isHeaderOperation() {
            // Walk up the cursor tree looking for a mapping with setHeader, header, or removeHeader
            for (int i = 0; i < 10; i++) { // Check up to 10 levels up
                Object value = getCursor().getParent(i) != null ? getCursor().getParent(i).getValue() : null;
                if (value instanceof Yaml.Mapping) {
                    Yaml.Mapping mapping = (Yaml.Mapping) value;
                    boolean hasHeaderOp = mapping.getEntries().stream()
                        .anyMatch(e -> {
                            String key = e.getKey().getValue();
                            return "setHeader".equals(key) || "header".equals(key) || "removeHeader".equals(key);
                        });
                    if (hasHeaderOp) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
