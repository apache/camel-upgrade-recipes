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
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.properties.PropertiesVisitor;
import org.openrewrite.properties.tree.Properties;

import java.util.ArrayList;
import java.util.List;

/**
 * Replaces prefix with the new one and changes the suffix tp start with lower case
 */
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@AllArgsConstructor
@Setter
public class ChangePropertyKeyWithCaseChange extends Recipe {

    @Option(example = "TODO Provide a usage example for the docs", displayName = "Old property key",
            description = "The property key to rename.")
    String oldPropertyKey;

    @Option(example = "TODO Provide a usage example for the docs", displayName = "New prefix before any group",
            description = "The prefix to be replaced with.")
    String newPrefix;

    @Option(displayName = "Exclusions",
            description = "Regexp for exclusions",
            example = "camel.springboot.main-run-controller")
    List<String> exclusions = new ArrayList<>();

    @Override
    public String getDisplayName() {
        return "Change prefix of property with Camel case";
    }

    @Override
    public String getDescription() {
        return "Change prefix of property with Camel case";
    }

    @Override
    public PropertiesVisitor<ExecutionContext> getVisitor() {
        return new PropertiesVisitor<>() {
            @Override
            public Properties visitEntry(Properties.Entry entry, ExecutionContext ctx) {
                for (String exclusion : exclusions) {
                    if (entry.getKey().equals(exclusion)) {
                        return super.visitEntry(entry, ctx);
                    }
                }

                if (entry.getKey().matches(oldPropertyKey)) {
                    entry = entry.withKey(getKey(entry))
                            .withPrefix(entry.getPrefix());
                }
                return super.visitEntry(entry, ctx);
            }

            //replace key
            private String getKey(Properties.Entry entry) {
                return newPrefix + entry.getKey().replaceFirst(oldPropertyKey, "$1").substring(0, 1).toLowerCase() +
                       entry.getKey().replaceFirst(oldPropertyKey, "$1").substring(1);

            }
        };
    }
}
