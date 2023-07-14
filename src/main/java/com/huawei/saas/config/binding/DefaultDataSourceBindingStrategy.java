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

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 租户未配置绑定数据源组且未扩展DataSourceBindingStrategy时，使用此默认扩展适配器
 *
 * @since 2022-04-27
 */
public class DefaultDataSourceBindingStrategy implements DataSourceBindingStrategy {
    @Override
    public DataSourceGroup getDataSource(Map<String, DataSourceGroup> dataSourceMap, DynamicSourceProperties dynamicSourceProperties) {
        String defaultSource = dynamicSourceProperties.getDefaultSource();

        // 默认配置优先， 没有默认配置时如果只配置了一个数据源
        return StringUtils.isNotBlank(defaultSource) ? dataSourceMap.get(defaultSource)
            : (dataSourceMap.size() == 1 ? dataSourceMap.entrySet().iterator().next().getValue() : null);
    }
}
