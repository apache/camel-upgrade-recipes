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

import org.apache.camel.upgrade.AbstractCamelXmlVisitor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_saga_eip">Saga EIP</a>
 * </p>
 * Changed model for configuring completion and compensation URIs in XML DSL.
 * Converts child elements to attributes.
 */
public class XmlDsl419SagaRecipe extends Recipe {

    private static final XPathMatcher SAGA_MATCHER = new XPathMatcher("//saga");

    @Override
    public String getDisplayName() {
        return "Camel XML DSL Saga EIP restructuring";
    }

    @Override
    public String getDescription() {
        return "Apache Camel XML DSL migration from version 4.18 to 4.19. Converts saga compensation and completion child elements to attributes.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractCamelXmlVisitor() {

            @Override
            public Xml.Tag doVisitTag(final Xml.Tag tag, final ExecutionContext ctx) {
                Xml.Tag t = super.doVisitTag(tag, ctx);

                if (SAGA_MATCHER.matches(getCursor())) {
                    List<Xml.Tag> children = new ArrayList<>(t.getChildren());
                    List<Xml.Attribute> attributes = new ArrayList<>(t.getAttributes());
                    boolean modified = false;

                    modified |= convertChildElementToAttribute(children, attributes, "compensation");
                    modified |= convertChildElementToAttribute(children, attributes, "completion");

                    if (modified) {
                        t = t.withContent(children);
                        t = t.withAttributes(attributes);
                    }
                }

                return t;
            }

            private boolean convertChildElementToAttribute(List<Xml.Tag> children, List<Xml.Attribute> attributes, String elementName) {
                Optional<Xml.Tag> childTag = children.stream()
                        .filter(child -> elementName.equals(child.getName()))
                        .findFirst();

                if (childTag.isPresent()) {
                    Xml.Tag tag = childTag.get();
                    String uri = null;

                    // First, try to get the uri from the 'uri' attribute
                    Optional<Xml.Attribute> uriAttr = tag.getAttributes().stream()
                            .filter(attr -> "uri".equals(attr.getKeyAsString()))
                            .findFirst();

                    if (uriAttr.isPresent()) {
                        uri = uriAttr.get().getValueAsString();
                    } else if (tag.getValue().isPresent()) {
                        // Fallback to text content if no uri attribute
                        uri = tag.getValue().get().trim();
                    }

                    if (uri != null && !uri.isEmpty()) {
                        children.removeIf(child -> elementName.equals(child.getName()));

                        if (attributes.stream().noneMatch(a -> elementName.equals(a.getKeyAsString()))) {
                            attributes.add(new Xml.Attribute(
                                    org.openrewrite.Tree.randomId(),
                                    " ",
                                    org.openrewrite.marker.Markers.EMPTY,
                                    new Xml.Ident(org.openrewrite.Tree.randomId(), "", org.openrewrite.marker.Markers.EMPTY, elementName),
                                    "",
                                    new Xml.Attribute.Value(
                                            org.openrewrite.Tree.randomId(),
                                            "",
                                            org.openrewrite.marker.Markers.EMPTY,
                                            Xml.Attribute.Value.Quote.Double,
                                            uri
                                    )
                            ));
                        }
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
