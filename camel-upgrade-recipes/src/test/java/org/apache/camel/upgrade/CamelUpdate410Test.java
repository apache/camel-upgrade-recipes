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

public class CamelUpdate410Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_10)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_9, "camel-smb", "camel-azure-files"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://github.com/apache/camel/blob/main/docs/user-manual/modules/ROOT/pages/camel-4x-upgrade-guide-4_10.adoc#xml-dsl-changes">xml DSL changes</a>
     */
    @Test
    public void testIntercept() {
        //language=xml
        rewriteRun(xml("""
            <camelContext>
            
              <intercept>
                  <when>
                    <simple>${body} contains 'Hello'</simple>
                    <to uri="log:test"/>
                    <stop/> <!-- stop continue routing -->
                  </when>
              </intercept>
            
              <route>
                <from uri="jms:queue:order"/>
                <to uri="bean:validateOrder"/>
                <to uri="bean:processOrder"/>
              </route>
            
            </camelContext>
                """, """
            <camelContext>
            
              <intercept>
                  <onWhen>
                    <simple>${body} contains 'Hello'</simple>
                    <to uri="log:test"/>
                    <stop/> <!-- stop continue routing -->
                  </onWhen>
              </intercept>
            
              <route>
                <from uri="jms:queue:order"/>
                <to uri="bean:validateOrder"/>
                <to uri="bean:processOrder"/>
              </route>
            
            </camelContext>
                """));
    }
    /**
     * <a href="https://github.com/apache/camel/blob/main/docs/user-manual/modules/ROOT/pages/camel-4x-upgrade-guide-4_10.adoc#xml-dsl-changes">xml DSL changes</a>
     */
    @Test
    public void testInterceptSendToEndpoint() {
        //language=xml
        rewriteRun(xml("""
            <camelContext>
            
              <interceptSendToEndpoint uri="kafka*" skipSendToOriginalEndpoint="true">
                <when><simple>${header.biztype} == 'TEST'</simple></when>
                <log message="TEST message detected - is NOT send to kafka"/>
              </interceptSendToEndpoint>
            
              <route>
                <from uri="jms:queue:order"/>
                <to uri="bean:validateOrder"/>
                <to uri="bean:processOrder"/>
                <to uri="kafka:order"/>
              </route>
            
            </camelContext>
                """, """
            <camelContext>
            
              <interceptSendToEndpoint uri="kafka*" skipSendToOriginalEndpoint="true">
                <onWhen><simple>${header.biztype} == 'TEST'</simple></onWhen>
                <log message="TEST message detected - is NOT send to kafka"/>
              </interceptSendToEndpoint>
            
              <route>
                <from uri="jms:queue:order"/>
                <to uri="bean:validateOrder"/>
                <to uri="bean:processOrder"/>
                <to uri="kafka:order"/>
              </route>
            
            </camelContext>
                """));
    }

    /**
     * <a href="https://github.com/apache/camel/blob/main/docs/user-manual/modules/ROOT/pages/camel-4x-upgrade-guide-4_10.adoc#camel-smb">camel-smb</a>
     */
    @Test
    public void testSmbChange() {
        //language=java
        rewriteRun(java(
                """
                             import org.apache.camel.component.smb.SmbConstants;
                             
                             public class SmbTest {
                                 public void test() {
                                        String s = SmbConstants.SMB_FILE_PATH;
                                 }
                             }
                        """,
                """
                             import org.apache.camel.component.smb.SmbConstants;
                             
                             public class SmbTest {
                                 public void test() {
                                        String s = SmbConstants.FILE_PATH;
                                 }
                             }
                        """));
    }
    /**
     * <a href="https://github.com/apache/camel/blob/main/docs/user-manual/modules/ROOT/pages/camel-4x-upgrade-guide-4_10.adoc#camel-azure-files">camel-azure-files</a>
     */
    @Test
    public void testAzureFiles() {
        //language=java
        rewriteRun(java(
                """
                             import org.apache.camel.component.file.azure.FilesHeaders;
                             
                             public class SmbTest {
                                 public void test() {
                                        String s = FilesHeaders.FILE_PATH;
                                 }
                             }
                        """,
                """
                             import org.apache.camel.component.file.azure.FilesConstants;
                             
                             public class SmbTest {
                                 public void test() {
                                        String s = FilesConstants.FILE_PATH;
                                 }
                             }
                        """));
    }

}
