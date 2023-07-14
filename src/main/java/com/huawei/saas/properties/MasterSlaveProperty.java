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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 主从对应配置关系
 *
 * @since 2022-4-22
 */
@Data
@Accessors(chain = true)
@Log4j2
@EqualsAndHashCode(callSuper = false)
public class MasterSlaveProperty extends PoolProperties {
    /**
     * 数据源组名称，唯一
     */
    private String groupName;

    /**
     * 允许修改库连接信息，当新的连接属性与使用中的属性不一致时，使用新属性重建连接池，默认关闭
     */
    private boolean modifyEnable = false;

    /**
     * 是否生效
     */
    private boolean enable = true;

    /**
     * 数据源组是否开起租户schema隔离
     */
    private boolean schemaIsolationEnable = false;

    /**
     * 负载均衡策略
     * {@link com.huawei.saas.config.balancestrategy.LoadBalanceStrategy}
     */
    private String loadBalanceStrategy;

    /**
     * 单主库，有需要可以改为多主
     */
    private DataSourceProperty master;

    /**
     * 多从库
     */
    private List<DataSourceProperty> slave;

    /**
     * 合并属性
     *
     * @param poolProperties 池属性
     */
    @Override
    public void mergeConfig(PoolProperties poolProperties) {
        super.mergeConfig(poolProperties);
        if (CollectionUtils.isEmpty(slave)) {
            return;
        }
        slave.forEach(item -> {
            item.setDriverClassName(StringUtils.isBlank(item.getDriverClassName())
                ? master.getDriverClassName()
                : item.getDriverClassName());
            item.setUsername(StringUtils.isBlank(item.getUsername()) ? master.getUsername() : item.getUsername());
            item.setPassword(StringUtils.isBlank(item.getPassword()) ? master.getPassword() : item.getPassword());
        });
    }

    /**
     * 是否无效配置
     *
     * @return 结果
     */
    public boolean isInValid() {
        if (!enable) {
            return true;
        }
        return (master == null || !master.isEnable()) && (CollectionUtils.isEmpty(slave) || slave.stream()
            .noneMatch(DataSourceProperty::isEnable));
    }
}
