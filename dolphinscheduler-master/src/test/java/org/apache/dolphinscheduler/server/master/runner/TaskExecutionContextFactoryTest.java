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

package org.apache.dolphinscheduler.server.master.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.AbstractResourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskExecutionContextFactoryTest {

    private static final String DS_NAME = "mysql_local";
    private static final int DS_ID = 42;
    private static final String CONNECTION_PARAMS = "{\"user\":\"root\"}";

    @InjectMocks
    private TaskExecutionContextFactory taskExecutionContextFactory;

    @Mock
    private ProcessService processService;

    private Map<Integer, AbstractResourceParameters> datasourceMap;

    @BeforeEach
    void setUp() {
        datasourceMap = new HashMap<>();
        DataSourceParameters ref = new DataSourceParameters();
        ref.setName(DS_NAME);
        datasourceMap.put(DS_NAME.hashCode(), ref);
    }

    /**
     * DATAXML 以 name.hashCode() 为 key 注册数据源，assemble 时需将 key 替换为真实 id，且不得触发 ConcurrentModificationException。
     */
    @Test
    void assembleDataSourceParameters_replacesHashCodeKeyWithDatasourceId() {
        DataSource dataSource = new DataSource();
        dataSource.setId(DS_ID);
        dataSource.setName(DS_NAME);
        dataSource.setType(DbType.MYSQL);
        dataSource.setConnectionParams(CONNECTION_PARAMS);
        when(processService.findDataSourceByName(eq(DS_NAME))).thenReturn(dataSource);

        ReflectionTestUtils.invokeMethod(taskExecutionContextFactory, "assembleDataSourceParameters", datasourceMap);

        assertEquals(1, datasourceMap.size());
        assertFalse(datasourceMap.containsKey(DS_NAME.hashCode()));
        DataSourceParameters assembled = (DataSourceParameters) datasourceMap.get(DS_ID);
        assertNotNull(assembled);
        assertEquals(DS_NAME, assembled.getName());
        assertEquals(DbType.MYSQL, assembled.getType());
        assertEquals(CONNECTION_PARAMS, assembled.getConnectionParams());
    }

    @Test
    void assembleDataSourceParameters_multipleEntries_noConcurrentModification() {
        String otherName = "mysql_other";
        DataSourceParameters otherRef = new DataSourceParameters();
        otherRef.setName(otherName);
        datasourceMap.put(otherName.hashCode(), otherRef);

        DataSource dataSource1 = new DataSource();
        dataSource1.setId(DS_ID);
        dataSource1.setName(DS_NAME);
        dataSource1.setType(DbType.MYSQL);
        dataSource1.setConnectionParams(CONNECTION_PARAMS);

        DataSource dataSource2 = new DataSource();
        dataSource2.setId(99);
        dataSource2.setName(otherName);
        dataSource2.setType(DbType.MYSQL);
        dataSource2.setConnectionParams(CONNECTION_PARAMS);

        when(processService.findDataSourceByName(eq(DS_NAME))).thenReturn(dataSource1);
        when(processService.findDataSourceByName(eq(otherName))).thenReturn(dataSource2);

        ReflectionTestUtils.invokeMethod(taskExecutionContextFactory, "assembleDataSourceParameters", datasourceMap);

        assertEquals(2, datasourceMap.size());
        assertTrue(datasourceMap.containsKey(DS_ID));
        assertTrue(datasourceMap.containsKey(99));
    }
}
