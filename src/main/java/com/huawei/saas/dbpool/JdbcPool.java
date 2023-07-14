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

package com.huawei.saas.dbpool;

import com.huawei.saas.customedprocessor.PoolRefreshProcessor;

/**
 * jdbc连接池
 *
 * @since 2022-4-22
 */
public interface JdbcPool extends PoolStrategy {
    /**
     * 是否需要刷新连接池属性，重建连接池, 平滑切换
     *
     * @param jdbcPool 新的池连接属性
     * @param processor 处理器
     * @return 判断结果
     */
    boolean refresh(JdbcPool jdbcPool, PoolRefreshProcessor processor);
}
