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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.*;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

class CamelUpdate412Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_12)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_11, "camel-api",
                        "camel-core-model", "camel-support", "camel-base-engine", "camel-base"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_12.html#_java_dsl">Java DSL</a>
     */
    @DocumentExample
    @Test
    void javaDslChoice() {
        //language=java
        rewriteRun(java(
                """
                import org.apache.camel.ExchangePattern;
                import org.apache.camel.builder.RouteBuilder;
            
                public class MySimpleToDRoute extends RouteBuilder {
            
                    @Override
                    public void configure() {
            
                        from("direct:start")
                            .choice()
                                .when(header("foo").isGreaterThan(1))
                                    .choice()
                                        .when(header("foo").isGreaterThan(5))
                                            .to("mock:big")
                                        .otherwise()
                                            .to("mock:med")
                                    .endChoice()
                                .otherwise()
                                    .to("mock:low")
                                .end();
                    }
                }
                """,
                """
                import org.apache.camel.ExchangePattern;
                import org.apache.camel.builder.RouteBuilder;
                
                public class MySimpleToDRoute extends RouteBuilder {
                
                    @Override
                    public void configure() {
                        
                        from("direct:start")
                            .choice()
                                .when(header("foo").isGreaterThan(1))
                                    .choice()
                                        .when(header("foo").isGreaterThan(5))
                                            .to("mock:big")
                                        .otherwise()
                                            .to("mock:med")
                                    .end().endChoice()
                                .otherwise()
                                    .to("mock:low")
                                .end();
                    }
                }
                """));
    }


    /**
     * The package scan classes has moved from camel-base-engine to camel-support
     *
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_12.html#_camel_core">Moved scan classes</a>
     */
    @CsvSource({"DefaultPackageScanClassResolver,org.apache.camel.impl.engine,org.apache.camel.support.scan",
            "DefaultPackageScanResourceResolver,org.apache.camel.impl.engine,org.apache.camel.support.scan",
            "WebSpherePackageScanClassResolver,org.apache.camel.impl.engine, org.apache.camel.support.scan",
            "AnnotatedWithAnyPackageScanFilter,org.apache.camel.impl.scan, org.apache.camel.support.scan",
            "AnnotatedWithPackageScanFilter,org.apache.camel.impl.scan, org.apache.camel.support.scan",
            "AssignableToPackageScanFilter,org.apache.camel.impl.scan, org.apache.camel.support.scan",
            "CompositePackageScanFilter,org.apache.camel.impl.scan, org.apache.camel.support.scan",
            "InvertingPackageScanFilter,org.apache.camel.impl.scan, org.apache.camel.support.scan"})
    @ParameterizedTest
    void movedScanClasses(String className, String originalImport, String expectedImport) {
        rewriteRun(
                mavenProject("testMovedScanClasses",
                    sourceSet(java(
                        String.format("""
                                     import %s.%s;
                                     
                                     public class Test {
                                         public void test() {
                                             %s b = null;
                                         }
                                     }
                                """, originalImport, className, className),
                        String.format("""
                                     import %s.%s;
                                     
                                     public class Test {
                                         public void test() {
                                             %s b = null;
                                         }
                                     }
                                """, expectedImport, className, className)).iterator().next(), "src/man/java"), pomXml(
                                """
                                        <project>
                                           <modelVersion>4.0.0</modelVersion>
                
                                           <artifactId>test</artifactId>
                                           <groupId>org.apache.camel.test</groupId>
                                           <version>1.0.0</version>
                
                                           <properties>
                                               <camel.version>4.11.0</camel.version>
                                           </properties>
                                           
                                           	<dependencyManagement>
                                           		<dependencies>>
                                           			<dependency>
                                           				<groupId>io.quarkus.platform</groupId>
                                           				<artifactId>quarkus-camel-bom</artifactId>
                                           				<version>3.2.11.Final</version>
                                           				<type>pom</type>
                                           				<scope>import</scope>
                                           			</dependency>
                                           		</dependencies>
                                           	</dependencyManagement>
                
                                           <dependencies>
                                               <dependency>
                                                   <groupId>org.apache.camel</groupId>
                                                   <artifactId>camel-base</artifactId>
                                               </dependency>
                                            </dependencies>
                
                                        </project>
                                        """,
                                """
                                        <project>
                                           <modelVersion>4.0.0</modelVersion>
                
                                           <artifactId>test</artifactId>
                                           <groupId>org.apache.camel.test</groupId>
                                           <version>1.0.0</version>
                
                                           <properties>
                                               <camel.version>4.11.0</camel.version>
                                           </properties>
                                           
                                           	<dependencyManagement>
                                           		<dependencies>>
                                           			<dependency>
                                           				<groupId>io.quarkus.platform</groupId>
                                           				<artifactId>quarkus-camel-bom</artifactId>
                                           				<version>3.2.11.Final</version>
                                           				<type>pom</type>
                                           				<scope>import</scope>
                                           			</dependency>
                                           		</dependencies>
                                           	</dependencyManagement>
                
                                           <dependencies>
                                               <dependency>
                                                   <groupId>org.apache.camel</groupId>
                                                   <artifactId>camel-base</artifactId>
                                               </dependency>
                                              <dependency>
                                                 <groupId>org.apache.camel</groupId>
                                                 <artifactId>camel-support</artifactId>
                                              </dependency>
                                            </dependencies>
                
                                        </project>
                                        """)
                ));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_12.html#_rest_dsl">Rest DSL</a>
     */
    @Test
    void xmlDslBearer() {
        //language=xml
        rewriteRun(xml(
                """
                <camelContext xmlns="http://camel.apache.org/schema/spring">
                    <rest path="/user" description="User rest service" consumes="application/json" produces="application/json">
                        <securityDefinitions>
                            <bearer key="myBearerKey" description="Bearer token for security"/>
                        </securityDefinitions>
                        <get path="/hello">
                            <to uri="direct:hello"/>
                        </get>
                        <get path="/bye" consumes="application/json">
                            <to uri="direct:bye"/>
                        </get>
                        <post path="/bye">
                            <to uri="mock:update"/>
                        </post>
                    </rest>
                </camelContext>
                """,
                """
                <camelContext xmlns="http://camel.apache.org/schema/spring">
                    <rest path="/user" description="User rest service" consumes="application/json" produces="application/json">
                        <securityDefinitions>
                            <bearerToken key="myBearerKey" description="Bearer token for security"/>
                        </securityDefinitions>
                        <get path="/hello">
                            <to uri="direct:hello"/>
                        </get>
                        <get path="/bye" consumes="application/json">
                            <to uri="direct:bye"/>
                        </get>
                        <post path="/bye">
                            <to uri="mock:update"/>
                        </post>
                    </rest>
                </camelContext>
                """));    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_12.html#_rest_dsl">Rest DSL</a>
     */
    @Test
    void yamlDslBearer() {
        //language=yaml
        rewriteRun(yaml(
                """
                    - rest:
                        path: "/user"
                        description: "User rest service"
                        consumes: "application/json"
                        produces: "application/json"
                        securityDefinitions:
                            bearer:
                              key: "test"
                        get:
                          - path: "/hello"
                            to: "direct:hello"
                          - path: "/bye"
                            consumes: "application/json"
                            to: "direct:bye"
                        post:
                          - path: "/bye"
                            to: "mock:update"
                    - route:
                        id: loadbalance-failover-route
                        from:
                          uri: "direct://loadbalance-failover"
                          steps:
                            - log:
                                message: "Processing message start: ${body}"                    
                """,
                """
                    - rest:
                        path: "/user"
                        description: "User rest service"
                        consumes: "application/json"
                        produces: "application/json"
                        securityDefinitions:
                            bearerToken:
                              key: "test"
                        get:
                          - path: "/hello"
                            to: "direct:hello"
                          - path: "/bye"
                            consumes: "application/json"
                            to: "direct:bye"
                        post:
                          - path: "/bye"
                            to: "mock:update"
                    - route:
                        id: loadbalance-failover-route
                        from:
                          uri: "direct://loadbalance-failover"
                          steps:
                            - log:
                                message: "Processing message start: ${body}"  
                """));
    }

}
