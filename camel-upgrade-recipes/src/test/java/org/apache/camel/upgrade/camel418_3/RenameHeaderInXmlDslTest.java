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
package org.apache.camel.upgrade.camel418_3;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

public class RenameHeaderInXmlDslTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameHeaderInXmlDsl("kafka.TOPIC", "CamelKafkaTopic"));
    }

    @DocumentExample
    @Test
    void setHeaderMigration() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="kafka.TOPIC">
                        <constant>my-topic</constant>
                    </setHeader>
                </route>
                """,
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="CamelKafkaTopic">
                        <constant>my-topic</constant>
                    </setHeader>
                </route>
                """
            )
        );
    }

    @Test
    void headerElementMigration() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <choice>
                        <when>
                            <header name="kafka.TOPIC"/>
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
                            <header name="CamelKafkaTopic"/>
                            <to uri="mock:found"/>
                        </when>
                    </choice>
                </route>
                """
            )
        );
    }

    @Test
    void removeHeaderMigration() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <removeHeader name="kafka.TOPIC"/>
                    <to uri="mock:result"/>
                </route>
                """,
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <removeHeader name="CamelKafkaTopic"/>
                    <to uri="mock:result"/>
                </route>
                """
            )
        );
    }

    @Test
    void multipleHeadersMigration() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="kafka.TOPIC">
                        <constant>topic1</constant>
                    </setHeader>
                    <setHeader name="other.header">
                        <constant>value</constant>
                    </setHeader>
                    <removeHeader name="kafka.TOPIC"/>
                </route>
                """,
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="CamelKafkaTopic">
                        <constant>topic1</constant>
                    </setHeader>
                    <setHeader name="other.header">
                        <constant>value</constant>
                    </setHeader>
                    <removeHeader name="CamelKafkaTopic"/>
                </route>
                """
            )
        );
    }

    @Test
    void setHeaderWithSimpleExpression() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="kafka.TOPIC">
                        <simple>${header.myHeader}</simple>
                    </setHeader>
                </route>
                """,
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="CamelKafkaTopic">
                        <simple>${header.myHeader}</simple>
                    </setHeader>
                </route>
                """
            )
        );
    }

    @Test
    void doesNotMigrateDifferentHeader() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <setHeader name="kafka.PARTITION">
                        <constant>0</constant>
                    </setHeader>
                </route>
                """
                // No change expected - different header name
            )
        );
    }

    @Test
    void doesNotMigrateOtherAttributes() {
        //language=xml
        rewriteRun(
            xml(
                """
                <route xmlns="http://camel.apache.org/schema/spring">
                    <from uri="direct:start"/>
                    <to uri="kafka:kafka.TOPIC"/>
                </route>
                """
                // No change expected - "kafka.TOPIC" is not a header name attribute
            )
        );
    }
}
