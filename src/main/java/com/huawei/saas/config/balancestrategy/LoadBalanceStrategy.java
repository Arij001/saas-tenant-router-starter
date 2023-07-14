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

package com.huawei.saas.config.balancestrategy;

import com.huawei.saas.config.dynamicdatasource.SnapshotDataSource;

import java.util.List;

/**
 * 数据源选则负责均衡策略
 *
 * @since 2022-4-22
 */
public interface LoadBalanceStrategy {
    /**
     * 获取负载均衡策略
     *
     * @return 策略
     */
    String getStrategyType();

    /**
     * 获取数据源
     *
     * @param dataSourceList 数据源列表
     * @return 数据源
     */
    SnapshotDataSource get(List<SnapshotDataSource> dataSourceList);
}
