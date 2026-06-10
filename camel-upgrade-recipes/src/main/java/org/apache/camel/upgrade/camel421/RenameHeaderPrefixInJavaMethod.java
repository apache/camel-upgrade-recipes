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

import java.util.ArrayList;
import java.util.List;

/**
 * Renames header prefixes in Message.setHeader() and Message.getHeader() method calls.
 * This recipe migrates string literals starting with a specific prefix to use a new prefix.
 */
public class RenameHeaderPrefixInJavaMethod extends Recipe {

    @Option(displayName = "Old header prefix",
            description = "The old header prefix to replace",
            example = "SolrField.")
    String oldPrefix;

    @Option(displayName = "New header prefix",
            description = "The new header prefix to use",
            example = "CamelSolrField.")
    String newPrefix;

    public RenameHeaderPrefixInJavaMethod() {
    }

    public RenameHeaderPrefixInJavaMethod(String oldPrefix, String newPrefix) {
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
        return "Rename header prefix in .setHeader()/.getHeader() calls";
    }

    @Override
    public String getDescription() {
        return "Renames header prefixes in Message.setHeader() and Message.getHeader() method calls. " +
               "Only migrates string literals in safe contexts. Does NOT migrate dynamic header names or Map.get() calls.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new RenameHeaderPrefixVisitor(oldPrefix, newPrefix));
    }

    private static class RenameHeaderPrefixVisitor extends AbstractCamelJavaVisitor {
        private static final String MATCHER_SET_HEADER_2_ARGS = "org.apache.camel.Message setHeader(String, Object)";
        private static final String MATCHER_GET_HEADER_1_ARG = "org.apache.camel.Message getHeader(String)";
        private static final String MATCHER_GET_HEADER_2_ARGS = "org.apache.camel.Message getHeader(String, Class)";
        private static final String MATCHER_GET_HEADER_3_ARGS = "org.apache.camel.Message getHeader(String, Object, Class)";
        private static final String MATCHER_GET_HEADER_SUPPLIER = "org.apache.camel.Message getHeader(String, java.util.function.Supplier, Class)";

        private final String oldPrefix;
        private final String newPrefix;

        RenameHeaderPrefixVisitor(String oldPrefix, String newPrefix) {
            this.oldPrefix = oldPrefix;
            this.newPrefix = newPrefix;
        }

        @Override
        protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation mi = super.doVisitMethodInvocation(method, ctx);

            // Check if this is a setHeader or getHeader call
            if (matchesHeaderMethod(mi)) {
                // Get the first argument (header name)
                if (!mi.getArguments().isEmpty() && mi.getArguments().get(0) instanceof J.Literal) {
                    J.Literal literal = (J.Literal) mi.getArguments().get(0);

                    // Check if it's a string literal starting with the old prefix
                    if (literal.getValue() != null &&
                        literal.getValue() instanceof String) {
                        String headerName = (String) literal.getValue();

                        if (headerName.startsWith(oldPrefix)) {
                            // Replace the prefix
                            String newHeaderName = newPrefix + headerName.substring(oldPrefix.length());

                            // Replace the string literal with the new header name
                            J.Literal newLiteral = literal.withValue(newHeaderName)
                                                         .withValueSource("\"" + newHeaderName + "\"");

                            List<org.openrewrite.java.tree.Expression> newArgs = new ArrayList<>(mi.getArguments());
                            newArgs.set(0, newLiteral);

                            return mi.withArguments(newArgs);
                        }
                    }
                }
            }

            return mi;
        }

        private boolean matchesHeaderMethod(J.MethodInvocation mi) {
            return getMethodMatcher(MATCHER_SET_HEADER_2_ARGS).matches(mi) ||
                   getMethodMatcher(MATCHER_GET_HEADER_1_ARG).matches(mi) ||
                   getMethodMatcher(MATCHER_GET_HEADER_2_ARGS).matches(mi) ||
                   getMethodMatcher(MATCHER_GET_HEADER_3_ARGS).matches(mi) ||
                   getMethodMatcher(MATCHER_GET_HEADER_SUPPLIER).matches(mi);
        }
    }
}
