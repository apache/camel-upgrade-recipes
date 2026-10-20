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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Guards the rule from RECIPE-CONVENTIONS.md that only the aggregates set the target Camel version:
 * {@code latest.yaml} and the {@code *LTS.yaml} files carry the {@code UpgradeDependencyVersion},
 * {@code UpgradePluginVersion}, {@code UpgradeParentVersion} and {@code ChangePropertyValue} entries
 * for the Camel coordinates. A version recipe ({@code 4.19.yaml}, {@code 4.22.1.yaml}, ...) is shared
 * by every aggregate that includes it, and OpenRewrite's {@code UpgradeDependencyVersion} never
 * downgrades, so a bump inside a version recipe pins every chain to that version and an LTS
 * aggregate can no longer target its own line.
 * <p>
 * The class is public and abstract so that both modules, and projects reusing the test-jar, can run
 * it against their own {@code META-INF/rewrite} directory.
 */
public abstract class AbstractTargetVersionOnlyInAggregatesTest {

    private static final Path REWRITE_PATH = Paths.get("src/main/resources/META-INF/rewrite");

    /** Build-time placeholders of the target versions, e.g. @camel-latest-version@ or @project.version@. */
    private static final Pattern VERSION_PLACEHOLDER = Pattern.compile("@(camel[\\w.-]*-version|project\\.version)@");
    private static final Pattern VERSION_RECIPE = Pattern.compile(
            "^- org\\.openrewrite\\.maven\\.(UpgradeDependencyVersion|UpgradePluginVersion|UpgradeParentVersion|ChangePropertyValue):$");
    private static final Pattern CAMEL_GROUP_ID = Pattern.compile("^groupId:\\s*['\"]?\\*?(org\\.apache\\.)?camel");
    private static final Pattern CAMEL_VERSION_PROPERTY = Pattern.compile("^key:\\s*['\"]?camel[\\w.-]*version");

    protected boolean isAggregate(Path yamlFile) {
        String name = yamlFile.getFileName().toString();
        return name.equals("latest.yaml") || name.endsWith("LTS.yaml");
    }

    @Test
    void versionRecipesDoNotSetTheTargetCamelVersion() throws IOException {
        List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(REWRITE_PATH)) {
            for (Path yamlFile : paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                    .filter(p -> !isAggregate(p))
                    .sorted()
                    .toList()) {
                violations.addAll(violationsIn(yamlFile));
            }
        }
        if (!violations.isEmpty()) {
            fail("\n\nOnly latest.yaml and the *LTS.yaml aggregates may set the target Camel version "
                 + "(see RECIPE-CONVENTIONS.md, 'Target Version Bumps').\n\nViolations found:\n  - "
                 + String.join("\n  - ", violations) + "\n");
        }
    }

    private static List<String> violationsIn(Path yamlFile) throws IOException {
        List<String> violations = new ArrayList<>();
        String fileName = yamlFile.getFileName().toString();
        List<String> lines = Files.readAllLines(yamlFile);
        int versionRecipeLine = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            int lineNumber = i + 1;
            if (VERSION_PLACEHOLDER.matcher(line).find()) {
                violations.add(String.format("%s:%d - target version placeholder: %s", fileName, lineNumber, line));
            }
            if (line.startsWith("- ") || line.startsWith("---") || line.isEmpty()) {
                // a new list item or document ends the current version recipe block
                versionRecipeLine = VERSION_RECIPE.matcher(line).matches() ? lineNumber : 0;
                continue;
            }
            if (versionRecipeLine > 0
                && (CAMEL_GROUP_ID.matcher(line).find() || CAMEL_VERSION_PROPERTY.matcher(line).find())) {
                violations.add(String.format("%s:%d - %s applied to a Camel coordinate or property: %s",
                        fileName, versionRecipeLine, lines.get(versionRecipeLine - 1).trim(), line));
            }
        }
        return violations;
    }
}
