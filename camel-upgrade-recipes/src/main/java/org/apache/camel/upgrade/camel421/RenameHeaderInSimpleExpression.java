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

import org.apache.camel.upgrade.AbstractCamelJavaVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.J;

import java.util.regex.Pattern;

/**
 * Renames header references in Simple expressions like ${header.oldName} or ${headers.oldName}.
 * This recipe only transforms strings inside simple() method calls to avoid false positives.
 */
public class RenameHeaderInSimpleExpression extends Recipe {

    @Option(displayName = "Old header name",
            description = "The old header name in Simple expression",
            example = "kafka.TOPIC")
    String oldHeaderName;

    @Option(displayName = "New header name",
            description = "The new header name to use",
            example = "CamelKafkaTopic")
    String newHeaderName;

    public RenameHeaderInSimpleExpression() {
    }

    public RenameHeaderInSimpleExpression(String oldHeaderName, String newHeaderName) {
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
        return "Rename header in Simple expressions";
    }

    @Override
    public String getDescription() {
        return "Renames header references in Simple expressions like ${header.oldName} → ${header.newName}. " +
               "Only migrates expressions inside simple() method calls.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new SimpleExpressionVisitor(oldHeaderName, newHeaderName));
    }

    private static class SimpleExpressionVisitor extends AbstractCamelJavaVisitor {
        private final String oldHeaderName;
        private final String newHeaderName;
        private final Pattern headerPattern;
        private final Pattern headersPattern;

        SimpleExpressionVisitor(String oldHeaderName, String newHeaderName) {
            this.oldHeaderName = oldHeaderName;
            this.newHeaderName = newHeaderName;

            // Escape dots in header name for regex, but keep them in the pattern
            String escapedOldName = Pattern.quote(oldHeaderName);

            // Match ${header.oldName} or ${headers.oldName}
            this.headerPattern = Pattern.compile("(\\$\\{header\\.)" + escapedOldName + "(\\})");
            this.headersPattern = Pattern.compile("(\\$\\{headers\\.)" + escapedOldName + "(\\})");
        }

        @Override
        protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation mi = super.doVisitMethodInvocation(method, ctx);

            // Check if this is a simple() method call (any class, method name "simple")
            if (mi.getSimpleName().equals("simple")) {
                // Get the first argument (the Simple expression string)
                if (!mi.getArguments().isEmpty() && mi.getArguments().get(0) instanceof J.Literal) {
                    J.Literal literal = (J.Literal) mi.getArguments().get(0);

                    if (literal.getValue() instanceof String) {
                        String expression = (String) literal.getValue();
                        String newExpression = expression;

                        // Replace ${header.oldName} with ${header.newName}
                        newExpression = headerPattern.matcher(newExpression).replaceAll("$1" + newHeaderName + "$2");

                        // Replace ${headers.oldName} with ${headers.newName}
                        newExpression = headersPattern.matcher(newExpression).replaceAll("$1" + newHeaderName + "$2");

                        // If changed, update the literal
                        if (!expression.equals(newExpression)) {
                            J.Literal newLiteral = literal.withValue(newExpression)
                                                         .withValueSource("\"" + newExpression + "\"");

                            java.util.List<org.openrewrite.java.tree.Expression> newArgs = new java.util.ArrayList<>(mi.getArguments());
                            newArgs.set(0, newLiteral);

                            return mi.withArguments(newArgs);
                        }
                    }
                }
            }

            return mi;
        }
    }
}
