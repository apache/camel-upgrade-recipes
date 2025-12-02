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
import org.apache.camel.upgrade.AbstractCamelYamlVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.tree.Yaml;

@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@AllArgsConstructor
@Setter
public class ReplacePropertyInDataFormatYaml extends Recipe {

    @Option(example = "TODO Provide a usage example for the docs", displayName = "Component",
            description = "Component name.")
    String component;

    @Option(example = "TODO Provide a usage example for the docs", displayName = "Old property key",
            description = "The property key to rename.")
    String oldPropertyKey;

    @Option(example = "TODO Provide a usage example for the docs", displayName = "New prefix before any group",
            description = "The prefix to be replaced with.")
    String newPropertyKey;


    @Override
    public String getDisplayName() {
        return "Renames property of the component";
    }

    @Override
    public String getDescription() {
        return "ARenames property of the component.";
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


                String path = RecipesUtil.getProperty(getCursor());
                //yaml marshal
                if(path.endsWith("dataFormats." + component + "." + oldPropertyKey)) {
                    return e.withKey(((Yaml.Scalar) e.getKey().copyPaste()).withValue(newPropertyKey));
                }                //yaml marshal
                if(path.endsWith("marshal." + component + "." + oldPropertyKey)) {
                    return e.withKey(((Yaml.Scalar) e.getKey().copyPaste()).withValue(newPropertyKey));
                }                //yaml marshal
                if(path.endsWith("unmarshal." + component + "." + oldPropertyKey)) {
                    return e.withKey(((Yaml.Scalar) e.getKey().copyPaste()).withValue(newPropertyKey));
                }

                return e;
            }

        };
    }

}
