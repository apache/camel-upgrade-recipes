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
package org.apache.camel.upgrade.camel412;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.camel.upgrade.AbstractCamelJavaVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.J;

/**
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_12.html#_java_dsl">Java DSL</a>
 */
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class Java412Recipes extends Recipe {

    private static final String M_END_CHOICE = "org.apache.camel.model.ChoiceDefinition endChoice()";

    @Override
    public String getDisplayName() {
        return "Camel Java DSL change for camel 4.12";
    }

    @Override
    public String getDescription() {
        return "Apache Camel Java DSL migration from version 4.11 to 4.12.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return RecipesUtil.newVisitor(new AbstractCamelJavaVisitor() {
            @Override
            protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
                J.MethodInvocation mi = super.doVisitMethodInvocation(method, context);

                if (getMethodMatcher(M_END_CHOICE).matches(mi, false)) {
                    return mi.withName(mi.getName().withSimpleName("end().endChoice"));
                }

                return mi;
            }
        });
    }
}