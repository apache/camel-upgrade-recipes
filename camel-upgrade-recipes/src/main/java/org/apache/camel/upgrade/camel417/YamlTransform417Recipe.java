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
package org.apache.camel.upgrade.camel417;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.camel.upgrade.AbstractCamelYamlVisitor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.tree.Yaml;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_17.html#_camel_core">camel-core for yaml</a>
 * </p>
 */
@EqualsAndHashCode(callSuper = false)
@Value
public class YamlTransform417Recipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Camel YML transform changes";
    }

    @Override
    public String getDescription() {
        return "Apache Camel YML DSL migration from version 4.16 o 4.17.";
    }

    private static final JsonPathMatcher pathMatcher = new JsonPathMatcher("$..route..steps.transform");

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

                //rename entry only if there is a child entry 'toType'
                if(pathMatcher.matches(getCursor()) && ((Yaml.Mapping)entry.getValue()).getEntries().stream().anyMatch(en -> en.getKey().getValue().equals("toType"))) {
                    e = entry.withKey(((Yaml.Scalar) entry.getKey().copyPaste()).withValue("transformDataType"));
                }

                return e;
            }

        };
    }

}
