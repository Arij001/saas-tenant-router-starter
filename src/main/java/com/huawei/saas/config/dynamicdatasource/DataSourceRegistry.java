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

package com.huawei.saas.config.dynamicdatasource;

import com.huawei.saas.properties.MasterSlaveProperty;

/**
 * 注册数据源,提供一个默认实现，可扩展为其他实现
 *
 * @since 2022-4-22
 */
public interface DataSourceRegistry {

    /**
     * 注册数据源，更新时需要移除老的数据源，以最新配置为准
     *
     * @param key 关键字
     * @param masterSlaveProperty 主从结构数据源
     */
    void registerDataSource(String key, MasterSlaveProperty masterSlaveProperty);

    /**
     * 移除数据源并关闭连接池
     *
     * @param dataSourceGroup 需要注销的数据源
     * @throws Exception 异常
     */
    void unRegisterDataSource(DataSourceGroup dataSourceGroup) throws Exception;

    /**
     * 获取数据源， 在获取数据源时实现读写分离，负载均衡，以及事务全部走主等等
     *
     * @param key 租户信息指定的数据源
     * @return 数据源
     */
    DataSourceGroup getDataSourceGroup(String key);
}
