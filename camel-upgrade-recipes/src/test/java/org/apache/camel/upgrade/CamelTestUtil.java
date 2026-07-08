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

import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.SourceSpecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.openrewrite.maven.Assertions.pomXml;

public class CamelTestUtil {
    public static final String PROPERTY_USE_RECIPE = "camelUpgradeRecipes-useRecipe";

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelTestUtil.class);

    /**
     * Enumeration of the Camel version, with precise versions of dependencies and the name of the recipe
     */
    public enum CamelVersion {
        v3_18(3, 18, 6),
        v4_0(4, 0, 3),
        v4_4(4, 4, 2),
        v4_5(4, 5, 0),
        v4_6(4, 6, 0),
        v4_7(4, 7, 0),
        v4_8(4, 8, 0),
        v4_9(4, 9, 0),
        v4_10(4, 10, 0),
        v4_10_4(4, 10, 4, true),
        v4_11(4, 11, 0),
        v4_12(4, 12, 0),
        v4_13(4, 13, 0),
        v4_14(4, 14, 0),
        v4_15(4, 15, 0),
        v4_16(4, 16, 0),
        v4_17(4, 17, 0),
        v4_18(4, 18, 0),
        v4_18_1(4, 18, 1, true),
        v4_18_3(4, 18, 3, true),
        v4_19(4, 19, 0),
        v4_20(4, 20, 0),
        v4_21(4, 21, 0);

        private int major;
        private int minor;
        private int patch;
        private boolean lts;

        CamelVersion(int major, int minor, int patch) {
            this(major, minor, patch, false);
        }

        CamelVersion(int major, int minor, int patch, boolean lts) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.lts = lts;
        }

        public String getVersion() {
            return getRecipeFile() + "." + patch;
        }

        private String getRecipeFile() {
            if(lts) {
                return major + "." + minor + "." + patch;
            }
            return major + "." + minor;
        }

        public String getYamlFile() {
            return "/META-INF/rewrite/" + getRecipeFile() + ".yaml";
        }

        public String getRecipe() {
            if(lts) {
                return "org.apache.camel.upgrade.camel" + major + minor + "_" + patch + ".CamelMigrationRecipe";
            }
            return "org.apache.camel.upgrade.camel" + major + minor + ".CamelMigrationRecipe";

        }

    }

    public static RecipeSpec recipe(RecipeSpec spec, CamelVersion to, String... activeRecipes) {
        return recipe(spec, to, false, activeRecipes);
    }
    public static RecipeSpec recipe(RecipeSpec spec, CamelVersion to, boolean useAllRecipes, String... activeRecipes) {
        String useRecipe = System.getProperty(CamelTestUtil.PROPERTY_USE_RECIPE);
        if (useRecipe != null && !useRecipe.isEmpty()) {
            return spec.recipeFromResources(useRecipe);
        }
        if (activeRecipes == null || activeRecipes.length == 0) {
            if (useAllRecipes) {
                return spec.recipeFromResources(to.getRecipe());
            }
            return spec.recipeFromResource(to.getYamlFile(), to.getRecipe());
        }
        return spec.recipeFromResource(to.getYamlFile(), activeRecipes);
    }

    public static Parser.Builder parserFromClasspath(CamelVersion from, String... classpath) {
        List<String> resources = Arrays.stream(classpath).map(cl -> {
              if (cl.startsWith("camel-")) {
                  String maxVersion = cl + "-" + from.getVersion();
                  //find the highest version lesser or equals the required one
                  Path path = Paths.get("target", "test-classes", "META-INF", "rewrite", "classpath");
                  Optional<String> dependency = Arrays.stream(path.toFile().listFiles())
                    .filter(f -> f.getName().startsWith(cl))
                    .map(f -> f.getName().substring(0, f.getName().lastIndexOf(".")))
                    //filter out or higher version the requested
                    .filter(f -> f.compareTo(maxVersion) <= 0)
                    .sorted(Comparator.reverseOrder())
                    .findFirst();

                  if (dependency.isEmpty()) {
                      LOGGER.warn("Dependency not found in classpath: {}", cl);
                  }

                  return dependency.orElse(null);
              }
              return cl;
          })
          .filter(Objects::nonNull)
          .toList();

        return JavaParser.fromJavaVersion()
          .logCompilationWarningsAndErrors(true)
          .classpathFromResources(new InMemoryExecutionContext(), resources.toArray(new String[0]));
    }

    /**
     * Gets the quarkus version from the versions.properties file. (which is populated during buildtime)
     *
     * @return the project version
     */
    public static String getCamelLatestVersion() {
        return getString(
                "camel.latest.version", "Could not determine camel latest version from properties file.");
    }
    public static String getCamel410LtsVersion() {
        return getString(
                "camel4.10.lts.version", "Could not determine 4.10 lts version from properties file.");
    }
    public static String getCamel418LtsVersion() {
        return getString(
                "camel4.18.lts.version", "Could not determine 4.18 lts version from properties file.");
    }


    /**
     * Reads property from the file versions.properties (which contains build time resolved versions)
     */
    public static String getString(String key, String error) {
        try {
            java.util.Properties properties = new java.util.Properties();
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("versions.properties")) {
                if (is != null) {
                    properties.load(is);
                    String version = properties.getProperty(key);
                    if (version != null && !version.trim().isEmpty()) {
                        return version;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(error, e);
        }

        // Ultimate fallback
        return "";
    }

    /**
     * Helper method to create a minimal pom.xml with a Camel component dependency.
     * This is required for the ModuleHasDependency precondition to work in tests.
     *
     * @param artifactId the Camel component artifact ID (e.g., "camel-kafka")
     * @param version the Camel version to use for the dependency
     * @return a minimal pom.xml string with the specified dependency
     */
    public static String pomXmlWithDependency(String artifactId, CamelVersion version) {
        return """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <properties>                                                                                     \s
                        <maven.compiler.release>17</maven.compiler.release>                                          \s
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.camel</groupId>
                            <artifactId>%s</artifactId>
                            <version>%s</version>
                        </dependency>
                    </dependencies>
                </project>
                """.formatted(artifactId, version.getVersion());
    }

    /**
     * Returns the target Camel version when running under a suite recipe that includes
     * UpgradeDependencyVersion, or null when no version upgrade is expected.
     */
    private static String getTargetVersionFromRecipe() {
        String useRecipe = System.getProperty(PROPERTY_USE_RECIPE);
        if (useRecipe == null || useRecipe.isEmpty()) {
            return null;
        }
        switch (useRecipe) {
            case "org.apache.camel.upgrade.CamelMigrationRecipe":
                return getCamelLatestVersion();
            case "org.apache.camel.upgrade.Camel418LTSMigrationRecipe":
                return getCamel418LtsVersion();
            case "org.apache.camel.upgrade.Camel410LTSMigrationRecipe":
                return getCamel410LtsVersion();
            default:
                return null;
        }
    }

    public static boolean isRecipeOverridden() {
        return getTargetVersionFromRecipe() != null;
    }

    /**
     * Creates a pomXml SourceSpecs that expects the camel dependency version to be upgraded
     * when running under a suite recipe (UpgradeDependencyVersion), or no POM change when
     * running standalone.
     */
    public static SourceSpecs pomXmlSpec(String artifactId, CamelVersion sourceVersion) {
        return pomXmlSpec(pomXmlWithDependency(artifactId, sourceVersion), sourceVersion.getVersion());
    }

    /**
     * Creates a pomXml SourceSpecs from a raw POM string, expecting the camel dependency version
     * to be upgraded when running under a suite recipe.
     */
    public static SourceSpecs pomXmlSpec(String pomBefore, String sourceVersion) {
        String targetVersion = getTargetVersionFromRecipe();
        if (targetVersion != null && !targetVersion.equals(sourceVersion)) {
            String pomAfter = pomBefore.replace(
                    "<version>" + sourceVersion + "</version>",
                    "<version>" + targetVersion + "</version>"
            );
            return pomXml(pomBefore, pomAfter);
        }
        return pomXml(pomBefore);
    }

}
