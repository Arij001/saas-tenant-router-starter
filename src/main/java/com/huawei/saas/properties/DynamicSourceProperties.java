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

package com.huawei.saas.properties;

import com.huawei.saas.utils.MapUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.Map;
import java.util.Optional;

/**
 * 动态数据源配置属性
 *
 * @since 2022-4-22
 */
@ConfigurationProperties(prefix = DynamicSourceProperties.PREFIX)
@Data
@RefreshScope
@Log4j2
@EqualsAndHashCode(callSuper = false)
public class DynamicSourceProperties extends PoolProperties {
    /**
     * 配置前缀
     */
    public static final String PREFIX = "spring.datasource.dynamic";

    /**
     * 是否开启配置
     */
    private boolean enable;

    /**
     * 默认源
     */
    private String defaultSource;

    /**
     * 数据源及其配置 key = groupName
     */
    private Map<String, MasterSlaveProperty> dataSourceMap;

    /**
     * 租户与数据源绑定关系
     */
    private Map<String, DataSourceBindingProperty> bindingMap;

    /**
     * 设置数据源组时，组名填充
     *
     * @param dataSourceMap 数据源组
     */
    public void setDataSourceMap(Map<String, MasterSlaveProperty> dataSourceMap) {
        if (MapUtils.isEmpty(dataSourceMap)) {
            return;
        }
        dataSourceMap.entrySet().forEach(entry -> {
            String key = entry.getKey();
            MasterSlaveProperty masterSlaveProperty = entry.getValue();
            Optional.ofNullable(masterSlaveProperty).ifPresent(property -> {
                property.setGroupName(key);
            });
        });
        this.dataSourceMap = dataSourceMap;
    }
}
