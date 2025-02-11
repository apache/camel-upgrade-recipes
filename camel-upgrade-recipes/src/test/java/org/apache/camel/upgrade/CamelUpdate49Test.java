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
import org.openrewrite.yaml.Assertions;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.xml.Assertions.xml;

public class CamelUpdate49Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_9)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_8,
                        "camel-api", "camel-core-model", "camel-debezium-db2", "camel-debezium-mysql", "camel-debezium-oracle", "camel-debezium-mongodb", "camel-debezium-postgres", "camel-debezium-sqlserver"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_9.html#_camel_api">camel-api</a>
     */
    @Test
    public void testApiChanges() {
        //language=java
        rewriteRun(java(
                """
                             import org.apache.camel.Exchange;
                             import org.apache.camel.ExchangePropertyKey;
                             
                             public class ApisTest {
                                 public void test() {
                                     var s1 = Exchange.ACTIVE_SPAN;
                                     var s2 = ExchangePropertyKey.ACTIVE_SPAN.name();
                                     System.out.println(s1 + ":" + s2);
                                 }
                             }
                        """,
                """
                             import org.apache.camel.Exchange;
                             import org.apache.camel.ExchangePropertyKey;
                            
                             public class ApisTest {
                                 public void test() {
                                     var s1 = Exchange.OTEL_ACTIVE_SPAN;
                                     var s2 = ExchangePropertyKey.OTEL_ACTIVE_SPAN.name();
                                     System.out.println(s1 + ":" + s2);
                                 }
                             }
                        """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_9.html#_camel_hashicorp_vault_properties_function">camel-hashicorp-vault properties function</a>
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_9.html#_camel_aws_secrets_manager_properties_function">camel-aws-secrets-manager properties function</a>
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_9.html#_camel_google_secret_manager_properties_function">camel-google-secret-manager properties function</a>
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_9.html#_camel_azure_key_vault_properties_function">camel-azure-key-vault properties function</a>
     */
    @Test
    public void testSecretFieldSyntax() {
        //language=java
        rewriteRun(java(
                """
                        import org.apache.camel.builder.RouteBuilder;
    
                        public class SecretFieldRoute extends RouteBuilder {
    
                            @Override
                            public void configure() {
                                from("direct:a")
                                    .inOut("{{hashicorp:secret:db/user}}")
                                    .to("log:result_hashicorp:secret");
                                from("direct:a")
                                    .inOut("{{aws:436etrt/Sheldon}}")
                                    .to("log:result_aws");
                                from("direct:a")
                                    .inOut("{{gcp:3ee4ff/Leonard}}")
                                    .to("log:result_aws");
                                from("direct:a")
                                    .inOut("{{azure:de43e56e6d6/Vilma}}")
                                    .to("log:result_azure");
                                from("direct:a")
                                    .inOut("{{azure:something/wrong/JoeD}}")
                                    .to("log:result_azure");
                                from("direct:a")
                                    .inOut("{{wrong:de43e56e6d6/JoeD}}")
                                    .to("log:result_azure");
                            }
                        }
                        """,
                """
                           import org.apache.camel.builder.RouteBuilder;
       
                           public class SecretFieldRoute extends RouteBuilder {
       
                               @Override
                               public void configure() {
                                   from("direct:a")
                                       .inOut("{{hashicorp:secret:db#user}}")
                                       .to("log:result_hashicorp:secret");
                                   from("direct:a")
                                       .inOut("{{aws:436etrt#Sheldon}}")
                                       .to("log:result_aws");
                                   from("direct:a")
                                       .inOut("{{gcp:3ee4ff#Leonard}}")
                                       .to("log:result_aws");
                                   from("direct:a")
                                       .inOut("{{azure:de43e56e6d6#Vilma}}")
                                       .to("log:result_azure");
                                   from("direct:a")
                                       .inOut("{{azure:something/wrong/JoeD}}")
                                       .to("log:result_azure");
                                   from("direct:a")
                                       .inOut("{{wrong:de43e56e6d6/JoeD}}")
                                       .to("log:result_azure");
                               }
                           }
                       """));
    }
    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_9.html#_camel_debezium">camel-debezium</a>
     */
    @Test
    public void testDebezium() {
        //language=java
        rewriteRun(java(
                """
                        import org.apache.camel.component.debezium.configuration.Db2ConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.DebeziumDb2ComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumDb2EndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumDb2EndpointUriFactory;
                        import org.apache.camel.component.debezium.DebeziumDb2Component;
                        import org.apache.camel.component.debezium.DebeziumDb2ComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumDb2EndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumDb2Endpoint;
                        import org.apache.camel.component.debezium.configuration.MongodbConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.DebeziumMongodbComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumMongodbEndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumMongodbEndpointUriFactory;
                        import org.apache.camel.component.debezium.DebeziumMongodbComponent;
                        import org.apache.camel.component.debezium.DebeziumMongodbComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumMongodbEndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumMongodbEndpoint;
                        import org.apache.camel.component.debezium.configuration.MySqlConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.DebeziumMySqlComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumMySqlEndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumMySqlEndpointUriFactory;
                        import org.apache.camel.component.debezium.DebeziumMySqlComponent;
                        import org.apache.camel.component.debezium.DebeziumMySqlComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumMySqlEndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumMySqlEndpoint;
                        import org.apache.camel.component.debezium.configuration.OracleConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.DebeziumOracleComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumOracleEndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumOracleEndpointUriFactory;
                        import org.apache.camel.component.debezium.DebeziumOracleComponent;
                        import org.apache.camel.component.debezium.DebeziumOracleComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumOracleEndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumOracleEndpoint;
                        import org.apache.camel.component.debezium.configuration.PostgresConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.DebeziumPostgresComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumPostgresEndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumPostgresEndpointUriFactory;
                        import org.apache.camel.component.debezium.DebeziumPostgresComponent;
                        import org.apache.camel.component.debezium.DebeziumPostgresComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumPostgresEndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumPostgresEndpoint;
                        import org.apache.camel.component.debezium.configuration.SqlserverConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.DebeziumSqlserverComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumSqlserverEndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumSqlserverEndpointUriFactory;
                        import org.apache.camel.component.debezium.DebeziumSqlserverComponent;
                        import org.apache.camel.component.debezium.DebeziumSqlserverComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumSqlserverEndpointConfigurer;
                        import org.apache.camel.component.debezium.DebeziumSqlserverEndpoint;
                        
                        public class DebeziumTest {
    
                            public void method() {
                                //db2
                                Db2ConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumDb2ComponentConfigurer configurer = null;
                                DebeziumDb2EndpointConfigurer endpointConfigurer = null;
                                DebeziumDb2EndpointUriFactory uriFactory = null;
                                DebeziumDb2Component component = null;
                                DebeziumDb2ComponentConfigurer componentConfigurer = null;
                                DebeziumDb2EndpointConfigurer endpointConfigurer = null;
                                DebeziumDb2Endpoint endpoint = null;
                                //mongodb
                                MongodbConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumMongodbComponentConfigurer configurer = null;
                                DebeziumMongodbEndpointConfigurer endpointConfigurer = null;
                                DebeziumMongodbEndpointUriFactory uriFactory = null;
                                DebeziumMongodbComponent component = null;
                                DebeziumMongodbComponentConfigurer componentConfigurer = null;
                                DebeziumMongodbEndpointConfigurer endpointConfigurer = null;
                                DebeziumMongodbEndpoint endpoint = null;
                                //mysql
                                MySqlConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumMySqlComponentConfigurer configurer = null;
                                DebeziumMySqlEndpointConfigurer endpointConfigurer = null;
                                DebeziumMySqlEndpointUriFactory uriFactory = null;
                                DebeziumMySqlComponent component = null;
                                DebeziumMySqlComponentConfigurer componentConfigurer = null;
                                DebeziumMySqlEndpointConfigurer endpointConfigurer = null;
                                DebeziumMySqlEndpoint endpoint = null;
                                //oracle
                                OracleConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumOracleComponentConfigurer configurer = null;
                                DebeziumOracleEndpointConfigurer endpointConfigurer = null;
                                DebeziumOracleEndpointUriFactory uriFactory = null;
                                DebeziumOracleComponent component = null;
                                DebeziumOracleComponentConfigurer componentConfigurer = null;
                                DebeziumOracleEndpointConfigurer endpointConfigurer = null;
                                DebeziumOracleEndpoint endpoint = null;
                                //postgres
                                PostgresConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumPostgresComponentConfigurer configurer = null;
                                DebeziumPostgresEndpointConfigurer endpointConfigurer = null;
                                DebeziumPostgresEndpointUriFactory uriFactory = null;
                                DebeziumPostgresComponent component = null;
                                DebeziumPostgresComponentConfigurer componentConfigurer = null;
                                DebeziumPostgresEndpointConfigurer endpointConfigurer = null;
                                DebeziumPostgresEndpoint endpoint = null;
                                //sqlserver
                                SqlserverConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumSqlserverComponentConfigurer configurer = null;
                                DebeziumSqlserverEndpointConfigurer endpointConfigurer = null;
                                DebeziumSqlserverEndpointUriFactory uriFactory = null;
                                DebeziumSqlserverComponent component = null;
                                DebeziumSqlserverComponentConfigurer componentConfigurer = null;
                                DebeziumSqlserverEndpointConfigurer endpointConfigurer = null;
                                DebeziumSqlserverEndpoint endpoint = null;
                            }
                        }
                        """,
                """
                        import org.apache.camel.component.debezium.configuration.MongodbConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.configuration.SqlserverConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.db2.*;
                        import org.apache.camel.component.debezium.db2.configuration.Db2ConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.mongodb.*;
                        import org.apache.camel.component.debezium.mysql.*;
                        import org.apache.camel.component.debezium.mysql.configuration.MySqlConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.oracle.*;
                        import org.apache.camel.component.debezium.oracle.configuration.OracleConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.postgres.*;
                        import org.apache.camel.component.debezium.postgres.configuration.PostgresConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.sqlserver.*;
                       \s
                        public class DebeziumTest {
                       \s
                            public void method() {
                                //db2
                                Db2ConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumDb2ComponentConfigurer configurer = null;
                                DebeziumDb2EndpointConfigurer endpointConfigurer = null;
                                DebeziumDb2EndpointUriFactory uriFactory = null;
                                DebeziumDb2Component component = null;
                                DebeziumDb2ComponentConfigurer componentConfigurer = null;
                                DebeziumDb2EndpointConfigurer endpointConfigurer = null;
                                DebeziumDb2Endpoint endpoint = null;
                                //mongodb
                                MongodbConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumMongodbComponentConfigurer configurer = null;
                                DebeziumMongodbEndpointConfigurer endpointConfigurer = null;
                                DebeziumMongodbEndpointUriFactory uriFactory = null;
                                DebeziumMongodbComponent component = null;
                                DebeziumMongodbComponentConfigurer componentConfigurer = null;
                                DebeziumMongodbEndpointConfigurer endpointConfigurer = null;
                                DebeziumMongodbEndpoint endpoint = null;
                                //mysql
                                MySqlConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumMySqlComponentConfigurer configurer = null;
                                DebeziumMySqlEndpointConfigurer endpointConfigurer = null;
                                DebeziumMySqlEndpointUriFactory uriFactory = null;
                                DebeziumMySqlComponent component = null;
                                DebeziumMySqlComponentConfigurer componentConfigurer = null;
                                DebeziumMySqlEndpointConfigurer endpointConfigurer = null;
                                DebeziumMySqlEndpoint endpoint = null;
                                //oracle
                                OracleConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumOracleComponentConfigurer configurer = null;
                                DebeziumOracleEndpointConfigurer endpointConfigurer = null;
                                DebeziumOracleEndpointUriFactory uriFactory = null;
                                DebeziumOracleComponent component = null;
                                DebeziumOracleComponentConfigurer componentConfigurer = null;
                                DebeziumOracleEndpointConfigurer endpointConfigurer = null;
                                DebeziumOracleEndpoint endpoint = null;
                                //postgres
                                PostgresConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumPostgresComponentConfigurer configurer = null;
                                DebeziumPostgresEndpointConfigurer endpointConfigurer = null;
                                DebeziumPostgresEndpointUriFactory uriFactory = null;
                                DebeziumPostgresComponent component = null;
                                DebeziumPostgresComponentConfigurer componentConfigurer = null;
                                DebeziumPostgresEndpointConfigurer endpointConfigurer = null;
                                DebeziumPostgresEndpoint endpoint = null;
                                //sqlserver
                                SqlserverConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumSqlserverComponentConfigurer configurer = null;
                                DebeziumSqlserverEndpointConfigurer endpointConfigurer = null;
                                DebeziumSqlserverEndpointUriFactory uriFactory = null;
                                DebeziumSqlserverComponent component = null;
                                DebeziumSqlserverComponentConfigurer componentConfigurer = null;
                                DebeziumSqlserverEndpointConfigurer endpointConfigurer = null;
                                DebeziumSqlserverEndpoint endpoint = null;
                            }
                        }
                      """));
    }

    /**
     * TODO update link
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_9.html#_removed_deprecated_components">Removed deprecated components</a>
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_9.html#_removed_api">Removed API</a>
     */
    @Test
    public void testRemovedDependencies() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                           <modelVersion>4.0.0</modelVersion>

                           <artifactId>test</artifactId>
                           <groupId>org.apache.camel.test</groupId>
                           <version>1.0.0</version>

                           <properties>
                               <camel.version>4.8.0</camel.version>
                           </properties>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-api</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-groovy-dsl</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-js-dsl</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-jsh-dsl</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-kotlin-api</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-kotlin-dsl</artifactId>
                                   <version>${camel.version}</version>
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
                               <camel.version>4.8.0</camel.version>
                           </properties>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel</groupId>
                                   <artifactId>camel-api</artifactId>
                                   <version>${camel.version}</version>
                               </dependency>
                            </dependencies>

                        </project>
                        """));
    }

}
