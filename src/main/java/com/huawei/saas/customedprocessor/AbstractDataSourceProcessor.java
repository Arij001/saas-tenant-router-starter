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

package com.huawei.saas.customedprocessor;

import com.huawei.saas.dbpool.PoolStrategy;
import com.huawei.saas.properties.DataSourceProperty;

import org.springframework.core.Ordered;

import javax.sql.DataSource;

/**
 * PoolStrategy.getPoolName() 为空时，所有类型连接池都会处理，不为空时，只会针对相应的连接池类型才生效
 * 后续看需求再添加其他生效策略
 *
 * @author lWX1156935
 * @since 2022-07-07
 */
public abstract class AbstractDataSourceProcessor implements Ordered, PoolStrategy {

    /**
     * 处理器前置操作
     *
     * @param property 连接属性
     */
    public abstract void beforeCreate(DataSourceProperty property);

    /**
     * 处理器后置操作
     *
     * @param property 连接属性
     * @param dataSource 数据源
     * @return 数据源
     */
    public abstract DataSource afterCreate(DataSourceProperty property, DataSource dataSource);
}
