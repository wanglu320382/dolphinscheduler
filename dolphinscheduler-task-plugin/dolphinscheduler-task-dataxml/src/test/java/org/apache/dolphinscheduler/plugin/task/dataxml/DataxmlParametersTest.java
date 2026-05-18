/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.task.dataxml;

import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DataxmlParametersTest {

  @Test
  void checkParametersWithValidXml() {
    DataxmlParameters params = new DataxmlParameters();
    params.setXmlContent("""
        <?xml version="1.0" encoding="UTF-8"?>
        <xdataTemplates xmlns:ns2="http://www.gzsw.com/xdata-templates/">
          <ns2:xdata-template busType="DB2PARA" fromDs="mysql_local" taskType="PARA">
            <ns2:column fromName="id" toName="id"/>
          </ns2:xdata-template>
        </xdataTemplates>
        """);
    Assertions.assertTrue(params.checkParameters());
    ResourceParametersHelper resources = params.getResources();
    Assertions.assertTrue(
        resources.getResourceMap(ResourceType.DATASOURCE).containsKey("mysql_local".hashCode()));
  }

  @Test
  void getResourcesCollectsDatasourceByName() {
    DataxmlParameters params = new DataxmlParameters();
    params.setXmlContent("""
        <?xml version="1.0" encoding="UTF-8"?>
        <xdataTemplates xmlns:ns2="http://www.gzsw.com/xdata-templates/">
          <ns2:xdata-template busType="DB2DB" fromDs="mysql_local" toDs="mysql_local"
                              fromObjectName="a" toObjectName="b"/>
        </xdataTemplates>
        """);
    ResourceParametersHelper resources = params.getResources();
    DataSourceParameters ref = (DataSourceParameters) resources
        .getResourceParameters(ResourceType.DATASOURCE, "mysql_local".hashCode());
    Assertions.assertNotNull(ref);
    Assertions.assertEquals("mysql_local", ref.getName());
  }

  @Test
  void checkParametersRejectsEmptyXml() {
    DataxmlParameters params = new DataxmlParameters();
    Assertions.assertFalse(params.checkParameters());
  }
}
