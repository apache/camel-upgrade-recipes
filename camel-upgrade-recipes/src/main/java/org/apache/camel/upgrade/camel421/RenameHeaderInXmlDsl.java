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

import org.apache.camel.upgrade.AbstractCamelXmlVisitor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.marker.Markers;
import org.openrewrite.xml.tree.Xml;

/**
 * Renames header references in XML DSL <setHeader name="..."> and <header name="..."> elements.
 */
public class RenameHeaderInXmlDsl extends Recipe {

    @Option(displayName = "Old header name",
            description = "The old header name",
            example = "kafka.TOPIC")
    String oldHeaderName;

    @Option(displayName = "New header name",
            description = "The new header name",
            example = "CamelKafkaTopic")
    String newHeaderName;

    public RenameHeaderInXmlDsl() {
    }

    public RenameHeaderInXmlDsl(String oldHeaderName, String newHeaderName) {
        this.oldHeaderName = oldHeaderName;
        this.newHeaderName = newHeaderName;
    }

    public void setOldHeaderName(String oldHeaderName) {
        this.oldHeaderName = oldHeaderName;
    }

    public void setNewHeaderName(String newHeaderName) {
        this.newHeaderName = newHeaderName;
    }

    @Override
    public String getDisplayName() {
        return "Rename header in XML DSL";
    }

    @Override
    public String getDescription() {
        return "Renames header references in XML DSL <setHeader name=\"...\">, <header name=\"...\">, " +
               "and <removeHeader name=\"...\"> elements.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new XmlHeaderVisitor(oldHeaderName, newHeaderName);
    }

    private static class XmlHeaderVisitor extends AbstractCamelXmlVisitor {
        private final String oldHeaderName;
        private final String newHeaderName;

        XmlHeaderVisitor(String oldHeaderName, String newHeaderName) {
            this.oldHeaderName = oldHeaderName;
            this.newHeaderName = newHeaderName;
        }

        @Override
        public Xml.Tag doVisitTag(Xml.Tag tag, ExecutionContext ctx) {
            Xml.Tag t = super.doVisitTag(tag, ctx);

            // Check if this is a setHeader, header, or removeHeader tag
            String tagName = t.getName();
            if ("setHeader".equals(tagName) || "header".equals(tagName) || "removeHeader".equals(tagName)) {
                // Look for the "name" attribute with oldHeaderName value
                return t.withAttributes(ListUtils.map(t.getAttributes(), attr -> {
                    if ("name".equals(attr.getKeyAsString()) &&
                        oldHeaderName.equals(attr.getValueAsString())) {
                        // Replace with new header name
                        return attr.withValue(
                            new Xml.Attribute.Value(
                                attr.getValue().getId(),
                                "",
                                Markers.EMPTY,
                                attr.getValue().getQuote(),
                                newHeaderName
                            )
                        );
                    }
                    return attr;
                }));
            }

            return t;
        }
    }
}
