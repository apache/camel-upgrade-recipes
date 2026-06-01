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
 * Renames header prefixes in Simple expressions like ${header.SolrField.id} → ${header.CamelSolrField.id}.
 * This recipe only transforms strings inside simple() method calls to avoid false positives.
 */
public class RenameHeaderPrefixInSimpleExpression extends Recipe {

    @Option(displayName = "Old header prefix",
            description = "The old header prefix in Simple expression",
            example = "SolrField.")
    String oldPrefix;

    @Option(displayName = "New header prefix",
            description = "The new header prefix to use",
            example = "CamelSolrField.")
    String newPrefix;

    public RenameHeaderPrefixInSimpleExpression() {
    }

    public RenameHeaderPrefixInSimpleExpression(String oldPrefix, String newPrefix) {
        this.oldPrefix = oldPrefix;
        this.newPrefix = newPrefix;
    }

    public void setOldPrefix(String oldPrefix) {
        this.oldPrefix = oldPrefix;
    }

    public void setNewPrefix(String newPrefix) {
        this.newPrefix = newPrefix;
    }

    @Override
    public String getDisplayName() {
        return "Rename header prefix in Simple expressions";
    }

    @Override
    public String getDescription() {
        return "Renames header prefixes in Simple expressions like ${header.SolrField.id} → ${header.CamelSolrField.id}. " +
               "Only migrates expressions inside simple() method calls.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new SimpleExpressionVisitor(oldPrefix, newPrefix));
    }

    private static class SimpleExpressionVisitor extends AbstractCamelJavaVisitor {
        private final String oldPrefix;
        private final String newPrefix;
        private final Pattern headerPattern;
        private final Pattern headersPattern;

        SimpleExpressionVisitor(String oldPrefix, String newPrefix) {
            this.oldPrefix = oldPrefix;
            this.newPrefix = newPrefix;

            // Escape dots in prefix for regex
            String escapedOldPrefix = Pattern.quote(oldPrefix);

            // Match ${header.SolrField.xxx} or ${headers.SolrField.xxx}
            // Captures: group1=${header. or ${headers., group2=rest after old prefix, group3=}
            this.headerPattern = Pattern.compile("(\\$\\{header\\.)" + escapedOldPrefix + "([^}]+)(\\})");
            this.headersPattern = Pattern.compile("(\\$\\{headers\\.)" + escapedOldPrefix + "([^}]+)(\\})");
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

                        // Replace ${header.SolrField.xxx} with ${header.CamelSolrField.xxx}
                        newExpression = headerPattern.matcher(newExpression).replaceAll("$1" + newPrefix + "$2$3");

                        // Replace ${headers.SolrField.xxx} with ${headers.CamelSolrField.xxx}
                        newExpression = headersPattern.matcher(newExpression).replaceAll("$1" + newPrefix + "$2$3");

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
