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
package org.apache.camel.upgrade.camel47;

import org.apache.camel.upgrade.AbstractCamelXmlVisitor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_7.html#_dsl">XML DSL</a>
 * </p>
 * The Load Balancer EIP has aligned naming and the following balancers has been renamed in XML and YAML DSL:
 */
public class XmlDsl47Recipe extends Recipe {

    private static final Map<XPathMatcher, String> transformations = Map.of(
            new XPathMatcher("//loadBalance/failover"), "failoverLoadBalancer",
            new XPathMatcher("//loadBalance/random"), "randomLoadBalancer",
            new XPathMatcher("//loadBalance/roundRobin"), "roundRobinLoadBalancer",
            new XPathMatcher("//loadBalance/sticky"), "stickyLoadBalancer",
            new XPathMatcher("//loadBalance/topic"), "topicLoadBalancer",
            new XPathMatcher("//loadBalance/weighted"), "weightedLoadBalancer"
    );

    @Override
    public String getDisplayName() {
        return "Camel XMl DSL changes";
    }

    @Override
    public String getDescription() {
        return "Apache Camel XML DSL migration from version 4.6 o 4.7.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractCamelXmlVisitor() {

            @Override
            public Xml.Tag doVisitTag(final Xml.Tag tag, final ExecutionContext ctx) {
                Xml.Tag t = super.doVisitTag(tag, ctx);

                //rename tags
                return transformations.entrySet().stream()
                        .filter(e -> e.getKey().matches(getCursor()))
                        //rename tag
                        .map(e -> t.withName(e.getValue()))
                        .findAny()
                        .orElse(t);
            }
        };
    }
}
