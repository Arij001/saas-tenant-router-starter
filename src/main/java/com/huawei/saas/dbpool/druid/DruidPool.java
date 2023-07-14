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

package com.huawei.saas.dbpool.druid;

import com.huawei.saas.constants.DbPoolEnum;
import com.huawei.saas.customedprocessor.PoolRefreshProcessor;
import com.huawei.saas.dbpool.JdbcPool;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;

/**
 * Druid参数配置
 *
 * @author lWX1156935
 * @since 2022/4/22
 */
@Data()
@EqualsAndHashCode(callSuper = false)
@Log4j2
@JsonIgnoreProperties(ignoreUnknown = true)
public class DruidPool extends DruidDataSource implements JdbcPool, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String getPoolName() {
        return DbPoolEnum.POOL_DRUID.getName();
    }

    @Override
    public boolean refresh(JdbcPool jdbcPool, PoolRefreshProcessor processor) {
        return processor != null && processor.check(this, jdbcPool);
    }
}