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
package org.apache.camel.upgrade.xml;

import org.apache.camel.upgrade.CamelTestUtil;
import org.apache.camel.upgrade.camel410.XmlDsl410Recipe;
import org.apache.camel.upgrade.camel418_1.XmlDsl418_1SagaRecipe;
import org.apache.camel.upgrade.camel43.CamelResequenceEIPXmlRecipe;
import org.apache.camel.upgrade.camel418_3.RenameHeaderInXmlDsl;
import org.apache.camel.upgrade.camel418_3.RenameHeaderPrefixInXmlDsl;
import org.apache.camel.upgrade.camel46.XmlDsl46Recipe;
import org.apache.camel.upgrade.camel47.XmlDsl47Recipe;
import org.apache.camel.upgrade.customRecipes.ReplacePropertyInComponentXml;
import org.apache.camel.upgrade.customRecipes.ReplacePropertyInDataFormatXml;
import org.apache.camel.upgrade.customRecipes.internal.ChangeXmlComponentUriRecipe;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.xml.Assertions.xml;

/**
 * Every XML DSL recipe is gated on {@code RecipesUtil.camelXmlDslPrecondition()}, so none of them may
 * touch a document that is not Camel XML. Each test feeds a document carrying exactly the shape the
 * recipe matches, wrapped in a root element that is plainly not Camel, and expects no change.
 * <p>
 * {@code camel40.xml.XmlDslRecipe} has no test here on purpose: its matchers are anchored at
 * {@code /routes/route}, so the document root has to be {@code routes}, which is a Camel root.
 */
class CamelXmlDslPreconditionTest implements RewriteTest {

    @DocumentExample
    @Test
    void springBeanDefinitionIsNotADataFormatOrHeaderCarrier() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new XmlDsl46Recipe()),
            xml(
                """
                <beans xmlns="http://www.springframework.org/schema/beans">
                    <bean id="example" class="com.example.Example">
                        <property name="topic" value="orders"/>
                    </bean>
                </beans>
                """
            )
        );
    }

    @Test
    void headerRenameLeavesNonCamelXmlAlone() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new RenameHeaderInXmlDsl("kafka.TOPIC", "CamelKafkaTopic")),
            xml(
                """
                <configuration xmlns="http://example.com/schema/http">
                    <endpoint url="https://example.com/publish">
                        <header name="kafka.TOPIC" value="orders"/>
                        <template>${header.kafka.TOPIC}</template>
                    </endpoint>
                </configuration>
                """
            )
        );
    }

    @Test
    void headerPrefixRenameLeavesNonCamelXmlAlone() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new RenameHeaderPrefixInXmlDsl("SolrField.", "CamelSolrField.")),
            xml(
                """
                <configuration xmlns="http://example.com/schema/http">
                    <endpoint url="https://example.com/search">
                        <header name="SolrField.id" value="doc123"/>
                    </endpoint>
                </configuration>
                """
            )
        );
    }

    @Test
    void sagaLeavesNonCamelXmlAlone() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new XmlDsl418_1SagaRecipe()),
            xml(
                """
                <workflow xmlns="http://example.com/schema/workflow">
                    <saga>
                        <compensation uri="http://example.com/undo"/>
                        <completion uri="http://example.com/done"/>
                    </saga>
                </workflow>
                """
            )
        );
    }

    @Test
    void circuitBreakerLeavesNonCamelXmlAlone() {
        // driven through the whole 4.0 migration, the way CameXmlDslRecipeTest drives it
        //language=xml
        rewriteRun(
            spec -> CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_0)
                    .typeValidationOptions(TypeValidation.none()),
            xml(
                """
                <application>
                    <circuitBreaker>
                        <resilience4jConfiguration>
                            <timeoutEnabled>true</timeoutEnabled>
                            <timeoutDuration>1</timeoutDuration>
                        </resilience4jConfiguration>
                    </circuitBreaker>
                </application>
                """
            )
        );
    }

    @Test
    void interceptLeavesNonCamelXmlAlone() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new XmlDsl410Recipe()),
            xml(
                """
                <pipeline xmlns="http://example.com/schema/pipeline">
                    <intercept>
                        <when>
                            <simple>true</simple>
                        </when>
                    </intercept>
                </pipeline>
                """
            )
        );
    }

    @Test
    void loadBalanceLeavesNonCamelXmlAlone() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new XmlDsl47Recipe()),
            xml(
                """
                <cluster xmlns="http://example.com/schema/cluster">
                    <loadBalance>
                        <roundRobin/>
                    </loadBalance>
                </cluster>
                """
            )
        );
    }

    @Test
    void componentPropertyLeavesNonCamelXmlAlone() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new ReplacePropertyInComponentXml("netty-http", "keyStoreFile", "keyStoreResource", "file:")),
            xml(
                """
                <deployment xmlns="http://example.com/schema/deployment">
                    <route>
                        <from uri="netty-http:https://0.0.0.0:8443/service?keyStoreFile=/opt/keystore.jks"/>
                    </route>
                </deployment>
                """
            )
        );
    }

    @Test
    void dataFormatPropertyLeavesNonCamelXmlAlone() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new ReplacePropertyInDataFormatXml("csv", "formatRef", "format")),
            xml(
                """
                <deployment xmlns="http://example.com/schema/deployment">
                    <marshal>
                        <csv formatRef="myCsvFormat"/>
                    </marshal>
                </deployment>
                """
            )
        );
    }

    @Test
    void componentUriLeavesNonCamelXmlAlone() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new ChangeXmlComponentUriRecipe("^langchain4j-tools:(.*)$", "ai-tool:${1}", true)),
            xml(
                """
                <deployment xmlns="http://example.com/schema/deployment">
                    <route>
                        <from uri="langchain4j-tools:weather?tags=weather"/>
                    </route>
                </deployment>
                """
            )
        );
    }

    @Test
    void resequenceLeavesNonCamelXmlAlone() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new CamelResequenceEIPXmlRecipe()),
            xml(
                """
                <pipeline xmlns="http://example.com/schema/pipeline">
                    <resequence>
                        <stream-config timeout="1000"/>
                    </resequence>
                </pipeline>
                """
            )
        );
    }

    // ---- documents that must still be recognised as Camel ----

    @Test
    void springXmlLayoutIsMigrated() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new RenameHeaderInXmlDsl("kafka.TOPIC", "CamelKafkaTopic")),
            xml(
                """
                <beans xmlns="http://www.springframework.org/schema/beans">
                    <camelContext xmlns="http://camel.apache.org/schema/spring">
                        <route>
                            <from uri="direct:start"/>
                            <setHeader name="kafka.TOPIC">
                                <constant>orders</constant>
                            </setHeader>
                        </route>
                    </camelContext>
                </beans>
                """,
                """
                <beans xmlns="http://www.springframework.org/schema/beans">
                    <camelContext xmlns="http://camel.apache.org/schema/spring">
                        <route>
                            <from uri="direct:start"/>
                            <setHeader name="CamelKafkaTopic">
                                <constant>orders</constant>
                            </setHeader>
                        </route>
                    </camelContext>
                </beans>
                """
            )
        );
    }

    @Test
    void camelRootIsMigrated() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new RenameHeaderInXmlDsl("kafka.TOPIC", "CamelKafkaTopic")),
            xml(
                """
                <camel>
                    <route>
                        <from uri="direct:start"/>
                        <setHeader name="kafka.TOPIC">
                            <constant>orders</constant>
                        </setHeader>
                    </route>
                </camel>
                """,
                """
                <camel>
                    <route>
                        <from uri="direct:start"/>
                        <setHeader name="CamelKafkaTopic">
                            <constant>orders</constant>
                        </setHeader>
                    </route>
                </camel>
                """
            )
        );
    }

    @Test
    void pluralWrapperRootIsMigrated() {
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new RenameHeaderInXmlDsl("kafka.TOPIC", "CamelKafkaTopic")),
            xml(
                """
                <routeConfigurations>
                    <routeConfiguration>
                        <onException>
                            <setHeader name="kafka.TOPIC">
                                <constant>dlq</constant>
                            </setHeader>
                        </onException>
                    </routeConfiguration>
                </routeConfigurations>
                """,
                """
                <routeConfigurations>
                    <routeConfiguration>
                        <onException>
                            <setHeader name="CamelKafkaTopic">
                                <constant>dlq</constant>
                            </setHeader>
                        </onException>
                    </routeConfiguration>
                </routeConfigurations>
                """
            )
        );
    }

    @Test
    void springStyleBeansInACamelDocumentAreLeftAlone() {
        // a Camel XML file legitimately carries Spring bean definitions next to its routes, and a Spring
        // bean declares its class with class= where a Camel bean uses type=
        //language=xml
        rewriteRun(
            spec -> spec.recipe(new XmlDsl46Recipe()),
            xml(
                """
                <camel>
                    <bean id="bean1" class="org.apache.camel.main.app.Bean1">
                        <property name="bean" ref="bean2"/>
                    </bean>
                </camel>
                """
            )
        );
    }

}
