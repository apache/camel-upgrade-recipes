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
 * Renames header prefixes in XML DSL <setHeader name="..."> and <header name="..."> elements.
 */
public class RenameHeaderPrefixInXmlDsl extends Recipe {

    @Option(displayName = "Old header prefix",
            description = "The old header prefix",
            example = "SolrField.")
    String oldPrefix;

    @Option(displayName = "New header prefix",
            description = "The new header prefix",
            example = "CamelSolrField.")
    String newPrefix;

    public RenameHeaderPrefixInXmlDsl() {
    }

    public RenameHeaderPrefixInXmlDsl(String oldPrefix, String newPrefix) {
        this.oldPrefix = oldPrefix;
        this.newPrefix = newPrefix;
    }

    public void setOldPrefix(String oldPrefix) {
        this.oldPrefix = oldPrefix;
    }

    public void setNewPrefix(String newPrefix) {
        this.newPrefix = newPrefix;
    }

    @Override
    public String getDisplayName() {
        return "Rename header prefix in XML DSL";
    }

    @Override
    public String getDescription() {
        return "Renames header prefixes in XML DSL <setHeader name=\"...\">, <header name=\"...\">, " +
               "and <removeHeader name=\"...\"> elements.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new XmlHeaderPrefixVisitor(oldPrefix, newPrefix);
    }

    private static class XmlHeaderPrefixVisitor extends AbstractCamelXmlVisitor {
        private final String oldPrefix;
        private final String newPrefix;

        XmlHeaderPrefixVisitor(String oldPrefix, String newPrefix) {
            this.oldPrefix = oldPrefix;
            this.newPrefix = newPrefix;
        }

        @Override
        public Xml.Tag doVisitTag(Xml.Tag tag, ExecutionContext ctx) {
            Xml.Tag t = super.doVisitTag(tag, ctx);

            // Check if this is a setHeader, header, or removeHeader tag
            String tagName = t.getName();
            if ("setHeader".equals(tagName) || "header".equals(tagName) || "removeHeader".equals(tagName)) {
                // Look for the "name" attribute starting with oldPrefix
                return t.withAttributes(ListUtils.map(t.getAttributes(), attr -> {
                    if ("name".equals(attr.getKeyAsString())) {
                        String headerName = attr.getValueAsString();
                        if (headerName != null && headerName.startsWith(oldPrefix)) {
                            // Replace the prefix
                            String newHeaderName = newPrefix + headerName.substring(oldPrefix.length());
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
                    }
                    return attr;
                }));
            }

            return t;
        }
    }
}
