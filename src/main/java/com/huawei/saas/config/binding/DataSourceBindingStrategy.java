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

package com.huawei.saas.config.binding;

import com.huawei.saas.config.dynamicdatasource.DataSourceGroup;
import com.huawei.saas.properties.DynamicSourceProperties;

import java.util.Map;

/**
 * 数据源选取适配
 *
 * @since 2022-04-27
 */
public interface DataSourceBindingStrategy {
    /**
     * 通过关键字获取数据源组
     *
     * @param dataSourceMap 数据源
     * @param dynamicSourceProperties 属性
     * @return 数据源组
     */
    DataSourceGroup getDataSource(Map<String, DataSourceGroup> dataSourceMap, DynamicSourceProperties dynamicSourceProperties);
}
