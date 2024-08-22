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
package org.apache.camel.updates.camel47;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Triple;
import org.apache.camel.updates.AbstractCamelJavaVisitor;
import org.apache.camel.updates.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Replaces prefix with the new one and changes the suffix tp start with lower case
 */
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class Java47Recipes extends Recipe {


    private static final String MATCHER_GET_HEADER = "org.apache.camel.Message getHeader(java.lang.String, java.lang.Class)";
    private static final String MATCHER_GET_IN = "org.apache.camel.Exchange getIn()";
    private static final List<Triple<String, String, String>> HEADERS_MAP = Arrays.asList(
            new Triple("org.apache.camel.Message getHeader(java.lang.String, java.lang.Class)", "Exchange.HTTP_SERVLET_REQUEST", "#{any(org.apache.camel.Exchange)}.getMessage(HttpMessage.class).getRequest()"),
            new Triple("org.apache.camel.Message getHeader(java.lang.String, java.lang.Class)", "Exchange.HTTP_SERVLET_RESPONSE", "#{any(org.apache.camel.Exchange)}.getMessage(HttpMessage.class).getResponse()"));

    @Override
    public String getDisplayName() {
        return "Change of headers with embedded HTTP server (consumer)";
    }

    @Override
    public String getDescription() {
        return "Change of headers with embedded HTTP server (consumer). The headers CamelHttpServletRequest and CamelHttpServletResponse has been removed..";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new AbstractCamelJavaVisitor() {
            @Override
            protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
                J.MethodInvocation mi = super.doVisitMethodInvocation(method, context);

                //get all mi starts with Exchange.getIn()
                if (mi.getSelect() instanceof J.MethodInvocation
                        && getMethodMatcher(MATCHER_GET_IN).matches((J.MethodInvocation) mi.getSelect(), false)) {
                    //apply map of transformations
                    Optional<J.MethodInvocation> result = HEADERS_MAP.stream()
                            .filter(triplet ->
                                    getMethodMatcher(triplet.a).matches(mi) && mi.toString().contains(triplet.b))
                            //rename tag
                            .map(triplet -> (J.MethodInvocation)JavaTemplate.builder(triplet.c)
                                    .build().apply(getCursor(), mi.getCoordinates().replace(), ((J.MethodInvocation)mi.getSelect()).getSelect())
                                    .withPrefix(mi.getPrefix()))
                            .findAny();

                    if(result.isPresent()) {
                        doAfterVisit(new AddImport<>("org.apache.camel.http.common.HttpMessage", null, false));
                        return result.get();
                    }

                }


                return mi;
            }

        });


    }
}