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

import org.apache.camel.upgrade.AbstractCamelXmlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import java.util.regex.Pattern;

/**
 * Transform component URIs in XML DSL using regexp with capturing groups.
 */
public class ChangeXmlComponentUriRecipe extends Recipe {

    private static final XPathMatcher FROM_MATCHER = new XPathMatcher("//route/from");
    private static final XPathMatcher TO_MATCHER = new XPathMatcher("//route/to");

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

    public ChangeXmlComponentUriRecipe() {
    }

    public ChangeXmlComponentUriRecipe(String uriPattern, String replacement) {
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
        return "Change Camel component URI in XML DSL";
    }

    @Override
    public String getDescription() {
        return "Transforms component URIs in XML DSL using regular expressions with capturing groups.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        Pattern pattern = Pattern.compile(uriPattern);

        return new AbstractCamelXmlVisitor() {
            @Override
            public Xml.Tag doVisitTag(Xml.Tag tag, ExecutionContext ctx) {
                Xml.Tag t = super.doVisitTag(tag, ctx);

                if (FROM_MATCHER.matches(getCursor()) || TO_MATCHER.matches(getCursor())) {
                    return transformXmlUri(t, pattern, replacement);
                }

                return t;
            }
        };
    }

    private static Xml.Tag transformXmlUri(Xml.Tag tag, Pattern pattern, String replacement) {
        return tag.withAttributes(ListUtils.map(tag.getAttributes(), attr -> {
            if (!"uri".equals(attr.getKey().getName())) {
                return attr;
            }
            return RecipesUtil.transform(attr.getValue().getValue(), pattern, replacement)
                    .map(newUri -> attr.withValue(attr.getValue().withValue(newUri)))
                    .orElse(attr);
        }));
    }
}
