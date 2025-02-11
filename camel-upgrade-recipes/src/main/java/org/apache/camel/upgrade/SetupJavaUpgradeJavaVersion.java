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

import org.openrewrite.*;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.YamlVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetupJavaUpgradeJavaVersion extends Recipe {

    @Option(displayName = "Java version",
            description = "The Java version to upgrade to.",
            example = "17")
    Integer minimumJavaMajorVersion;

    @Override
    public String getDisplayName() {
        return "Upgrade `actions/setup-java` `java-version`";
    }

    @Override
    public String getDescription() {
        return "Update the Java version used by `actions/setup-java` if it is below the expected version number.";
    }

    private static final JsonPathMatcher javaVersion = new JsonPathMatcher("..steps[?(@.uses =~ 'actions/setup-java@v*.*')].with.java-version");
    private static final Pattern javaVersionPattern = Pattern.compile("([0-9]+)(\\.[0-9]+)*([-+].*)?");

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new FindSourceFiles(".github/workflows/*.yml"),
                new YamlVisitor<>() {
                    @Override
                    public Yaml visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
                        if (!javaVersion.matches(getCursor())) {
                            return super.visitMappingEntry(entry, ctx);
                        }

                        Yaml.Scalar currentValue = (Yaml.Scalar) entry.getValue();

                        // specific versions are allowed by `actions/setup-java`
                        Matcher matcher = javaVersionPattern.matcher(currentValue.getValue());
                        if (!matcher.matches()) {
                            return super.visitMappingEntry(entry, ctx);
                        }

                        int currentMajorVersion;
                        try {
                            currentMajorVersion = Integer.parseInt(matcher.group(1));
                        } catch (NumberFormatException ex) {
                            return super.visitMappingEntry(entry, ctx);
                        }

                        if (currentMajorVersion >= minimumJavaMajorVersion) {
                            return super.visitMappingEntry(entry, ctx);
                        }

                        return super.visitMappingEntry(
                                entry.withValue(currentValue.withValue(String.valueOf(minimumJavaMajorVersion))),
                                ctx
                        );
                    }
                });
    }

    public Integer getMinimumJavaMajorVersion() {
        return minimumJavaMajorVersion;
    }

    public void setMinimumJavaMajorVersion(Integer minimumJavaMajorVersion) {
        this.minimumJavaMajorVersion = minimumJavaMajorVersion;
    }
}
