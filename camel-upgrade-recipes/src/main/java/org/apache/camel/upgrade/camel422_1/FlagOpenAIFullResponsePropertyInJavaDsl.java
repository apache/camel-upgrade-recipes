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

import org.apache.camel.upgrade.AbstractCamelJavaVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Flags camel-openai {@code CamelOpenAIResponse} / {@code OpenAIConstants.RESPONSE} usages in Java
 * DSL for manual review (CAMEL-24539): with {@code storeFullResponse=true}, embeddings,
 * audio-transcription and audio-translation moved to their own exchange property, while
 * chat-completion is unchanged. Which one applies to a given occurrence is not mechanically
 * decidable in general (exchange properties cross route boundaries via error handlers,
 * {@code onCompletion}, and endpoints such as {@code direct:}), so this recipe marks every
 * occurrence with a comment instead of rewriting it.
 * <p>
 * Comments are attached only to literal/field-access argument nodes, never to a method invocation
 * that is itself part of a fluent chain: OpenRewrite prints an invocation's own prefix before its
 * whole {@code select} subtree, so a comment on a chain link renders before the start of the chain
 * rather than next to that link.
 */
public class FlagOpenAIFullResponsePropertyInJavaDsl extends Recipe {

    static final String WARNING_COMMENT
            = " CAMEL-24539 (Camel 4.22.1): with storeFullResponse=true, embeddings/audio-transcription/audio-translation "
              + "now use CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse "
              + "instead of CamelOpenAIResponse; chat-completion is unchanged. Verify which operation this refers to and update "
              + "the property name manually if needed.";

    @Override
    public String getDisplayName() {
        return "Flag camel-openai CamelOpenAIResponse property in Java DSL";
    }

    @Override
    public String getDescription() {
        return "Adds a review comment wherever CamelOpenAIResponse or OpenAIConstants.RESPONSE is used, since whether it " +
               "should become CamelOpenAIEmbeddingsResponse/CamelOpenAIAudioTranscriptionResponse/CamelOpenAIAudioTranslationResponse " +
               "or stay CamelOpenAIResponse cannot be decided statically.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new FlagVisitor());
    }

    private static class FlagVisitor extends AbstractCamelJavaVisitor {
        private static final String OLD_PROPERTY_NAME = "CamelOpenAIResponse";
        private static final String OPENAI_CONSTANTS_FQN = "org.apache.camel.component.openai.OpenAIConstants";

        // ${exchangeProperty.CamelOpenAIResponse}, ${exchangeProperty[CamelOpenAIResponse]},
        // ${exchangeProperty.CamelOpenAIResponse.someOgnl}, ${exchangeProperty(CamelOpenAIResponse)},
        // ${exchangePropertyAs(CamelOpenAIResponse, ...)} -- whole name only, so CamelOpenAIResponseModel etc. don't match
        private static final Pattern SIMPLE_EXCHANGE_PROPERTY = Pattern.compile(
                "\\$\\{exchangeProperty(?:\\.CamelOpenAIResponse[.}]|\\[CamelOpenAIResponse]}|(?:As)?\\(CamelOpenAIResponse[,)])");

        @Override
        protected J.FieldAccess doVisitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext ctx) {
            J.FieldAccess fa = super.doVisitFieldAccess(fieldAccess, ctx);

            if (referencesOpenAIConstantsResponse(fa) && !RecipesUtil.isCommentBeforeElement(fa, WARNING_COMMENT)) {
                fa = fa.withComments(Collections.singletonList(RecipesUtil.createMultinlineComment(WARNING_COMMENT)));
            }

            return fa;
        }

        @Override
        protected J.Literal doVisitLiteral(J.Literal literal, ExecutionContext ctx) {
            J.Literal l = super.doVisitLiteral(literal, ctx);

            if (!(l.getValue() instanceof String)) {
                return l;
            }
            String value = (String) l.getValue();

            boolean flag = OLD_PROPERTY_NAME.equals(value) || SIMPLE_EXCHANGE_PROPERTY.matcher(value).find();
            if (flag && !RecipesUtil.isCommentBeforeElement(l, WARNING_COMMENT)) {
                l = l.withComments(Collections.singletonList(RecipesUtil.createMultinlineComment(WARNING_COMMENT)));
            }

            return l;
        }

        /**
         * True for {@code OpenAIConstants.RESPONSE}. The type may not be attributed (camel-openai need not
         * be on the recipe's own classpath), so this falls back to the class import when there is no type.
         */
        private boolean referencesOpenAIConstantsResponse(J.FieldAccess fa) {
            if (!"RESPONSE".equals(fa.getSimpleName()) || !(fa.getTarget() instanceof J.Identifier)) {
                return false;
            }

            JavaType targetType = fa.getTarget().getType();
            if (targetType != null) {
                return TypeUtils.isOfClassType(targetType, OPENAI_CONSTANTS_FQN);
            }

            if (!"OpenAIConstants".equals(((J.Identifier) fa.getTarget()).getSimpleName())) {
                return false;
            }
            J.CompilationUnit cu = getCursor().firstEnclosing(J.CompilationUnit.class);
            return cu != null && cu.getImports().stream()
                    .anyMatch(imp -> !imp.isStatic() && OPENAI_CONSTANTS_FQN.equals(imp.getTypeName()));
        }
    }
}
