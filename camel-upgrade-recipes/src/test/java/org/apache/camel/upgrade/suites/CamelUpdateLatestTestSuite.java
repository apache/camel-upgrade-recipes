package org.apache.camel.upgrade.suites;

import org.apache.camel.upgrade.CamelTestUtil;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.platform.suite.api.*;

@DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
@Suite
@SuiteDisplayName("LATEST")
@SelectPackages("org.apache.camel.upgrade")
public class CamelUpdateLatestTestSuite {

    @BeforeSuite
    public static void beforeSuite() {
        System.setProperty(CamelTestUtil.PROPERTY_USE_RECIPE, "org.apache.camel.upgrade.CamelMigrationRecipe");
    }

    @AfterSuite
    public static void afterSuite() {
        System.clearProperty(CamelTestUtil.PROPERTY_USE_RECIPE);
    }
}
