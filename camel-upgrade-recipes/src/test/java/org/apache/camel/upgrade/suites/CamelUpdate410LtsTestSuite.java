package org.apache.camel.upgrade.suites;

import org.apache.camel.upgrade.*;
import org.junit.platform.suite.api.*;

@SelectClasses({CamelUpdate45Test.class, CamelUpdate46Test.class, CamelUpdate47Test.class, CamelUpdate49Test.class, CamelUpdate410Test.class})
@SelectMethod("org.apache.camel.upgrade.CamelUpdate412Test#javaDslChoice") //test for 4.10.3 to 4.10.4 java dsl - https://camel.apache.org/manual/camel-4x-upgrade-guide-4_10.html#_java_dsl
@SelectPackages({"org.apache.camel.upgrade.camel40", "org.apache.camel.upgrade.camel44", "org.apache.camel.upgrade.camel410lts"})
@Suite
@SuiteDisplayName("4.10LTS")
public class CamelUpdate410LtsTestSuite {

    @BeforeSuite
    public static void beforeSuite() {
        System.setProperty(CamelTestUtil.PROPERTY_USE_RECIPE, "org.apache.camel.upgrade.Camel410LTSMigrationRecipe");
    }

    @AfterSuite
    public static void afterSuite() {
        System.clearProperty(CamelTestUtil.PROPERTY_USE_RECIPE);
    }
}
