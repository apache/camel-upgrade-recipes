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
 * Includes all tests from 4.0 to 4.18.3, covering the complete upgrade path.
 */
@SelectClasses({
    CamelUpdate45Test.class,
    CamelUpdate46Test.class,
    CamelUpdate47Test.class,
    CamelUpdate49Test.class,
    CamelUpdate410Test.class,
    CamelUpdate411Test.class,
    CamelUpdate412Test.class,
    CamelUpdate413Test.class,
    CamelUpdate414Test.class,
    CamelUpdate415Test.class,
    CamelUpdate417Test.class,
    CamelUpdate418Test.class,
    CamelUpdate418_1Test.class,
    CamelUpdate418_3Test.class
})
@SelectPackages({"org.apache.camel.upgrade.versions",
        "org.apache.camel.upgrade.camel40",
        "org.apache.camel.upgrade.camel44"
})
@Suite
@SuiteDisplayName("4.18LTS")
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
