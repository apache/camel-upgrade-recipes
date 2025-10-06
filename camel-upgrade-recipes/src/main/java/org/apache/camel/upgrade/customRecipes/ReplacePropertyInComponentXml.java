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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.camel.upgrade.AbstractCamelXmlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@AllArgsConstructor
@Setter
public class ReplacePropertyInComponentXml extends Recipe {

    private static final XPathMatcher FROM_MATCHER = new XPathMatcher("//route/from");
    private static final XPathMatcher TO_MATCHER = new XPathMatcher("//route/to");


    @Option(example = "TODO Provide a usage example for the docs", displayName = "Component",
            description = "Component name.")
    String component;

    @Option(example = "TODO Provide a usage example for the docs", displayName = "Old property key",
            description = "The property key to rename.")
    String oldPropertyKey;

    @Option(example = "TODO Provide a usage example for the docs", displayName = "New prefix before any group",
            description = "The prefix to be replaced with.")
    String newPropertyKey;

    @Option(example = "file:", displayName = "New prefix before value of the changed property",
            description = "This value is appended before the current value of the modified method.",
            required = false)
    String valuePrefix;

    @Override
    public String getDisplayName() {
        return "Camel XMl DSL changes";
    }

    @Override
    public String getDescription() {
        return "Apache Camel XML DSL migration from version 4.9 o 4.10.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractCamelXmlVisitor() {

            @Override
            public Xml.Tag doVisitTag(final Xml.Tag tag, final ExecutionContext ctx) {
                Xml.Tag t = super.doVisitTag(tag, ctx);

                if(FROM_MATCHER.matches(getCursor())) {
                    return replacePropertyIfPossible(t);
                }
                if(TO_MATCHER.matches(getCursor())) {
                    return replacePropertyIfPossible(t);
                }

                return t;
            }
        };
    }

    private Xml.Tag replacePropertyIfPossible(final Xml.Tag tag) {

        List<Xml.Attribute> attributes = new ArrayList<>(tag.getAttributes());

        Optional<Xml.Attribute> uri = attributes.stream().filter(a -> "uri".equals(a.getKey().getName())).findAny();
        if(uri.isPresent() && (component.equals(uri.get().getValue().getValue()) || uri.get().getValue().getValue().startsWith(component + ":"))) {
            String u = uri.get().getValue().getValue();
            //replace property and apply optionalPrefix
            u = RecipesUtil.replacePropertyInUrl(u, component, oldPropertyKey, newPropertyKey, valuePrefix);
            if(u != null) {
                attributes.remove(uri.get());
                attributes.add(uri.get().withValue(uri.get().getValue().withValue(u)));
                return tag.withAttributes(attributes);
            }
        }
        return tag;


    }
}
