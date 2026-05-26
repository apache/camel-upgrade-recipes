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
package org.apache.camel.upgrade.customRecipes.internal;

import org.apache.camel.upgrade.AbstractCamelJavaVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.regex.Pattern;

/**
 * Transform component URIs in Java code using regexp with capturing groups.
 */
public class ChangeJavaComponentUriRecipe extends Recipe {

    @Option(
        displayName = "URI pattern",
        description = "Regular expression to match the component URI. Use capturing groups for parts to preserve.",
        example = "^pulsar:((persistent|non-persistent)://([^/]+)/([^/]+)/([^/]+)/(.+))$"
    )
    public String uriPattern;

    @Option(
        displayName = "Replacement",
        description = "Replacement string using ${1}, ${2}, etc. to reference capturing groups from the pattern.",
        example = "pulsar:${2}://${3}/${5}/${6}"
    )
    public String replacement;

    public ChangeJavaComponentUriRecipe() {
    }

    public ChangeJavaComponentUriRecipe(String uriPattern, String replacement) {
        this.uriPattern = uriPattern;
        this.replacement = replacement;
    }

    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    @Override
    public String getDisplayName() {
        return "Change Camel component URI in Java";
    }

    @Override
    public String getDescription() {
        return "Transforms component URIs in Java code using regular expressions with capturing groups.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        Pattern pattern = Pattern.compile(uriPattern);

        return new AbstractCamelJavaVisitor() {
            @Override
            protected J.Literal doVisitLiteral(J.Literal literal, ExecutionContext ctx) {
                J.Literal l = super.doVisitLiteral(literal, ctx);

                if (JavaType.Primitive.String == l.getType() && l.getValue() != null) {
                    String value = (String) l.getValue();
                    return RecipesUtil.transform(value, pattern, replacement)
                            .map(newValue -> RecipesUtil.createStringLiteral(newValue).withPrefix(literal.getPrefix()))
                            .orElse(l);
                }

                return l;
            }
        };
    }
}
