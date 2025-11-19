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
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate416From3xTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_16)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v3_18,
                        "camel-milo", "stack-server-0.6.8", "stack-client-0.6.8", "stack-core-0.6.8", "sdk-client-0.6.8"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_16.html#_certificate_validation_api_changes">camel-milo API changes</a>
     */
    @DocumentExample
    @Test
    void miloApiChange() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.component.milo.server.MiloServerComponent;
                  import org.eclipse.milo.opcua.stack.server.security.ServerCertificateValidator;
                  import org.eclipse.milo.opcua.stack.server.security.DefaultServerCertificateValidator;
                  
                  public class MiloTest {
                  
                      public void test()  {
                          MiloServerComponent server = null;
                          ServerCertificateValidator scv = new DefaultServerCertificateValidator(null);
                          server.setCertificateValidator(scv);
                      }
                  }
                  """,
                """
                  import org.apache.camel.component.milo.server.MiloServerComponent;
                  import org.eclipse.milo.opcua.stack.core.security.CertificateValidator;
                  import org.eclipse.milo.opcua.stack.server.security.DefaultServerCertificateValidator;
                  
                  public class MiloTest {
                  
                      public void test()  {
                          MiloServerComponent server = null;
                          CertificateValidator scv = new DefaultServerCertificateValidator(null);
                          server.setCertificateValidator(scv);
                      }
                  }
                  """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_16.html#_subscription_monitoring_api_changes">camel-milo subscription monitoring API changes</a>
     */
    @Test
    void miloSubscriptionMonitoringApiChanges() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.component.milo.server.MiloServerComponent;
                  import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaMonitoredItem;
                  
                  public class MiloTest {
                  
                      public void test()  {
                          OpcUaMonitoredItem item = null;
                          item.setValueConsumer(dataValue -> {int i = 0;});
                      }
                  }
                  """,
                """
                  import org.apache.camel.component.milo.server.MiloServerComponent;
                  import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaMonitoredItem;
                  
                  public class MiloTest {
                  
                      public void test()  {
                          OpcUaMonitoredItem item = null;
                          item.setDataValueListener((item,dataValue) -> {int i = 0;});
                      }
                  }
                  """));
    }


}
