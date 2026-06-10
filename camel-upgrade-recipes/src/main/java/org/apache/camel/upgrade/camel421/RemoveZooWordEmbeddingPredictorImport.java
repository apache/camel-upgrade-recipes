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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

import java.util.ArrayList;
import java.util.List;

public class RemoveZooWordEmbeddingPredictorImport extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove ZooWordEmbeddingPredictor import";
    }

    @Override
    public String getDescription() {
        return "Removes import for ZooWordEmbeddingPredictor which has been removed from camel-djl. The underlying MXNet engine is discontinued and the zoo model never loaded successfully.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
                J.CompilationUnit c = super.visitCompilationUnit(cu, ctx);

                // Find and remove the ZooWordEmbeddingPredictor import
                List<J.Import> imports = new ArrayList<>(c.getImports());
                int removedIndex = -1;
                for (int i = 0; i < imports.size(); i++) {
                    String importType = imports.get(i).getQualid().printTrimmed(getCursor());
                    // The class could be in different packages, so we check the simple name
                    if (importType.endsWith(".ZooWordEmbeddingPredictor")) {
                        removedIndex = i;
                        imports.remove(i);
                        break;
                    }
                }

                if (removedIndex >= 0 && !imports.isEmpty()) {
                    // If we removed the first import and there are more imports,
                    // clean up the prefix of the new first import
                    if (removedIndex == 0) {
                        J.Import firstImport = imports.get(0);
                        String prefix = firstImport.getPrefix().getWhitespace();
                        // Remove leading newlines
                        String cleanedPrefix = prefix.replaceFirst("^\\n+", "");
                        imports.set(0, firstImport.withPrefix(firstImport.getPrefix().withWhitespace(cleanedPrefix)));
                    }
                    c = c.withImports(imports);
                } else if (removedIndex >= 0) {
                    // All imports removed
                    c = c.withImports(imports);
                }

                return c;
            }
        };
    }
}
