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
package org.apache.camel.updates.camel46;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.camel.updates.AbstractCamelYamlVisitor;
import org.apache.camel.updates.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Fixes following yaml change.
 *
 * When using YAML DSL to define properties on bean then property has been removed in favour of using properties.
 *
 * The old syntax:
 *
 * <pre>
 *     beans:
 *        - name: "myProcessor"
 *          type: "#class:com.foo.MyClass"
 *          property:
 *            - key: "payload"
 *              value: "test-payload"
 * </pre>
 *
 * should be changed to:
 *
 * <pre>
 *   beans:
 *      - name: "myProcessor"
 *        type: "#class:com.foo.MyClass"
 *        properties:
 *          payload: "test-payload"
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class YamlDsl46Recipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Bean property to properties.";
    }

    @Override
    public String getDescription() {
        return "Each bean property is changed to bean properties.";
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
                //resolve path
                String path = RecipesUtil.getProperty(getCursor());

                //remove unnecessary children and switch Sequence to Mapping
                if(path.endsWith("beans.property")) {
                    //it is intentional to fail if the casting is wrong, in that case the Abstract*Visitor
                    //logs warning about failure and no migration is applied
                    Yaml.Mapping m = (Yaml.Mapping) ((Yaml.Sequence)e.getValue()).getEntries().get(0).getBlock();
                    //apply correct prefix for the new 'properties' values
                    String prefix = e.getPrefix() + "  ";
                    List<Yaml.Mapping.Entry> entries = ((Yaml.Sequence)e.getValue()).getEntries().stream()
                            //iterate over all blocks
                            .flatMap(entry1 -> ((Yaml.Mapping)entry1.getBlock()).getEntries().stream())
                            //apply correct prefix
                            .map(entry2 -> entry2.withPrefix(prefix))
                            .collect(Collectors.toList());

                    return e.withKey(((Yaml.Scalar) e.getKey().copyPaste()).withValue("properties"))
                            .withValue(m.copyPaste().withEntries(entries));
                }


                //property key is saved
                if(path.matches(".*beans.property.key") && e.getValue() instanceof Yaml.Scalar) {
                    //save value into parent context, so the value can reach it
                    getCursor().getParent(4).putMessage("key", ((Yaml.Scalar) e.getValue()).getValue());
                    //return original key, which will be removed in the code above
                    return null;
                }
                //property 'value' is replaced by the key from previous
                if(path.matches(".*beans.property.value")  && e.getValue() instanceof Yaml.Scalar && getCursor().getNearestMessage("key") != null) {
                    String key = getCursor().getNearestMessage("key");
                    if(key != null) {
                        return e.withKey(((Yaml.Scalar) e.getKey().copyPaste()).withValue(key));
                    }
                }

                return e;
            }

        };
    }

}
