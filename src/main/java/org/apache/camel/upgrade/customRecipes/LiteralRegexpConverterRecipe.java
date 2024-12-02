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
package org.apache.camel.upgrade.customRecipes;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.camel.upgrade.AbstractCamelJavaVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces literal matching pattern and replacing it with a replacement (regexp groups are supported)
 */
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@AllArgsConstructor
public class LiteralRegexpConverterRecipe extends Recipe {

    @Option(displayName = "Literal regexp name",
            description = "Regexp for matching a literal.")
    public String regexp;

    @Option(displayName = "Replacement to use",
            description = "Replacement to use.")
    public String replacement;

    @Override
    public String getDisplayName() {
        return "Replaces a literal matching an expression";
    }

    @Override
    public String getDescription() {
        return "Replaces literal, groups from regexp can be used as ${0}, ${1}, ...";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new AbstractCamelJavaVisitor() {

            @Override
            protected J.Literal doVisitLiteral(J.Literal literal, ExecutionContext context) {
                J.Literal l  =  super.doVisitLiteral(literal, context);

                // Only handle String literals
                if (TypeUtils.isString(literal.getType()) && literal.getValue() != null) {
                    Matcher m = getPattern(regexp.trim()).matcher((String)literal.getValue());
                    if(m.matches()) {
                        //if no group one group is used
                        String tmp = replacement;
                        for (int i = 1; i <= 100; i++) {
                            if(!tmp.contains("${" + i + "}")) {
                                break;
                            }
                            String tmp2 = tmp.replaceAll("\\$\\{" + i + "}", m.group(i));
                            //if there is no change, return value
                            if(tmp.equals(tmp2)) {
                                break;
                            }
                            tmp = tmp2;
                        }
                        return RecipesUtil.createStringLiteral(tmp);
                    }
                }

                return l;
            }
        });
    }
}
