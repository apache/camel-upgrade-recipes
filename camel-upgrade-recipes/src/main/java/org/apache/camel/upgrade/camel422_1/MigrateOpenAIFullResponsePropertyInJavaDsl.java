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
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Renames the camel-openai {@code CamelOpenAIResponse} exchange property (and the matching
 * {@code OpenAIConstants.RESPONSE} field) in Java DSL routes whose single openai operation is
 * embeddings, audio-transcription or audio-translation (CAMEL-24539). A route is one fluent chain
 * rooted at {@code from(...)}, including lambdas and processors nested in the chain.
 * <p>
 * Processors or beans defined outside the route (e.g. in a separate class) cannot be linked to an
 * operation and are left untouched.
 */
public class MigrateOpenAIFullResponsePropertyInJavaDsl extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate camel-openai CamelOpenAIResponse property in Java DSL";
    }

    @Override
    public String getDescription() {
        return "Renames the CamelOpenAIResponse exchange property (and OpenAIConstants.RESPONSE) in a Java DSL " +
               "route whose single openai operation is embeddings, audio-transcription or audio-translation. " +
               "Routes with several openai operations, a chat-completion/responses/tool-execution/audio-speech " +
               "operation, or no openai endpoint are left unchanged. Processors or beans defined outside the " +
               "route cannot be linked to an operation and are not migrated.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new RouteVisitor());
    }

    private static class RouteVisitor extends AbstractCamelJavaVisitor {

        @Override
        protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation mi = super.doVisitMethodInvocation(method, ctx);

            if (!isRouteRoot(method)) {
                return mi;
            }

            Set<String> operations = collectOpenAIOperations(mi);
            if (operations.size() != 1) {
                return mi;
            }

            Optional<OpenAIResponseProperty> replacement = OpenAIResponseProperty.forOperation(operations.iterator().next());
            if (replacement.isEmpty()) {
                return mi;
            }

            return (J.MethodInvocation) new PropertyRenameVisitor(replacement.get()).visitNonNull(mi, ctx);
        }

        /**
         * True when {@code method} is the outermost invocation of a fluent chain that bottoms out
         * at a bare {@code from(...)} call, i.e. the whole route.
         */
        private boolean isRouteRoot(J.MethodInvocation method) {
            Object parentValue = getCursor().getParentTreeCursor().getValue();
            if (parentValue instanceof J.MethodInvocation && ((J.MethodInvocation) parentValue).getSelect() == method) {
                return false;
            }

            J.MethodInvocation current = method;
            while (current.getSelect() instanceof J.MethodInvocation) {
                current = (J.MethodInvocation) current.getSelect();
            }
            return current.getSelect() == null && "from".equals(current.getSimpleName());
        }

        private static Set<String> collectOpenAIOperations(J.MethodInvocation route) {
            Set<String> operations = new LinkedHashSet<>();
            new JavaIsoVisitor<Set<String>>() {
                @Override
                public J.MethodInvocation visitMethodInvocation(J.MethodInvocation mi, Set<String> acc) {
                    endpointOperation(mi).ifPresent(acc::add);
                    return super.visitMethodInvocation(mi, acc);
                }
            }.visit(route, operations);
            return operations;
        }

        private static Optional<String> endpointOperation(J.MethodInvocation mi) {
            if (!OpenAIResponseProperty.ENDPOINT_URI_METHOD_NAMES.contains(mi.getSimpleName()) || mi.getArguments().isEmpty()) {
                return Optional.empty();
            }

            Expression arg = mi.getArguments().get(0);
            Optional<String> literalOperation = stringLiteral(arg).flatMap(OpenAIResponseProperty::operationFromUri);
            if (literalOperation.isPresent()) {
                return literalOperation;
            }

            // Endpoint DSL form: to(openai("embeddings"))
            if (arg instanceof J.MethodInvocation) {
                J.MethodInvocation endpointDsl = (J.MethodInvocation) arg;
                if ("openai".equals(endpointDsl.getSimpleName()) && endpointDsl.getArguments().size() == 1) {
                    return stringLiteral(endpointDsl.getArguments().get(0));
                }
            }

            return Optional.empty();
        }

        private static Optional<String> stringLiteral(Expression expression) {
            if (expression instanceof J.Literal && ((J.Literal) expression).getValue() instanceof String) {
                return Optional.of((String) ((J.Literal) expression).getValue());
            }
            return Optional.empty();
        }
    }

    private static class PropertyRenameVisitor extends AbstractCamelJavaVisitor {
        private static final String MATCHER_EXCHANGE_GET_PROPERTY_1 = "org.apache.camel.Exchange getProperty(String)";
        private static final String MATCHER_EXCHANGE_GET_PROPERTY_CLASS
                = "org.apache.camel.Exchange getProperty(String, Class)";
        private static final String MATCHER_EXCHANGE_GET_PROPERTY_DEFAULT_CLASS
                = "org.apache.camel.Exchange getProperty(String, Object, Class)";
        private static final String MATCHER_EXCHANGE_SET_PROPERTY = "org.apache.camel.Exchange setProperty(String, Object)";
        private static final String MATCHER_EXCHANGE_REMOVE_PROPERTY = "org.apache.camel.Exchange removeProperty(String)";
        // matchOverrides=true: the DSL methods are invoked on ProcessorDefinition subtypes (RouteDefinition, ...)
        private static final MethodMatcher DSL_SET_PROPERTY_MATCHER =
                new MethodMatcher("org.apache.camel.model.ProcessorDefinition setProperty(String, ..)", true);
        private static final MethodMatcher DSL_REMOVE_PROPERTY_MATCHER =
                new MethodMatcher("org.apache.camel.model.ProcessorDefinition removeProperty(String)", true);
        private static final MethodMatcher BUILDER_EXCHANGE_PROPERTY_MATCHER =
                new MethodMatcher("org.apache.camel.builder.BuilderSupport exchangeProperty(String)", true);

        private final OpenAIResponseProperty replacement;

        PropertyRenameVisitor(OpenAIResponseProperty replacement) {
            this.replacement = replacement;
        }

        @Override
        protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation mi = super.doVisitMethodInvocation(method, ctx);

            if (!matchesPropertyMethod(mi) || mi.getArguments().isEmpty()) {
                return mi;
            }

            Expression arg0 = mi.getArguments().get(0);
            if (!(arg0 instanceof J.Literal) || !OpenAIResponseProperty.OLD_PROPERTY_NAME.equals(((J.Literal) arg0).getValue())) {
                return mi;
            }

            J.Literal newLiteral = ((J.Literal) arg0).withValue(replacement.newPropertyName)
                    .withValueSource("\"" + replacement.newPropertyName + "\"");
            List<Expression> newArgs = new ArrayList<>(mi.getArguments());
            newArgs.set(0, newLiteral);
            return mi.withArguments(newArgs);
        }

        @Override
        protected J.FieldAccess doVisitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext ctx) {
            J.FieldAccess fa = super.doVisitFieldAccess(fieldAccess, ctx);

            if (!OpenAIResponseProperty.OLD_CONSTANT_FIELD.equals(fa.getSimpleName())
                    || !(fa.getTarget() instanceof J.Identifier)
                    || !OpenAIResponseProperty.OPENAI_CONSTANTS_TYPE.equals(((J.Identifier) fa.getTarget()).getSimpleName())) {
                return fa;
            }

            return fa.withName(fa.getName().withSimpleName(replacement.newConstantField));
        }

        @Override
        protected J.Literal doVisitLiteral(J.Literal literal, ExecutionContext ctx) {
            J.Literal l = super.doVisitLiteral(literal, ctx);

            if (!(l.getValue() instanceof String)) {
                return l;
            }

            String value = (String) l.getValue();
            String renamed = replacement.renameSimpleExpression(value);
            if (value.equals(renamed)) {
                return l;
            }

            l = l.withValue(renamed);
            if (l.getValueSource() != null) {
                l = l.withValueSource(replacement.renameSimpleExpression(l.getValueSource()));
            }
            return l;
        }

        private boolean matchesPropertyMethod(J.MethodInvocation mi) {
            return getMethodMatcher(MATCHER_EXCHANGE_GET_PROPERTY_1).matches(mi)
                    || getMethodMatcher(MATCHER_EXCHANGE_GET_PROPERTY_CLASS).matches(mi)
                    || getMethodMatcher(MATCHER_EXCHANGE_GET_PROPERTY_DEFAULT_CLASS).matches(mi)
                    || getMethodMatcher(MATCHER_EXCHANGE_SET_PROPERTY).matches(mi)
                    || getMethodMatcher(MATCHER_EXCHANGE_REMOVE_PROPERTY).matches(mi)
                    || DSL_SET_PROPERTY_MATCHER.matches(mi)
                    || DSL_REMOVE_PROPERTY_MATCHER.matches(mi)
                    || BUILDER_EXCHANGE_PROPERTY_MATCHER.matches(mi);
        }
    }
}
