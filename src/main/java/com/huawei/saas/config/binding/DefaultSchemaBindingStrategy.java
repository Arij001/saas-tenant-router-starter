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

import com.huawei.saas.properties.DataSourceBindingProperty;
import com.huawei.saas.properties.DynamicSourceProperties;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 默认schema转换逻辑，未自定义情况下使用此默认转换逻辑
 *
 * @since 2022-4-22
 */
public class DefaultSchemaBindingStrategy implements SchemaBindingStrategy {
    @Autowired
    private DynamicSourceProperties dynamicSourceProperties;

    @Override
    public String getSchema(String key) {
        // 租户标识获取配置的对应schema以及数据源绑定信息
        DataSourceBindingProperty bindingProperty = dynamicSourceProperties.getBindingMap() != null
            ? dynamicSourceProperties.getBindingMap().getOrDefault(key, null)
            : null;
        String catalog = null;
        if (bindingProperty != null && StringUtils.isNotBlank(bindingProperty.getSchema())) {
            // 配置指定的schema, 优先级最高
            catalog = bindingProperty.getSchema();
        }
        return catalog;
    }
}
