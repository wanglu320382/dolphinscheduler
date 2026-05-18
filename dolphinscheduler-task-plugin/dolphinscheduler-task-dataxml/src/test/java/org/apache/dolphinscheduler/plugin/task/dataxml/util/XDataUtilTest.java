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

package org.apache.dolphinscheduler.plugin.task.dataxml.util;

import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.BusType;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class XDataUtilTest {

  @Test
  void parseTemplates() throws Exception {
    String xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <xdataTemplates xmlns:ns2="http://www.gzsw.com/xdata-templates/">
          <ns2:xdata-template name="t1" busType="DB2DB" fromDs="1" toDs="2"
              fromObjectName="src_table" toObjectName="dst_table" taskType="INSERT"/>
        </xdataTemplates>
        """;
    List<XdataTemplate> templates = XDataUtil.parseXdataTemplates(xml);
    Assertions.assertEquals(1, templates.size());
    Assertions.assertEquals("t1", templates.get(0).getName());
    Assertions.assertEquals(BusType.DB2DB.name(), templates.get(0).getBusType());
  }
}
