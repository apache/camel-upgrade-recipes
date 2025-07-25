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
package org.apache.camel.upgrade.camel46;

import org.apache.camel.upgrade.AbstractCamelXmlVisitor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_6.html#_xml_dsl">XML DSL</a>
 * </p>
 * When using XML DSL to define properties on <bean> then <property> must now be declared inside <properties>.
 */
public class XmlDsl46Recipe extends Recipe {

    private static final XPathMatcher BEAN_PROPERTY_XPATH_MATCHER = new XPathMatcher("bean/property");
    private static final XPathMatcher BEAN_XPATH_MATCHER = new XPathMatcher("bean");

    @Override
    public String getDisplayName() {
        return "Camel XMl DSL changes";
    }

    @Override
    public String getDescription() {
        return "Apache Camel XML DSL migration from version 3.20 or higher to 4.0.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractCamelXmlVisitor() {

            @Override
            public  Xml.@Nullable Tag doVisitTag(final Xml.Tag tag, final ExecutionContext ctx) {
                Xml.Tag t = super.doVisitTag(tag, ctx);

                //save all properties into a list placed to the bean tag
                //and the first property rename to properties and strip content
                if (BEAN_PROPERTY_XPATH_MATCHER.matches(getCursor())) {

                    List<Xml.Tag> sb = getCursor().getParent().getMessage("properties");
                    if(sb == null) {
                        sb = new LinkedList<>();
                        getCursor().getParent().putMessage("properties", sb);
                        getCursor().getParent().putMessage("propertiesPrefix", t.getPrefix());
                    }
                    //make prefix bigger, as those values would come to the new nested level
                    sb.add(t.withPrefix(t.getPrefix() + "  "));
                    //skip property
                    return null;
                }
                if (BEAN_XPATH_MATCHER.matches(getCursor()) && getCursor().getMessage("properties") != null) {
                    //save description into context for parent
                    List<Xml.Tag> sb = getCursor().getMessage("properties");
                    String prefix = getCursor().getMessage("propertiesPrefix");
                    if(sb != null) {
                        List<Content> content = new LinkedList<>();
                        content.addAll(t.getContent());
                        content.add(Xml.Tag.build("<properties/>").withPrefix(prefix).withContent(sb));
                        return t.withContent(content);

                    }
                }

                return t;
            }

        };
    }
}
