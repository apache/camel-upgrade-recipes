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
package org.apache.camel.upgrade.camel410lts;

import org.apache.camel.upgrade.AbstractCamelUpdateVersionTest;
import org.apache.camel.upgrade.CamelTestUtil;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

//class has to stay public, because test is extended in project quarkus-updates
@EnabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = "org.apache.camel.upgrade.Camel410LTSMigrationRecipe")
public class CamelUpdate410LtsVersionTest extends AbstractCamelUpdateVersionTest {


    @Override
    protected String targetVersion() {
        return System.getProperty("camel4.10-lts-version");
    }
}
