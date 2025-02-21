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
package org.apache.camel.upgrade;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.properties.PropertiesVisitor;
import org.openrewrite.properties.tree.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A recipe that changes property keys in property files by replacing a specified key prefix
 * and applying CamelCase formatting to any remaining suffix.
 */
public class PatternMatcherChangePropertyKey extends Recipe {

    @Option(displayName = "Old property key",
            description = "The property key to rename.",
            example = "management.metrics.binders.files.enabled")
    String oldPropertyKey;

    @Option(displayName = "New property key",
            description = "The new name for the key identified by `oldPropertyKey`.",
            example = "management.metrics.enable.process.files")
    String newPropertyKey;

    @Option(displayName = "Exclusions",
            description = "Regexp for exclusions",
            example = "camel.springboot.main-run-controller")
    List<String> exclusions = new ArrayList<>();

    public PatternMatcherChangePropertyKey() {}

    public PatternMatcherChangePropertyKey(String oldPropertyKey, String newPropertyKey, List<String> exclusions) {
        this.oldPropertyKey = oldPropertyKey;
        this.newPropertyKey = newPropertyKey;
        this.exclusions = exclusions == null ? new ArrayList<>() : exclusions;
    }

    @Override
    public String getDisplayName() {
        return "Change property key and apply CamelCase format";
    }

    @Override
    public String getDescription() {
        return "Change property key applying CamelCase format leaving the value intact.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new PatternMatcherChangePropertyKey.ChangePropertyKeyVisitor<>();
    }

    public class ChangePropertyKeyVisitor<P> extends PropertiesVisitor<P> {
        public ChangePropertyKeyVisitor() {
        }

        @Override
        public Properties visitEntry(Properties.Entry entry, P p) {
            for (String exclusion : exclusions) {
                if (entry.getKey().equals(exclusion)) {
                    return super.visitEntry(entry, p);
                }
            }

            Pattern pattern = Pattern.compile("(" + oldPropertyKey + ")" + "(.*)");
            Matcher matcher = pattern.matcher(entry.getKey());
            if (matcher.find()) {
                if(matcher.group(2).startsWith(".")) {
                    entry = entry.withKey(newPropertyKey + matcher.group(2));
                } else {
                    String newSuffix =
                            matcher.group(2).substring(0, 1).toLowerCase() + matcher.group(2).substring(1);

                    entry = entry.withKey(newPropertyKey + "." + newSuffix);
                }
            }

            return super.visitEntry(entry, p);
        }
    }

    public String getOldPropertyKey() {
        return oldPropertyKey;
    }

    public void setOldPropertyKey(String oldPropertyKey) {
        this.oldPropertyKey = oldPropertyKey;
    }

    public String getNewPropertyKey() {
        return newPropertyKey;
    }

    public void setNewPropertyKey(String newPropertyKey) {
        this.newPropertyKey = newPropertyKey;
    }

    public List<String> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
    }
}
