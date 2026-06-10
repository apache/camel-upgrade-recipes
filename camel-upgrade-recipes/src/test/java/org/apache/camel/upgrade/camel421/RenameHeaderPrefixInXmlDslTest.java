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
package org.apache.camel.upgrade.camel421;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

public class RenameHeaderPrefixInXmlDslTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameHeaderPrefixInXmlDsl("SolrField.", "CamelSolrField."));
    }

    @DocumentExample
    @Test
    void testSetHeaderPrefixMigration() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="SolrField.id">
                        <constant>doc123</constant>
                    </setHeader>
                </route>
                """,
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="CamelSolrField.id">
                        <constant>doc123</constant>
                    </setHeader>
                </route>
                """
            )
        );
    }

    @Test
    void testHeaderElementPrefixMigration() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <choice>
                        <when>
                            <header name="SolrField.id"/>
                            <to uri="mock:found"/>
                        </when>
                    </choice>
                </route>
                """,
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <choice>
                        <when>
                            <header name="CamelSolrField.id"/>
                            <to uri="mock:found"/>
                        </when>
                    </choice>
                </route>
                """
            )
        );
    }

    @Test
    void testRemoveHeaderPrefixMigration() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <removeHeader name="SolrField.id"/>
                    <to uri="mock:result"/>
                </route>
                """,
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <removeHeader name="CamelSolrField.id"/>
                    <to uri="mock:result"/>
                </route>
                """
            )
        );
    }

    @Test
    void testMultipleHeadersPrefixMigration() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="SolrField.id">
                        <constant>123</constant>
                    </setHeader>
                    <setHeader name="SolrField.name">
                        <constant>Test</constant>
                    </setHeader>
                    <to uri="mock:result"/>
                </route>
                """,
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="CamelSolrField.id">
                        <constant>123</constant>
                    </setHeader>
                    <setHeader name="CamelSolrField.name">
                        <constant>Test</constant>
                    </setHeader>
                    <to uri="mock:result"/>
                </route>
                """
            )
        );
    }

    @Test
    void testDoesNotMigrateDifferentPrefix() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="SolrParam.commit">
                        <constant>true</constant>
                    </setHeader>
                </route>
                """
                // No change expected
            )
        );
    }

    @Test
    void testDoesNotMigrateArbitraryAttributes() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <to uri="solr://localhost?field=SolrField.id"/>
                </route>
                """
                // No change expected
            )
        );
    }

    @Test
    void testMigratesVariousFieldNames() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="SolrField.id"><constant>1</constant></setHeader>
                    <setHeader name="SolrField.name"><constant>Test</constant></setHeader>
                    <setHeader name="SolrField.description"><constant>Desc</constant></setHeader>
                    <setHeader name="SolrField.custom_field_123"><constant>Custom</constant></setHeader>
                </route>
                """,
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="CamelSolrField.id"><constant>1</constant></setHeader>
                    <setHeader name="CamelSolrField.name"><constant>Test</constant></setHeader>
                    <setHeader name="CamelSolrField.description"><constant>Desc</constant></setHeader>
                    <setHeader name="CamelSolrField.custom_field_123"><constant>Custom</constant></setHeader>
                </route>
                """
            )
        );
    }
}
