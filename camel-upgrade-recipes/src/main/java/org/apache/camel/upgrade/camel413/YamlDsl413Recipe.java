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
package org.apache.camel.upgrade.camel413;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.camel.upgrade.AbstractCamelYamlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.tree.Yaml;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_13.html#_camel_yaml_dsl">YML DSL</a>
 * </p>
 * Kebab-case is changed to camelCase.
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class YamlDsl413Recipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Camel YML DSL changes";
    }

    @Override
    public String getDescription() {
        return "Apache Camel YML DSL migration from version 4.12 o 4.13.";
    }

    private static JsonPathMatcher FURY_MARSHALL_PATH = new JsonPathMatcher("$..marshal.fury");

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

                //switch kebabC-case to camelCase if possible
                if(e.getKey() instanceof Yaml.Scalar) {
                    String origValue = e.getKey().getValue();
                    String camelCase = RecipesUtil.kebabCaseToCamelCase(origValue);
                    if(camelCase != null && !camelCase.equals(origValue)) {
                        return e.withKey(((Yaml.Scalar) e.getKey().copyPaste()).withValue(camelCase));
                    }
                }

                String path = RecipesUtil.getProperty(getCursor());
                //yaml fury marshal
                if(path.endsWith("unmarshal.fury") || path.endsWith("marshal.fury")) {
                    return e.withKey(((Yaml.Scalar) e.getKey().copyPaste()).withValue("fory"));
                }

                return e;
            }

        };
    }

}
