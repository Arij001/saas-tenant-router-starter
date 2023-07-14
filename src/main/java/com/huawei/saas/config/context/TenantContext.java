/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.saas.config.context;

import com.huawei.saas.constants.Constants;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

import lombok.Data;

import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 保存当前租户标识
 *
 * @since 2022-02-14
 */
public class TenantContext {
    private static final HystrixRequestVariableDefault<Map<String, Object>> GLOBAL_CACHE
        = new HystrixRequestVariableDefault<>();

    /**
     * 获取租户标识
     *
     * @return 标识
     */
    public static String getDomain() {
        return Optional.ofNullable(getRoutingBasis()).orElse(new RoutingBasis()).getTenantDomain();
    }

    /**
     * 初始化时设置domain，直到会话结束时才会销毁
     *
     * @param domain 标识
     * @param force 是否强制执行
     */
    public static void setDomain(String domain, boolean force) {
        init(force);
        ((RoutingBasis) GLOBAL_CACHE.get().get(Constants.TENANT_DOMAIN)).setTenantDomain(domain);
    }

    /**
     * 在有租户的前提下，携带数据操作类型
     *
     * @param sqlCommandType 操作类型
     */
    public static void setDbStrategyType(String sqlCommandType) {
        init(false);
        if (Constants.DB_SLAVE.equalsIgnoreCase(sqlCommandType)) {
            return;
        }
        ((RoutingBasis) GLOBAL_CACHE.get().get(Constants.TENANT_DOMAIN)).setMasterSlaveStrategy(sqlCommandType);
    }

    /**
     * 获取当前会话的主从策略
     *
     * @return 策略
     */
    public static String getDbStrategyType() {
        return Optional.ofNullable(getRoutingBasis()).orElse(new RoutingBasis()).getMasterSlaveStrategy();
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    public static RoutingBasis getRoutingBasis() {
        if (!HystrixRequestContext.isCurrentThreadInitialized()) {
            return null;
        }
        Object obj = Optional.ofNullable(GLOBAL_CACHE.get()).orElse(new HashMap<>()).get(Constants.TENANT_DOMAIN);
        if (obj == null) {
            return null;
        }
        return (RoutingBasis) obj;
    }

    /**
     * 当前租户是否schema隔离
     *
     * @param enable enable
     */
    public static void setSchemaIsolation(boolean enable) {
        init(false);
        ((RoutingBasis) GLOBAL_CACHE.get().get(Constants.TENANT_DOMAIN)).setSchemaIsolationEnable(enable);
    }

    /**
     * 获取租户隔离标识
     *
     * @return 是否开启schema隔离
     */
    public static boolean isSchemaIsolation() {
        return Optional.ofNullable(getRoutingBasis()).orElse(new RoutingBasis()).isSchemaIsolationEnable();
    }

    /**
     * 移除
     */
    public static void remove() {
        GLOBAL_CACHE.remove();
    }

    /**
     * 重置缓存
     *
     * @param forceInit 是否强制重置
     */
    public static void init(boolean forceInit) {
        if (HystrixRequestContext.isCurrentThreadInitialized()) {
            if (forceInit) {
                HystrixRequestContext.initializeContext();
            }
        } else {
            HystrixRequestContext.initializeContext();
        }
        if (CollectionUtils.isEmpty(GLOBAL_CACHE.get())) {
            GLOBAL_CACHE.set(new HashMap<>());
        }
        if (GLOBAL_CACHE.get().get(Constants.TENANT_DOMAIN) == null) {
            GLOBAL_CACHE.get().put(Constants.TENANT_DOMAIN, new RoutingBasis());
        }
    }

    /**
     * 租户路由信息
     */
    @Data
    static class RoutingBasis {

        /**
         * 租户标识
         */
        private String tenantDomain;

        /**
         * 读写分离标识
         */
        private String masterSlaveStrategy;

        /**
         * 开启schema隔离标识
         */
        private boolean schemaIsolationEnable = false;
    }
}
