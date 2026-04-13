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
package org.apache.camel.upgrade.camel419;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Optional;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_camel_test_infra">camel-test-infra</a>
 * </p>
 * Test infrastructure modules no longer produce separate test-JAR artifacts.
 * Removes {@code <type>test-jar</type>} from camel-test-infra-* dependencies.
 */
public class Pom419TestInfraRecipe extends Recipe {

    private static final XPathMatcher DEPENDENCY_MATCHER = new XPathMatcher("/project/dependencies/dependency");

    @Override
    public String getDisplayName() {
        return "Remove test-jar type from camel-test-infra dependencies";
    }

    @Override
    public String getDescription() {
        return "Removes <type>test-jar</type> from camel-test-infra-* dependencies as they no longer produce separate test-JAR artifacts.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new XmlVisitor<ExecutionContext>() {

            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                Xml.Tag t = (Xml.Tag) super.visitTag(tag, ctx);

                if (DEPENDENCY_MATCHER.matches(getCursor())) {
                    // Check if this is a camel-test-infra-* dependency
                    Optional<String> groupId = t.getChildValue("groupId");
                    Optional<String> artifactId = t.getChildValue("artifactId");

                    if (groupId.isPresent() && "org.apache.camel".equals(groupId.get()) &&
                        artifactId.isPresent() && artifactId.get().startsWith("camel-test-infra-")) {

                        // Check if it has <type>test-jar</type>
                        Optional<Xml.Tag> typeTag = t.getChild("type");
                        if (typeTag.isPresent() && "test-jar".equals(typeTag.get().getValue().orElse(""))) {
                            // Remove the type tag
                            t = t.withContent(
                                t.getContent().stream()
                                    .filter(content -> content != typeTag.get())
                                    .toList()
                            );
                        }
                    }
                }

                return t;
            }
        };
    }
}
