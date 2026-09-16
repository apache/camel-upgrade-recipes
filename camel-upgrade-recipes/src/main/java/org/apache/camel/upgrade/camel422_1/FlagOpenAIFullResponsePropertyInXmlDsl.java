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
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.camel.upgrade.camel422_1.FlagOpenAIFullResponsePropertyInJavaDsl.WARNING_COMMENT;

/**
 * Flags camel-openai {@code CamelOpenAIResponse} usages in XML DSL for manual review (CAMEL-24539).
 * See {@link FlagOpenAIFullResponsePropertyInJavaDsl} for why this marks instead of rewriting.
 * <p>
 * A comment is inserted as a real {@code Xml.Comment} sibling in the parent's content, right before
 * the flagged tag, rather than as raw {@code <!--...-->} text glued into the tag's prefix: on reparse
 * (e.g. a second recipe run) a prefix-embedded comment turns into its own sibling {@code Xml.Comment}
 * node, so a prefix-string-based idempotency check would stop seeing it and duplicate the comment.
 */
public class FlagOpenAIFullResponsePropertyInXmlDsl extends Recipe {

    // A short, unique substring of WARNING_COMMENT used to detect an already-added comment
    private static final String MARKER = "CAMEL-24539";

    private static final String OLD_PROPERTY_NAME = "CamelOpenAIResponse";

    private static final Pattern SIMPLE_EXCHANGE_PROPERTY = Pattern.compile(
            "\\$\\{exchangeProperty(?:\\.CamelOpenAIResponse[.}]|\\[CamelOpenAIResponse]}|(?:As)?\\(CamelOpenAIResponse[,)])");

    @Override
    public String getDisplayName() {
        return "Flag camel-openai CamelOpenAIResponse property in XML DSL";
    }

    @Override
    public String getDescription() {
        return "Adds a review comment wherever CamelOpenAIResponse is used, since whether it should become " +
               "CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse " +
               "or stay CamelOpenAIResponse cannot be decided statically.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(RecipesUtil.camelXmlDslPrecondition(), new FlagVisitor());
    }

    private static class FlagVisitor extends AbstractCamelXmlVisitor {

        @Override
        public Xml.Tag doVisitTag(Xml.Tag tag, ExecutionContext ctx) {
            Xml.Tag t = super.doVisitTag(tag, ctx);

            List<? extends Content> content = t.getContent();
            if (content == null || content.isEmpty()) {
                return t;
            }

            List<Content> newContent = null;
            for (int i = 0; i < content.size(); i++) {
                Content c = content.get(i);
                if (c instanceof Xml.Tag && shouldFlag((Xml.Tag) c) && !hasPrecedingMarkerComment(content, i)) {
                    if (newContent == null) {
                        newContent = new ArrayList<>(content.subList(0, i));
                    }
                    newContent.add(RecipesUtil.createXmlComment(WARNING_COMMENT).withPrefix(c.getPrefix()));
                }
                if (newContent != null) {
                    newContent.add(c);
                }
            }

            return newContent != null ? t.withContent(newContent) : t;
        }

        private static boolean hasPrecedingMarkerComment(List<? extends Content> content, int index) {
            return index > 0 && content.get(index - 1) instanceof Xml.Comment
                    && ((Xml.Comment) content.get(index - 1)).getText().contains(MARKER);
        }

        private static boolean shouldFlag(Xml.Tag tag) {
            boolean propertyTag = "setProperty".equals(tag.getName()) || "removeProperty".equals(tag.getName());

            for (Xml.Attribute attr : tag.getAttributes()) {
                String value = attr.getValueAsString();
                if (value == null) {
                    continue;
                }
                if (propertyTag && "name".equals(attr.getKeyAsString()) && OLD_PROPERTY_NAME.equals(value)) {
                    return true;
                }
                if (SIMPLE_EXCHANGE_PROPERTY.matcher(value).find()) {
                    return true;
                }
            }

            if (tag.getChildren().isEmpty()) {
                String value = tag.getValue().orElse(null);
                if (value != null && SIMPLE_EXCHANGE_PROPERTY.matcher(value).find()) {
                    return true;
                }
            }

            return false;
        }
    }
}
