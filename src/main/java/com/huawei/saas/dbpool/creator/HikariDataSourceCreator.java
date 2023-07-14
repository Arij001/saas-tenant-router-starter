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

import com.huawei.saas.constants.DbPoolEnum;
import com.huawei.saas.dbpool.hikari.HikariCpPool;
import com.huawei.saas.properties.DataSourceProperty;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.log4j.Log4j2;

import java.util.Optional;

import javax.sql.DataSource;

/**
 * Hikari数据源创建器
 *
 * @since 2022-07-26
 */
@Log4j2
public class HikariDataSourceCreator implements DataSourceCreator {

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        HikariCpPool poolTemplate = (HikariCpPool) dataSourceProperty.getPool(getPoolName());
        HikariConfig config = poolTemplate != null ? poolTemplate : new HikariConfig();
        config.setUsername(dataSourceProperty.getUsername());
        config.setPassword(dataSourceProperty.getPassword());
        config.setJdbcUrl(dataSourceProperty.getUrl());
        Optional.ofNullable(dataSourceProperty.getDriverClassName()).ifPresent(config::setDriverClassName);
        log.warn("Hikari {} create success !", config.getPoolName());
        return new HikariDataSource(config);
    }

    @Override
    public boolean close(DataSource dataSource) {
        if (dataSource == null) {
            return true;
        }
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            return true;

        }
        return false;
    }

    @Override
    public String getPoolName() {
        return DbPoolEnum.POOL_HIKARI.getName();
    }
}
