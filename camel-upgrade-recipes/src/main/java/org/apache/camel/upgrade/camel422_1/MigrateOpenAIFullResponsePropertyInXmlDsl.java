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

import org.apache.camel.upgrade.AbstractCamelXmlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.marker.Markers;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Renames the camel-openai {@code CamelOpenAIResponse} exchange property in XML DSL routes whose
 * single openai operation is embeddings, audio-transcription or audio-translation (CAMEL-24539).
 * A route is one {@code <route>} element.
 */
public class MigrateOpenAIFullResponsePropertyInXmlDsl extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate camel-openai CamelOpenAIResponse property in XML DSL";
    }

    @Override
    public String getDescription() {
        return "Renames the CamelOpenAIResponse exchange property in an XML DSL route whose single openai " +
               "operation is embeddings, audio-transcription or audio-translation. Routes with several openai " +
               "operations, a chat-completion/responses/tool-execution/audio-speech operation, or no openai " +
               "endpoint are left unchanged.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(RecipesUtil.camelXmlDslPrecondition(), new RouteVisitor());
    }

    private static class RouteVisitor extends AbstractCamelXmlVisitor {

        @Override
        public Xml.Tag doVisitTag(Xml.Tag tag, ExecutionContext ctx) {
            Xml.Tag t = super.doVisitTag(tag, ctx);

            if (!"route".equals(t.getName())) {
                return t;
            }

            Set<String> operations = collectOpenAIOperations(t);
            if (operations.size() != 1) {
                return t;
            }

            Optional<OpenAIResponseProperty> replacement = OpenAIResponseProperty.forOperation(operations.iterator().next());
            if (replacement.isEmpty()) {
                return t;
            }

            return (Xml.Tag) new PropertyRenameVisitor(replacement.get()).visitNonNull(t, ctx);
        }

        private static Set<String> collectOpenAIOperations(Xml.Tag route) {
            Set<String> operations = new LinkedHashSet<>();
            new XmlIsoVisitor<Set<String>>() {
                @Override
                public Xml.Tag visitTag(Xml.Tag tag, Set<String> acc) {
                    if (OpenAIResponseProperty.ENDPOINT_URI_METHOD_NAMES.contains(tag.getName())) {
                        tag.getAttributes().stream()
                                .filter(a -> "uri".equals(a.getKeyAsString()))
                                .map(Xml.Attribute::getValueAsString)
                                .findFirst()
                                .flatMap(OpenAIResponseProperty::operationFromUri)
                                .ifPresent(acc::add);
                    }
                    return super.visitTag(tag, acc);
                }
            }.visit(route, operations);
            return operations;
        }
    }

    private static class PropertyRenameVisitor extends AbstractCamelXmlVisitor {
        private final OpenAIResponseProperty replacement;

        PropertyRenameVisitor(OpenAIResponseProperty replacement) {
            this.replacement = replacement;
        }

        @Override
        public Xml.Tag doVisitTag(Xml.Tag tag, ExecutionContext ctx) {
            Xml.Tag t = super.doVisitTag(tag, ctx);

            String tagName = t.getName();
            boolean propertyTag = "setProperty".equals(tagName) || "removeProperty".equals(tagName);

            t = t.withAttributes(ListUtils.map(t.getAttributes(), attr -> {
                String value = attr.getValueAsString();
                if (value == null) {
                    return attr;
                }

                if (propertyTag && "name".equals(attr.getKeyAsString()) && OpenAIResponseProperty.OLD_PROPERTY_NAME.equals(value)) {
                    return withValue(attr, replacement.newPropertyName);
                }

                String renamed = replacement.renameSimpleExpression(value);
                return value.equals(renamed) ? attr : withValue(attr, renamed);
            }));

            // Element text holds Simple expressions too, e.g. <simple>${exchangeProperty.x}</simple>
            if (t.getChildren().isEmpty()) {
                Optional<String> value = t.getValue();
                if (value.isPresent()) {
                    String renamed = replacement.renameSimpleExpression(value.get());
                    if (!value.get().equals(renamed)) {
                        t = t.withValue(renamed);
                    }
                }
            }

            return t;
        }

        private static Xml.Attribute withValue(Xml.Attribute attr, String newValue) {
            return attr.withValue(
                    new Xml.Attribute.Value(
                            attr.getValue().getId(),
                            "",
                            Markers.EMPTY,
                            attr.getValue().getQuote(),
                            newValue
                    )
            );
        }
    }
}
