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

package com.huawei.saas.config;

import com.huawei.saas.config.balancestrategy.LoadBalanceStrategy;
import com.huawei.saas.config.balancestrategy.RandomLoadBalanceStrategy;
import com.huawei.saas.config.binding.DataSourceBindingStrategy;
import com.huawei.saas.config.binding.DefaultDataSourceBindingStrategy;
import com.huawei.saas.config.binding.DefaultSchemaBindingStrategy;
import com.huawei.saas.config.binding.SchemaBindingStrategy;
import com.huawei.saas.config.dynamicdatasource.DataSourceRegistry;
import com.huawei.saas.config.dynamicdatasource.DynamicRoutingDataSource;
import com.huawei.saas.dbpool.creator.DataSourceCreator;
import com.huawei.saas.dbpool.creator.DruidDataSourceCreator;
import com.huawei.saas.dbpool.creator.HikariDataSourceCreator;
import com.huawei.saas.dbpool.druid.DruidDynamicDataSourceConfiguration;
import com.huawei.saas.interceptor.MybatisReadWriteSeparationInterceptor;
import com.huawei.saas.interceptor.MybatisSchemaInterceptor;
import com.huawei.saas.interceptor.TenantDomainInterceptor;
import com.huawei.saas.properties.DynamicSourceProperties;

import lombok.extern.log4j.Log4j2;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

import javax.sql.DataSource;

/**
 * 自动装配
 *
 * @since 2022-02-14
 */
@Configuration
@EnableConfigurationProperties(value = {DynamicSourceProperties.class})
@Log4j2
@AutoConfigureBefore(value = DataSourceAutoConfiguration.class,
    name = "com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure")
@Import(value = {DruidDynamicDataSourceConfiguration.class})
@ConditionalOnProperty(prefix = "spring.datasource.dynamic", name = "enable", havingValue = "true")
public class TenantAutoConfiguration {
    /**
     * mybatis schema选择拦截器
     *
     * @return mybatis拦截器
     */
    @Bean
    public MybatisSchemaInterceptor mybatisInterceptor() {
        return new MybatisSchemaInterceptor();
    }

    /**
     * mybatis读写分离拦截器
     *
     * @return mybatis拦截器
     */
    @Bean
    public MybatisReadWriteSeparationInterceptor mybatisReadWriteSeparationInterceptor() {
        return new MybatisReadWriteSeparationInterceptor();
    }

    /**
     * 支持多数据源，多schema配置
     *
     * @param property 配置属性
     * @param dataSourceCreators 数据源创建器
     * @return 动态数据源
     */
    @Bean
    public DataSource dynamicDataSource(DynamicSourceProperties property, List<DataSourceCreator> dataSourceCreators) {
        // 传入全局配置
        DataSourceRegistry registry = new DynamicRoutingDataSource(dataSourceCreators, property);
        return (DataSource) registry;
    }

    /**
     * 负载均衡-随机算法
     *
     * @return 随机算法
     */
    @Bean
    public LoadBalanceStrategy randomStrategy() {
        return new RandomLoadBalanceStrategy();
    }

    /**
     * 注入schema选择策略
     *
     * @return bean
     */
    @Bean
    @ConditionalOnMissingBean(SchemaBindingStrategy.class)
    public SchemaBindingStrategy defaultSchemaAdapter() {
        return new DefaultSchemaBindingStrategy();
    }

    /**
     * 注入数据源绑定策略
     *
     * @return bean
     */
    @Bean
    @ConditionalOnMissingBean(DataSourceBindingStrategy.class)
    public DataSourceBindingStrategy defaultDataSourceAdapter() {
        return new DefaultDataSourceBindingStrategy();
    }

    /**
     * 数据源创建器-druid
     *
     * @return druid数据源创建器
     */
    @Bean
    public DataSourceCreator druidDataSourceCreator() {
        return new DruidDataSourceCreator();
    }

    /**
     * 数据源创建器-hikari
     *
     * @return hikari数据源创建器
     */
    @Bean
    public DataSourceCreator hikariDataSourceCreator() {
        return new HikariDataSourceCreator();
    }

    /**
     * 默认的schema转换逻辑，需要进行schema路由时使用
     *
     * @return 默认schema适配器
     */
    @Bean
    @ConditionalOnMissingBean(SchemaBindingStrategy.class)
    public SchemaBindingStrategy defaultSchemaStrategy() {
        return new DefaultSchemaBindingStrategy();
    }

    /**
     * 默认的数据源适配逻辑，未自定义扩展时使用
     *
     * @return 数据源适配器
     */
    @Bean
    @ConditionalOnMissingBean(DataSourceBindingStrategy.class)
    public DataSourceBindingStrategy defaultDataSourceStrategy() {
        return new DefaultDataSourceBindingStrategy();
    }

    /**
     * 设置租户标识
     *
     * @return bean
     */
    @Bean
    public HandlerInterceptor tenantDomainInterceptor() {
        return new TenantDomainInterceptor();
    }
}
