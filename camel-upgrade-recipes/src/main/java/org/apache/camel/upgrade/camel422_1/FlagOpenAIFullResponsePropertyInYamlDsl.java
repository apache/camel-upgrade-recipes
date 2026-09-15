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
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.regex.Pattern;

import static org.apache.camel.upgrade.camel422_1.FlagOpenAIFullResponsePropertyInJavaDsl.WARNING_COMMENT;

/**
 * Flags camel-openai {@code CamelOpenAIResponse} usages in YAML DSL for manual review (CAMEL-24539).
 * See {@link FlagOpenAIFullResponsePropertyInJavaDsl} for why this marks instead of rewriting.
 */
public class FlagOpenAIFullResponsePropertyInYamlDsl extends Recipe {

    // A short, unique substring of WARNING_COMMENT used to detect an already-added comment
    private static final String MARKER = "CAMEL-24539";

    private static final String OLD_PROPERTY_NAME = "CamelOpenAIResponse";

    private static final Pattern SIMPLE_EXCHANGE_PROPERTY = Pattern.compile(
            "\\$\\{exchangeProperty(?:\\.CamelOpenAIResponse[.}]|\\[CamelOpenAIResponse]}|(?:As)?\\(CamelOpenAIResponse[,)])");

    @Override
    public String getDisplayName() {
        return "Flag camel-openai CamelOpenAIResponse property in YAML DSL";
    }

    @Override
    public String getDescription() {
        return "Adds a review comment wherever CamelOpenAIResponse is used, since whether it should become " +
               "CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse " +
               "or stay CamelOpenAIResponse cannot be decided statically.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(RecipesUtil.camelYamlDslPrecondition(), new FlagVisitor());
    }

    private static class FlagVisitor extends AbstractCamelYamlVisitor {

        @Override
        protected void clearLocalCache() {
            // Nothing to clear
        }

        @Override
        public Yaml.Mapping.Entry doVisitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
            Yaml.Mapping.Entry e = super.doVisitMappingEntry(entry, ctx);

            if (!(e.getValue() instanceof Yaml.Scalar)) {
                return e;
            }
            Yaml.Scalar scalar = (Yaml.Scalar) e.getValue();

            boolean flag = ("name".equals(e.getKey().getValue()) && OLD_PROPERTY_NAME.equals(scalar.getValue())
                            && isPropertyOperation())
                           || SIMPLE_EXCHANGE_PROPERTY.matcher(scalar.getValue()).find();

            if (flag && !e.getPrefix().contains(MARKER)) {
                return e.withPrefix(addCommentToPrefix(e.getPrefix()));
            }

            return e;
        }

        /**
         * Check if this entry is within a setProperty or removeProperty mapping.
         */
        private boolean isPropertyOperation() {
            for (int i = 0; i < 10; i++) {
                Object value = getCursor().getParent(i) != null ? getCursor().getParent(i).getValue() : null;
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

        private static String addCommentToPrefix(String prefix) {
            int lastNewline = prefix.lastIndexOf('\n');
            String indent = lastNewline >= 0 ? prefix.substring(lastNewline + 1) : "";
            return prefix + "#" + WARNING_COMMENT + "\n" + indent;
        }
    }
}
