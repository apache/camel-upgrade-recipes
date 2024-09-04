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
package org.apache.camel.updates;

import org.junit.jupiter.api.Test;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelUpdate45Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_5)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_4,
                        "camel-api", "camel-base-engine", "camel-spring-redis", "camel-opensearch", "camel-elasticsearch"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_5.html#_camel_core">CAMEL_CORE</a>
     */
    @Test
    public void testCamelCore() {
        //language=java
        rewriteRun(java(
                """
                        import org.apache.camel.impl.engine.AbstractCamelContext;
                        
                        public class Test {
                        
                            AbstractCamelContext context = null;
                        
                            public void test() {
                                context.setTypeConverter(null);
                                context.getOrCreateTypeConverter();
                                context.setManagementMBeanAssembler(null);
                                context.getRestRegistryFactory();
                                context.setRestRegistryFactory(null);
                                context.setTransformerRegistry(null);
                                context.setValidatorRegistry(null);
                                context.setName("name");
                                context.setDescription("description");
                                context.getBootstrapFactoryFinder();
                                context.getFactoryFinder("something");
                                context.addInterceptStrategy(null);
                                context.getStartupStepRecorder();
                                context.setStartupStepRecorder(null);
                                context.resolvePropertyPlaceholders("", false);
                                context.getBasePackageScan();
                                context.setBasePackageScan("something");
                            }
                        }
                        """,
                """
                            import org.apache.camel.impl.engine.AbstractCamelContext;
                            
                            public class Test {
                            
                                AbstractCamelContext context = null;
                            
                                public void test() {
                                    context.getCamelContextExtension().setTypeConverter(null);
                                    context.getCamelContextExtension().getOrCreateTypeConverter();
                                    context.getCamelContextExtension().setManagementMBeanAssembler(null);
                                    context.getCamelContextExtension().getRestRegistryFactory();
                                    context.getCamelContextExtension().setRestRegistryFactory(null);
                                    context.getCamelContextExtension().setTransformerRegistry(null);
                                    context.getCamelContextExtension().setValidatorRegistry(null);
                                    context.getCamelContextExtension().setName("name");
                                    context.getCamelContextExtension().setDescription("description");
                                    context.getCamelContextExtension().getBootstrapFactoryFinder();
                                    context.getCamelContextExtension().getFactoryFinder("something");
                                    context.getCamelContextExtension().addInterceptStrategy(null);
                                    context.getCamelContextExtension().getStartupStepRecorder();
                                    context.getCamelContextExtension().setStartupStepRecorder(null);
                                    context.getCamelContextExtension().resolvePropertyPlaceholders("", false);
                                    context.getCamelContextExtension().getBasePackageScan();
                                    context.getCamelContextExtension().setBasePackageScan("something");
                                }
                            }
                        """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_5.html#_camel_main">CAMEL-MAIN</a>
     */
    @Test
    public void testCamelMain() {
        rewriteRun(Assertions.properties("""
                   #test
                   camel.main.backlogTracing=true
                """,
                """
                            #test
                            camel.trace.enabled=true
                        """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_5.html#_camel_spring_redis">CAMEL-SPRING-REDIS</a>
     */
    @Test
    public void testSpringRedis() {
        //language=java
        rewriteRun(java(
                """
                            import org.apache.camel.component.redis.processor.idempotent.RedisIdempotentRepository;
                            import org.apache.camel.component.redis.processor.idempotent.RedisStringIdempotentRepository;
                            
                            public class RedisTest {
                                public void test() {
                            
                                    RedisIdempotentRepository redisIdempotentRepository = null;
                                    RedisStringIdempotentRepository redisStringIdempotentRepository = null;
                                }
                            }
                        """,
                """
                            import org.apache.camel.component.redis.processor.idempotent.SpringRedisIdempotentRepository;
                            import org.apache.camel.component.redis.processor.idempotent.SpringRedisStringIdempotentRepository;
                            
                            public class RedisTest {
                                public void test() {
                            
                                    SpringRedisIdempotentRepository redisIdempotentRepository = null;
                                    SpringRedisStringIdempotentRepository redisStringIdempotentRepository = null;
                                }
                            }
                        """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_5.html#_camel_elasticsearch_camel_opensearch">CAMEL-ELASTICSEARCH/CAMEL-OPENSEARCH</a>
     */
    @Test
    public void testSearch() {
        //language=java
        rewriteRun(java(
                """
                            public class SearchTest {
                                 public void test() {
                             
                                     org.apache.camel.component.es.aggregation.BulkRequestAggregationStrategy elasticAggregationStrategy = null;
                                     org.apache.camel.component.opensearch.aggregation.BulkRequestAggregationStrategy openAggregationStrategy = null;
                                 }
                            }
                        """,
                """
                            public class SearchTest {
                                 public void test() {
                            
                                     org.apache.camel.component.es.aggregation.ElastichsearchBulkRequestAggregationStrategy elasticAggregationStrategy = null;
                                     org.apache.camel.component.opensearch.aggregation.OpensearchBulkRequestAggregationStrategy openAggregationStrategy = null;
                                 }
                            }
                        """));
    }

}
