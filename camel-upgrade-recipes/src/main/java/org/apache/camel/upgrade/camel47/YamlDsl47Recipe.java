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

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.camel.upgrade.AbstractCamelYamlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.tree.Yaml;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_7.html#_dsl">YML DSL</a>
 * </p>
 * The Load Balancer EIP has aligned naming and the following balancers has been renamed in XML and YAML DSL:
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class YamlDsl47Recipe extends Recipe {


    private static final Map<JsonPathMatcher, String> transformations = Map.of(
            new JsonPathMatcher("$..loadBalance.failover"), "failoverLoadBalancer",
            new JsonPathMatcher("$..loadBalance.random"), "randomLoadBalancer",
            new JsonPathMatcher("$..loadBalance.roundRobin"), "roundRobinLoadBalancer",
            new JsonPathMatcher("$..loadBalance.sticky"), "stickyLoadBalancer",
            new JsonPathMatcher("$..loadBalance.topic"), "topicLoadBalancer",
            new JsonPathMatcher("$..loadBalance.weighted"), "weightedLoadBalancer"
    );

    @Override
    public String getDisplayName() {
        return "Camel YML DSL changes";
    }

    @Override
    public String getDescription() {
        return "Apache Camel YML DSL migration from version 4.6 o 4.7.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new AbstractCamelYamlVisitor() {

            @Override
            protected void clearLocalCache() {
                //nothing to do
            }

            @Override
            public Yaml.Mapping.Entry doVisitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
                Yaml.Mapping.Entry e = super.doVisitMappingEntry(entry, ctx);

                //rename entry
                return transformations.entrySet().stream()
                        .filter(en -> en.getKey().matches(getCursor()))
                        //rename tag
                        .map(en -> e.withKey(((Yaml.Scalar) e.getKey().copyPaste()).withValue(en.getValue())))
                        .findAny()
                        .orElse(e);
            }

        };
    }

}
