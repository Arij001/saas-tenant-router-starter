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

/**
 * 连接池刷新判断处理器
 *
 * @author lWX1156935
 * @since 2022-06-29
 */
public interface PoolRefreshProcessor extends PoolStrategy {
    /**
     * 检查
     *
     * @param origin 原始属性
     * @param current 最新属性
     * @return 检查结果
     */
    boolean check(Object origin, Object current);
}
