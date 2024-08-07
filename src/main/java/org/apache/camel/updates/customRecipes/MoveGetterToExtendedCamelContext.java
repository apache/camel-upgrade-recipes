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
package org.apache.camel.updates.customRecipes;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.camel.updates.AbstractCamelJavaVisitor;
import org.apache.camel.updates.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.regex.Pattern;

/**
 * Replaces prefix with the new one and changes the suffix tp start with lower case
 */
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@AllArgsConstructor
public class MoveGetterToExtendedCamelContext extends Recipe {

    private static final String MATCHER_GET_NAME_RESOLVER = "org.apache.camel.ExtendedCamelContext getComponentNameResolver()";
    private static final String MATCHER_GET_MODEL_JAXB_CONTEXT_FACTORY
            = "org.apache.camel.ExtendedCamelContext getModelJAXBContextFactory()";
    private static final String MATCHER_GET_MODEL_TO_XML_DUMPER = "org.apache.camel.ExtendedCamelContext getModelToXMLDumper()";
    private static final Pattern ABSTRACT_CONTEXT_TYPE = Pattern.compile("org.apache.camel.impl.engine.AbstractCamelContext");
    private static final String MATCHER_CONTEXT_GET_EXT = "org.apache.camel.CamelContext getExtension(java.lang.Class)";

    @Option(displayName = "Method name",
            description = "Name of the method on external camel context.")
    public String oldMethodName;

    @Override
    public String getDisplayName() {
        return "Move getter from context to ExtendedCamelContext.";
    }

    @Override
    public String getDescription() {
        return "Move getter from context to ExtendedCamelContext";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new AbstractCamelJavaVisitor() {
            @Override
            protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
                J.MethodInvocation mi = super.doVisitMethodInvocation(method, context);

                // extendedContext.getModelJAXBContextFactory() -> PluginHelper.getModelJAXBContextFactory(extendedContext)
                if (getMethodMatcher(getOldMethodMatcher()).matches(mi, false)) {
                        mi = JavaTemplate.builder(getNewMethodFromExternalContextContext())
                                //.contextSensitive()
                                .build()
                                .apply(getCursor(), mi.getCoordinates().replace(), mi.getSelect());

                        mi = mi.withArguments(method.getArguments());
                }

                return mi;
            }

            private String getOldMethodMatcher() {
                return "org.apache.camel.impl.engine.AbstractCamelContext " + oldMethodName + "(..)";
            }

            private String getNewMethodFromContext() {
                return "PluginHelper." + oldMethodName + "(#{any(org.apache.camel.CamelContext)})";
            }

            private String getNewMethodFromExternalContextContext() {
                return "#{any(org.apache.camel.CamelContext)}.getCamelContextExtension()." + oldMethodName + "()";
            }
        });
    }
}
