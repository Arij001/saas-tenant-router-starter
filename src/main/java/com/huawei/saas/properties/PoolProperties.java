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

import com.huawei.saas.constants.DbPoolEnum;
import com.huawei.saas.dbpool.JdbcPool;
import com.huawei.saas.utils.BeanUtil;
import com.huawei.saas.utils.MapUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 动态数据源配置
 *
 * @since 2022-4-22
 */
@Data
@Log4j2
public class PoolProperties {
    /**
     * 生效的连接池名称
     */
    private String effectivePoolName;

    /**
     * 连接池配置，key=poolName value=配置
     */
    private Map<String, Map<String, Object>> pools;

    /**
     * 配置转换成的对象 key=poolName value=属性配置转换成的连接池配置对象
     */
    @JsonIgnore
    private Map<String, JdbcPool> poolTemplateMap;

    /**
     * 当未配置时，默认使用druid
     *
     * @return 生效的池类型
     */
    public String getEffectivePoolName() {
        return StringUtils.defaultIfBlank(effectivePoolName, DbPoolEnum.POOL_HIKARI.getName());
    }

    /**
     * 获取连接池信息配置对象
     *
     * @param poolName 池名称
     * @return 连接池信息配置
     */
    public JdbcPool getPool(String poolName) {
        if (MapUtils.isEmpty(poolTemplateMap)) {
            return null;
        }
        return poolTemplateMap.get(poolName);
    }

    /**
     * 合并配置属性
     *
     * @param poolProperties 连接池属性
     */
    public void mergeConfig(PoolProperties poolProperties) {
        if (StringUtils.isBlank(effectivePoolName)) {
            this.effectivePoolName = poolProperties.effectivePoolName;
        }
        Map<String, Map<String, Object>> parentConfigs = poolProperties.getPools();
        if (pools == null) {
            pools = new HashMap<>(16);
        }
        Optional.ofNullable(parentConfigs).ifPresent(configs -> configs.entrySet().forEach(item -> {
            String key = item.getKey();
            if (MapUtils.isEmpty(item.getValue())) {
                return;
            }
            Map<String, Object> copy = (Map<String, Object>) BeanUtil.copy(item.getValue(), Map.class);
            pools.put(key, MapUtils.mergeIn(pools.get(key), copy));
        }));
    }

    /**
     * 属性赋值完成之后，属性map转换成对象
     *
     * @param jdbcPoolMap 已注册连接池类型
     */
    public void poolTemplateInit(Map<String, JdbcPool> jdbcPoolMap) {
        if (MapUtils.isEmpty(jdbcPoolMap)) {
            return;
        }
        poolTemplateMap = createPoolTemplateMap(jdbcPoolMap, pools);
    }

    private Map<String, JdbcPool> createPoolTemplateMap(Map<String, JdbcPool> jdbcPoolMap,
        Map<String, Map<String, Object>> poolsConfig) {
        Map<String, JdbcPool> poolMap = new HashMap<>(jdbcPoolMap.size());
        Optional.ofNullable(jdbcPoolMap).ifPresent(clazzMap -> Optional.ofNullable(poolsConfig).ifPresent(configs -> configs.entrySet().forEach(entry -> {
            JdbcPool jdbcPool = clazzMap.get(entry.getKey());
            Optional.ofNullable(jdbcPool).ifPresent(pool -> poolMap.put(entry.getKey(), (JdbcPool) BeanUtil.copy(entry.getValue(), jdbcPool.getClass())));
        })));
        return poolMap;
    }
}
