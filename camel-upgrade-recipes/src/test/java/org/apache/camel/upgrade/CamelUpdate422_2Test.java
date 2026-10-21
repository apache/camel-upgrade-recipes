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
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;

/**
 * Tests for migrating from Camel 4.22.1 to 4.22.2: the Bouncy Castle 1.86 parameter spec classes used with
 * camel-pqc (CAMEL-25468), which also ship in 4.23.0. The 4.18.5 mustache and chunk header renames that the
 * 4.22.2 chain references are covered by {@link CamelUpdate418_5Test}.
 */
public class CamelUpdate422_2Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        // the chain references the camel418_5 recipes, so load the whole classpath of yaml resources
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_22_2, true)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_22, "camel-api", "camel-core-model", "camel-support", "bcprov-jdk18on"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void migratePqcParameterSpecs() {
        rewriteRun(
                mavenProject("test-pqc",
                        CamelTestUtil.pomXmlSpec("camel-pqc", CamelTestUtil.CamelVersion.v4_22_1),
                        //language=java
                        java(
                                """
                                import org.bouncycastle.pqc.jcajce.spec.CMCEParameterSpec;
                                import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
                                import org.bouncycastle.pqc.jcajce.spec.FrodoParameterSpec;
                                import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

                                public class KeyMaterial {
                                    private CMCEParameterSpec cmce;
                                    private DilithiumParameterSpec dilithium;
                                    private FrodoParameterSpec frodo;
                                    private KyberParameterSpec kyber;
                                }
                                """,
                                // at most four imports from one package per file: five would be folded into a star
                                // import, and whether that folding happens depends on the order in which the pom and
                                // the Java sources are visited once the pom bump has marked the classpath dirty
                                """
                                import org.bouncycastle.jcajce.spec.CMCEParameterSpec;
                                import org.bouncycastle.jcajce.spec.FrodoKEMParameterSpec;
                                import org.bouncycastle.jcajce.spec.MLDSAParameterSpec;
                                import org.bouncycastle.jcajce.spec.MLKEMParameterSpec;

                                public class KeyMaterial {
                                    private CMCEParameterSpec cmce;
                                    private MLDSAParameterSpec dilithium;
                                    private FrodoKEMParameterSpec frodo;
                                    private MLKEMParameterSpec kyber;
                                }
                                """
                        ),
                        //language=java
                        java(
                                """
                                import org.bouncycastle.pqc.jcajce.spec.SPHINCSPlusParameterSpec;

                                public class SignatureMaterial {
                                    private SPHINCSPlusParameterSpec sphincs;
                                }
                                """,
                                """
                                import org.bouncycastle.jcajce.spec.SLHDSAParameterSpec;

                                public class SignatureMaterial {
                                    private SLHDSAParameterSpec sphincs;
                                }
                                """
                        )
                )
        );
    }


    @Test
    void preconditionBlocksPqcSpecsWithoutDependency() {
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(CamelTestUtil.isRecipeOverridden() ? 1 : 0),
                mavenProject("test-no-pqc",
                        CamelTestUtil.pomXmlSpec("camel-core", CamelTestUtil.CamelVersion.v4_22_1),
                        //language=java
                        java(
                                """
                                import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

                                public class KeyMaterial {
                                    private KyberParameterSpec kyber;
                                }
                                """
                        )
                )
        );
    }
}
