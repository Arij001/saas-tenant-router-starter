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

import com.huawei.saas.customedprocessor.PoolRefreshProcessor;
import com.huawei.saas.dbpool.JdbcPool;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 连接属性
 *
 * @since 2022-4-22
 */
@Data
@Accessors(chain = true)
@Log4j2
@EqualsAndHashCode(callSuper = false)
public class DataSourceProperty extends PoolProperties implements Cloneable {
    /**
     * 是否生效
     */
    private boolean enable = true;

    /**
     * JDBC连接参数 driver
     */
    private String driverClassName;

    /**
     * JDBC连接参数 url 地址
     */
    private String url;

    /**
     * JDBC连接参数 用户名
     */
    private String username;

    /**
     * JDBC连接参数 密码
     */
    private String password;

    /**
     * 是否有效
     *
     * @return 判断结果
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(driverClassName) && StringUtils.isNotBlank(url) && StringUtils.isNotBlank(
            username) && StringUtils.isNotBlank(password);
    }

    /**
     * 比较数据库配置是否被修改，作为重建连接池的依据，不考虑连接池属性
     *
     * @param dataSourceProperty 新的库属性
     * @return 比较结果
     */
    public boolean dbPropertiesModified(DataSourceProperty dataSourceProperty) {
        if (dataSourceProperty == null) {
            return true;
        }
        return !(StringUtils.equals(driverClassName, dataSourceProperty.getDriverClassName()) && StringUtils.equals(url,
            dataSourceProperty.getUrl()) && StringUtils.equals(username, dataSourceProperty.getUsername())
            && StringUtils.equals(password, dataSourceProperty.getPassword()));
    }

    /**
     * 池属性是否被修改，作为刷新连接池判断依据
     *
     * @param dataSourceProperty 新的库属性
     * @param processorMap 自定义处理器
     * @return 判断结果
     */
    public boolean poolRefresh(DataSourceProperty dataSourceProperty, Map<String, PoolRefreshProcessor> processorMap) {
        if (dataSourceProperty == null) {
            return false;
        }

        // 最新配置
        JdbcPool currentPool = getPoolTemplateMap().get(getEffectivePoolName());

        // 备份的当前生效的配置
        JdbcPool originPool = dataSourceProperty.getPoolTemplateMap().get(dataSourceProperty.getEffectivePoolName());
        return originPool.refresh(currentPool, processorMap.get(currentPool.getPoolName()));
    }

    @Override
    public DataSourceProperty clone() throws CloneNotSupportedException {
        DataSourceProperty clone = JSONObject.parseObject(JSONObject.toJSONString(super.clone()), this.getClass());
        clone.setPoolTemplateMap(getPoolTemplateMap());
        return clone;
    }
}
