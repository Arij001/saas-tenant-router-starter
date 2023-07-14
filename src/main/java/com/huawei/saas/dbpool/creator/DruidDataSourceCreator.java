/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.saas.dbpool.creator;

import com.alibaba.druid.pool.DruidDataSource;
import com.huawei.saas.constants.DbPoolEnum;
import com.huawei.saas.dbpool.druid.DruidPool;
import com.huawei.saas.properties.DataSourceProperty;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;

/**
 * druid数据源创建器
 *
 * @since 2022-4-22
 */
@Slf4j
public class DruidDataSourceCreator implements DataSourceCreator {

    /**
     * 获取池名称
     *
     * @return 池名称
     */
    @Override
    public String getPoolName() {
        return DbPoolEnum.POOL_DRUID.getName();
    }

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        DruidPool poolTemplate = (DruidPool) dataSourceProperty.getPool(getPoolName());
        DruidDataSource dataSource = poolTemplate != null ? poolTemplate : new DruidPool();
        dataSource.setUsername(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        dataSource.setUrl(dataSourceProperty.getUrl());
        Optional.ofNullable(dataSourceProperty.getDriverClassName()).ifPresent(dataSource::setDriverClassName);
        try {
            dataSource.init();
        } catch (SQLException e) {
            log.error("druid pool init failed {}", dataSource.getName());
        }
        return dataSource;
    }

    /**
     * 关闭连接池
     *
     * @param dataSource 数据源
     * @return 关闭结果
     */
    @Override
    public boolean close(DataSource dataSource) {
        if (dataSource == null) {
            return true;
        }
        if (dataSource instanceof DruidDataSource) {
            ((DruidDataSource) dataSource).close();
            return true;
        }
        return false;
    }
}
