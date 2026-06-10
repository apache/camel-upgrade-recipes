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
package org.apache.camel.upgrade.suites;

import org.apache.camel.upgrade.*;
import org.junit.platform.suite.api.*;

/**
 * Test suite for Camel 4.18.3 LTS migration.
 * Includes all tests from 4.17 -> 4.18.0, 4.18.0 -> 4.18.1, and 4.18.1 -> 4.18.3.
 */
@SelectClasses({
    CamelUpdate418Test.class,    // 4.17 -> 4.18.0
    CamelUpdate418_3Test.class   // 4.18.1 -> 4.18.3 (header renames)
})
// 4.18.0 -> 4.18.1 changes (from 4.19 recipes, backported to 4.18.1)
@SelectMethod("org.apache.camel.upgrade.CamelUpdate419Test#removedComponents")
@SelectMethod("org.apache.camel.upgrade.CamelUpdate419Test#sagaEipXml")
@SelectMethod("org.apache.camel.upgrade.CamelUpdate419Test#sagaEipYaml")
@SelectMethod("org.apache.camel.upgrade.CamelUpdate419Test#yamlDslRoutePolicyRename")
@Suite
@SuiteDisplayName("4.18.3 LTS")
public class CamelUpdate418LtsTestSuite {

    @BeforeSuite
    public static void beforeSuite() {
        System.setProperty(CamelTestUtil.PROPERTY_USE_RECIPE, "org.apache.camel.upgrade.Camel418LTSMigrationRecipe");
    }

    @AfterSuite
    public static void afterSuite() {
        System.clearProperty(CamelTestUtil.PROPERTY_USE_RECIPE);
    }
}
