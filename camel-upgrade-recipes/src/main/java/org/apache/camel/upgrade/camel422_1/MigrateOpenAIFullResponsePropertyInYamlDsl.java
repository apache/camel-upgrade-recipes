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
package org.apache.camel.upgrade.camel422_1;

import org.apache.camel.upgrade.AbstractCamelYamlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Renames the camel-openai {@code CamelOpenAIResponse} exchange property in YAML DSL routes whose
 * single openai operation is embeddings, audio-transcription or audio-translation (CAMEL-24539).
 * A route is one {@code - route:} / {@code - from:} sequence item.
 */
public class MigrateOpenAIFullResponsePropertyInYamlDsl extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate camel-openai CamelOpenAIResponse property in YAML DSL";
    }

    @Override
    public String getDescription() {
        return "Renames the CamelOpenAIResponse exchange property in a YAML DSL route whose single openai " +
               "operation is embeddings, audio-transcription or audio-translation. Routes with several openai " +
               "operations, a chat-completion/responses/tool-execution/audio-speech operation, or no openai " +
               "endpoint are left unchanged.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(RecipesUtil.camelYamlDslPrecondition(), new RouteVisitor());
    }

    private static class RouteVisitor extends AbstractCamelYamlVisitor {

        @Override
        protected void clearLocalCache() {
            // Nothing to clear
        }

        @Override
        public Yaml.Mapping doVisitMapping(Yaml.Mapping mapping, ExecutionContext ctx) {
            Yaml.Mapping m = super.doVisitMapping(mapping, ctx);

            if (!isRouteRoot(m)) {
                return m;
            }

            Set<String> operations = collectOpenAIOperations(m);
            if (operations.size() != 1) {
                return m;
            }

            Optional<OpenAIResponseProperty> replacement = OpenAIResponseProperty.forOperation(operations.iterator().next());
            if (replacement.isEmpty()) {
                return m;
            }

            return (Yaml.Mapping) new PropertyRenameVisitor(replacement.get()).visitNonNull(m, ctx);
        }

        private boolean isRouteRoot(Yaml.Mapping mapping) {
            if (!(getCursor().getParentTreeCursor().getValue() instanceof Yaml.Sequence.Entry)) {
                return false;
            }
            return mapping.getEntries().stream()
                    .anyMatch(e -> "route".equals(e.getKey().getValue()) || "from".equals(e.getKey().getValue()));
        }

        private static Set<String> collectOpenAIOperations(Yaml.Mapping route) {
            Set<String> operations = new LinkedHashSet<>();
            new YamlIsoVisitor<Set<String>>() {
                @Override
                public Yaml.Mapping.Entry visitMappingEntry(Yaml.Mapping.Entry entry, Set<String> acc) {
                    if (OpenAIResponseProperty.ENDPOINT_URI_METHOD_NAMES.contains(entry.getKey().getValue())) {
                        uriFromEndpointValue(entry.getValue())
                                .flatMap(OpenAIResponseProperty::operationFromUri)
                                .ifPresent(acc::add);
                    }
                    return super.visitMappingEntry(entry, acc);
                }
            }.visit(route, operations);
            return operations;
        }

        private static Optional<String> uriFromEndpointValue(Yaml.Block value) {
            if (value instanceof Yaml.Scalar) {
                return Optional.of(((Yaml.Scalar) value).getValue());
            }
            if (value instanceof Yaml.Mapping) {
                return ((Yaml.Mapping) value).getEntries().stream()
                        .filter(e -> "uri".equals(e.getKey().getValue()))
                        .map(e -> RecipesUtil.getValueFromScalar(e.getValue()))
                        .filter(Objects::nonNull)
                        .findFirst();
            }
            return Optional.empty();
        }
    }

    private static class PropertyRenameVisitor extends AbstractCamelYamlVisitor {
        private final OpenAIResponseProperty replacement;

        PropertyRenameVisitor(OpenAIResponseProperty replacement) {
            this.replacement = replacement;
        }

        @Override
        protected void clearLocalCache() {
            // Nothing to clear
        }

        @Override
        public Yaml.Mapping.Entry doVisitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
            Yaml.Mapping.Entry e = super.doVisitMappingEntry(entry, ctx);

            if ("name".equals(e.getKey().getValue())
                    && e.getValue() instanceof Yaml.Scalar
                    && OpenAIResponseProperty.OLD_PROPERTY_NAME.equals(((Yaml.Scalar) e.getValue()).getValue())
                    && isPropertyOperation()) {
                return e.withValue(((Yaml.Scalar) e.getValue()).withValue(replacement.newPropertyName));
            }

            if (e.getValue() instanceof Yaml.Scalar) {
                Yaml.Scalar scalar = (Yaml.Scalar) e.getValue();
                String renamed = replacement.renameSimpleExpression(scalar.getValue());
                if (!scalar.getValue().equals(renamed)) {
                    return e.withValue(scalar.withValue(renamed));
                }
            }

            return e;
        }

        /**
         * Check if this "name" entry is within a setProperty or removeProperty mapping.
         */
        private boolean isPropertyOperation() {
            for (int i = 0; i < 10; i++) {
                Cursor parent = getCursor().getParent(i);
                Object value = parent != null ? parent.getValue() : null;
                if (value instanceof Yaml.Mapping) {
                    boolean hasPropertyOp = ((Yaml.Mapping) value).getEntries().stream()
                            .anyMatch(e -> "setProperty".equals(e.getKey().getValue()) || "removeProperty".equals(e.getKey().getValue()));
                    if (hasPropertyOp) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
