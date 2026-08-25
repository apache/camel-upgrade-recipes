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

import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

/**
 * Search recipe form of {@link RecipesUtil#camelXmlDslPrecondition()}, so that declarative recipes can
 * reference it from a {@code preconditions:} block the way the Java recipes use it in code.
 */
public class FindCamelXmlDsl extends Recipe {

    @Override
    public String getDisplayName() {
        return "Find Camel XML DSL documents";
    }

    @Override
    public String getDescription() {
        return "Marks XML documents that belong to the Camel XML DSL, so that a recipe operating on XML " +
               "is not applied to unrelated documents such as Spring bean definitions. A Camel namespace " +
               "or a Camel context element anywhere in the document is conclusive; otherwise the root " +
               "element decides.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.camelXmlDslPrecondition();
    }
}
