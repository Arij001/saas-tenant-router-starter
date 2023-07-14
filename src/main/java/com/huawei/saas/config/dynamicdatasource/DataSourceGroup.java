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

import com.huawei.saas.config.balancestrategy.LoadBalanceStrategy;
import com.huawei.saas.config.context.TenantContext;
import com.huawei.saas.constants.Constants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据源组
 *
 * @since 2022-4-22
 */
@Data
@AllArgsConstructor
@Log4j2
public class DataSourceGroup {
    /**
     * 组名
     */
    private String groupName;

    /**
     * 数据源组是否开起租户schema隔离
     */
    private boolean schemaIsolationEnable;

    /**
     * 负载均衡策略
     */
    private LoadBalanceStrategy loadBalanceStrategy;

    /**
     * 主库
     */
    private List<SnapshotDataSource> masterGroup;

    /**
     * 从库
     */
    private List<SnapshotDataSource> slaveGroup;

    /**
     * 获取组
     *
     * @return 数据源组
     */
    public SnapshotDataSource getDataSource() {
        String dbStrategy = TenantContext.getDbStrategyType();
        Optional.ofNullable(masterGroup).ifPresent(master -> masterGroup = master.stream().filter(item -> !item.isClosed()).collect(Collectors.toList()));
        Optional.ofNullable(slaveGroup).ifPresent(slave ->
            slaveGroup = slave.stream().filter(item -> !item.isClosed()).collect(Collectors.toList()));
        boolean selectMaster = Constants.DB_MASTER.equalsIgnoreCase(dbStrategy) || CollectionUtils.isEmpty(slaveGroup);
        log.warn("{} select db {}", TenantContext.getDomain(), selectMaster ? Constants.DB_MASTER : Constants.DB_SLAVE);
        if (selectMaster) {
            if (CollectionUtils.isEmpty(masterGroup)) {
                log.warn("Failed to write data to the database because data source group {} does not have a valid"
                    + " primary database.", groupName);
            }
            return loadBalanceStrategy.get(masterGroup);
        }
        return loadBalanceStrategy.get(slaveGroup);
    }

    /**
     * 无效数据源组
     *
     * @return 是否有效
     */
    public boolean isInValid() {
        return (CollectionUtils.isEmpty(masterGroup) || masterGroup.stream().allMatch(SnapshotDataSource::isClosed)) && (
            CollectionUtils.isEmpty(slaveGroup) || slaveGroup.stream().allMatch(SnapshotDataSource::isClosed));
    }
}
