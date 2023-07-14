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

import com.huawei.saas.dbpool.PoolStrategy;
import com.huawei.saas.properties.DataSourceProperty;

import javax.sql.DataSource;

/**
 * 创建数据源
 *
 * @since 2022-4-22
 */
public interface DataSourceCreator extends PoolStrategy {
    /**
     * 通过属性创建数据源
     *
     * @param dataSourceProperty 数据源属性
     * @return 被创建的数据源
     */
    DataSource createDataSource(DataSourceProperty dataSourceProperty);

    /**
     * 关闭连接池
     *
     * @param dataSource 数据源
     * @return 关闭结果
     */
    boolean close(DataSource dataSource);
}
