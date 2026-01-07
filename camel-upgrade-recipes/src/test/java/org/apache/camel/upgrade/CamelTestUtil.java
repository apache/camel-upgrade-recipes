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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
        v4_17(4, 17, 0);

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
        String useRecipe = System.getProperty(CamelTestUtil.PROPERTY_USE_RECIPE);
        if (useRecipe != null && !useRecipe.isEmpty()) {
            return spec.recipeFromResources(useRecipe);
        }
        if (activeRecipes == null || activeRecipes.length == 0) {
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

}
