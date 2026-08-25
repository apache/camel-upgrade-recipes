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
package org.apache.camel.upgrade.camel418_3;

import org.apache.camel.upgrade.AbstractCamelJavaVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.J;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renames header references in Simple expressions like ${header.oldName} or ${headers.oldName}.
 * The whole placeholder is matched, so any string literal carrying a Simple expression is covered,
 * whether it is passed to simple(), to log(), or to an endpoint URI.
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
        return "Renames header references in Simple expressions like ${header.oldName} → ${header.newName}, " +
               "in every string literal carrying a Simple expression: simple(), log(), endpoint URIs and the like. " +
               "Only the complete ${header.oldName} placeholder is matched, so plain occurrences of the name are left alone.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new SimpleExpressionVisitor(oldHeaderName, newHeaderName));
    }

    private static class SimpleExpressionVisitor extends AbstractCamelJavaVisitor {
        private final Pattern headerPattern;
        private final Pattern headersPattern;
        private final String replacement;

        SimpleExpressionVisitor(String oldHeaderName, String newHeaderName) {
            // Escape dots in header name for regex, but keep them in the pattern
            String escapedOldName = Pattern.quote(oldHeaderName);

            // Match ${header.oldName} or ${headers.oldName}
            this.headerPattern = Pattern.compile("(\\$\\{header\\.)" + escapedOldName + "(\\})");
            this.headersPattern = Pattern.compile("(\\$\\{headers\\.)" + escapedOldName + "(\\})");
            this.replacement = "$1" + Matcher.quoteReplacement(newHeaderName) + "$2";
        }

        @Override
        protected J.Literal doVisitLiteral(J.Literal literal, ExecutionContext ctx) {
            J.Literal l = super.doVisitLiteral(literal, ctx);

            if (!(l.getValue() instanceof String)) {
                return l;
            }

            String expression = (String) l.getValue();
            String newExpression = rename(expression);
            if (expression.equals(newExpression)) {
                return l;
            }

            l = l.withValue(newExpression);

            // The placeholder needs no escaping, so the same replacement can be applied to the
            // source representation, which keeps the original quoting style intact
            if (l.getValueSource() != null) {
                l = l.withValueSource(rename(l.getValueSource()));
            }

            return l;
        }

        private String rename(String value) {
            String renamed = headerPattern.matcher(value).replaceAll(replacement);
            return headersPattern.matcher(renamed).replaceAll(replacement);
        }
    }
}
