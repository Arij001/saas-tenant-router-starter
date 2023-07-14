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
import com.huawei.saas.config.balancestrategy.RandomLoadBalanceStrategy;
import com.huawei.saas.config.binding.DataSourceBindingStrategy;
import com.huawei.saas.config.context.TenantContext;
import com.huawei.saas.constants.Constants;
import com.huawei.saas.customedprocessor.AbstractDataSourceProcessor;
import com.huawei.saas.customedprocessor.PoolRefreshProcessor;
import com.huawei.saas.dbpool.JdbcPool;
import com.huawei.saas.dbpool.PoolStrategy;
import com.huawei.saas.dbpool.creator.DataSourceCreator;
import com.huawei.saas.exception.RoutingException;
import com.huawei.saas.properties.DataSourceBindingProperty;
import com.huawei.saas.properties.DataSourceProperty;
import com.huawei.saas.properties.DynamicSourceProperties;
import com.huawei.saas.properties.MasterSlaveProperty;
import com.huawei.saas.utils.BeanUtil;
import com.huawei.saas.utils.StringUtil;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.sql.DataSource;

/**
 * 动态数据源
 *
 * @author lWX1156935
 * @since 2022/4/22
 */
@Log4j2
public class DynamicRoutingDataSource extends AbstractDataSource implements DataSourceRegistry {
    private final ConcurrentHashMap<String, DataSourceGroup> dataSourceGroupMap = new ConcurrentHashMap<>(16);

    // 数据源创建器列表
    private final Map<String, DataSourceCreator> creatorMap = new HashMap<>(16);

    // 动态数据源最新配置
    private DynamicSourceProperties dynamicSourceProperties;

    // 已实现jdbc连接池类型
    private final Map<String, JdbcPool> poolTypeRegistry = new HashMap<>(16);

    private final Map<String, PoolRefreshProcessor> poolRefreshProcessorMap = new HashMap<>(16);

    private final List<AbstractDataSourceProcessor> dataSourceProcessorList = new ArrayList<>(10);

    @Autowired
    private DataSourceBindingStrategy dataSourceBindingStrategy;

    /**
     * 构造器
     *
     * @param dataSourceCreators 数据源创建器列表
     * @param dynamicSourceProperties 配置属性
     */
    public DynamicRoutingDataSource(List<DataSourceCreator> dataSourceCreators,
        DynamicSourceProperties dynamicSourceProperties) {
        creatorMap.putAll(dataSourceCreators.stream()
            .filter(item -> item.getPoolName() != null)
            .collect(Collectors.toMap(item -> item.getPoolName().toLowerCase(Locale.ENGLISH), creator -> creator)));
        this.dynamicSourceProperties = dynamicSourceProperties;
        registerJdbcPool();
        poolRefreshProcessInit();
        dataSourceProcessorInit();
        poolBeanInit(this.dynamicSourceProperties, poolTypeRegistry);
        Map<String, MasterSlaveProperty> map = this.dynamicSourceProperties.getDataSourceMap();
        if (map == null || map.isEmpty()) {
            return;
        }
        for (Map.Entry<String, MasterSlaveProperty> entry : map.entrySet()) {
            registerDataSource(entry.getKey(), entry.getValue());
        }
    }

    private void dataSourceProcessorInit() {
        dataSourceProcessorList.addAll(
            BeanUtil.getImplementationList(AbstractDataSourceProcessor.class, this.getClass().getClassLoader()));
    }

    private void poolRefreshProcessInit() {
        List<PoolRefreshProcessor> processors = BeanUtil.getImplementationList(PoolRefreshProcessor.class,
            this.getClass().getClassLoader());
        if (CollectionUtils.isEmpty(processors)) {
            return;
        }
        poolRefreshProcessorMap.putAll(
            processors.stream().collect(Collectors.toMap(PoolStrategy::getPoolName, item -> item)));
    }

    /**
     * 注册已注入的连接池类型
     */
    private void registerJdbcPool() {
        List<JdbcPool> pools = BeanUtil.getImplementationList(JdbcPool.class, this.getClass().getClassLoader());
        if (CollectionUtils.isEmpty(pools)) {
            return;
        }
        poolTypeRegistry.putAll(pools.stream().collect(Collectors.toMap(PoolStrategy::getPoolName, item -> item)));
    }

    @Override
    public void registerDataSource(String groupName, MasterSlaveProperty masterSlaveProperty) {
        if (masterSlaveProperty.isInValid()) {
            log.warn("Invalid configuration. Failed to create data source group {}.",
                masterSlaveProperty.getGroupName());
            return;
        }

        List<LoadBalanceStrategy> balanceStrategies = BeanUtil.getImplementationList(LoadBalanceStrategy.class,
            this.getClass().getClassLoader());
        Map<String, LoadBalanceStrategy> balanceStrategyMap = balanceStrategies.stream()
            .collect(Collectors.toMap(LoadBalanceStrategy::getStrategyType, item -> item));
        LoadBalanceStrategy loadBalanceStrategy = balanceStrategyMap.getOrDefault(
            masterSlaveProperty.getLoadBalanceStrategy(), new RandomLoadBalanceStrategy());

        // 解析出区分master|slave|schema的结构
        DataSourceGroup dataSourceGroup = new DataSourceGroup(groupName, masterSlaveProperty.isSchemaIsolationEnable(),
            loadBalanceStrategy, createDataSource(masterSlaveProperty.getMaster(), Constants.DB_MASTER, groupName),
            createDataSource(masterSlaveProperty.getSlave(), Constants.DB_SLAVE, groupName));
        dataSourceGroupMap.put(groupName, dataSourceGroup);
    }

    @Override
    public void unRegisterDataSource(DataSourceGroup dataSourceGroup) throws IOException {
        if (dataSourceGroup == null || dataSourceGroup.isInValid()) {
            return;
        }
        if (!CollectionUtils.isEmpty(dataSourceGroup.getMasterGroup())) {
            closeDataSource(dataSourceGroup.getMasterGroup());
        }
        if (!CollectionUtils.isEmpty(dataSourceGroup.getSlaveGroup())) {
            closeDataSource(dataSourceGroup.getSlaveGroup());
        }

        // master 和 slave 都已经被注销，从数据源列表中删除
        if (dataSourceGroup.isInValid()) {
            dataSourceGroupMap.remove(dataSourceGroup.getGroupName());
        }
    }

    private void unRegisterDataSource(SnapshotDataSource snapshotDataSource) throws IOException {
        if (snapshotDataSource == null || snapshotDataSource.isClosed()) {
            return;
        }
        snapshotDataSource.close();
    }

    private void closeDataSource(List<SnapshotDataSource> dataSourceList) throws IOException {
        if (CollectionUtils.isEmpty(dataSourceList)) {
            return;
        }
        for (SnapshotDataSource dataSource : dataSourceList) {
            dataSource.close();
        }
    }

    @Override
    public DataSourceGroup getDataSourceGroup(String key) {
        DataSourceGroup dataSourceGroup = null;
        Map<String, DataSourceBindingProperty> bindingMap = dynamicSourceProperties.getBindingMap();
        if (bindingMap != null && bindingMap.get(key) != null && StringUtils.isNotBlank(
            bindingMap.get(key).getGroupName())) {
            // 配置指定的数据源有效时
            dataSourceGroup = dataSourceGroupMap.get(bindingMap.get(key).getGroupName());
            // 配置显示绑定的数据源不存在，直接抛出异常
            Optional.ofNullable(dataSourceGroup)
                .orElseThrow(() -> new RoutingException(
                    String.format(Locale.ENGLISH, "The data source group bound to %s does not exist", key)));
        }

        // 指定数据源无效时, 从扩展的数据源适配器中获取数据源组
        if (dataSourceGroup == null) {
            dataSourceGroup = dataSourceBindingStrategy.getDataSource(dataSourceGroupMap, dynamicSourceProperties);
        }
        Optional.ofNullable(dataSourceGroup)
            .orElseThrow(() -> new RoutingException(
                String.format(Locale.ENGLISH, "The tenant %s is not bound to a data source.", key)));
        Optional.ofNullable(dataSourceGroup)
            .ifPresent(source -> log.warn("{} select DataSource {} success!", key,
                StringUtils.defaultIfBlank(source.getGroupName(), "")));
        return dataSourceGroup;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getDataSource().getSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getDataSource().getSource().getConnection(username, password);
    }

    /**
     * 属性动态变化之后，判断是否重建或者新建或者删除链接池，即动态管理连接池
     * 使用的是spring的@RefreshScope 机制，如使用的动态刷新逻辑中未使用此事件通知机制，需自行实现触发事件
     *
     * @param event 事件
     */
    @EventListener
    public void refresh(RefreshScopeRefreshedEvent event) {
        // 判断各连接池的实现，确定是否重建连接池，先新增，再更新，最后才删除
        Map<String, MasterSlaveProperty> dataSourceMap = dynamicSourceProperties.getDataSourceMap();
        // 避免误操作，当最新的配置没有数据源信息时，不进行任何操作
        if (dataSourceMap == null || dataSourceMap.isEmpty()) {
            return;
        }

        // 配置重置，仅仅针对连接池以及数据库属性配置
        poolBeanInit(dynamicSourceProperties, poolTypeRegistry);

        // 刷新数据源
        refresh(dynamicSourceProperties);

        // 清理已关闭数据源
        clear();
    }

    private void clear() {
        List<String> remove = new ArrayList<>(16);
        Optional.of(dataSourceGroupMap).ifPresent(groups -> groups.forEach((key, value) -> {
            removeClosedDataSource(value.getMasterGroup());
            removeClosedDataSource(value.getSlaveGroup());
            if (value.isInValid()) {
                remove.add(key);
            }
        }));
        remove.forEach(dataSourceGroupMap::remove);
    }

    private void removeClosedDataSource(List<SnapshotDataSource> snapshotDataSources) {
        List<SnapshotDataSource> remove = new ArrayList<>(16);
        Optional.ofNullable(snapshotDataSources)
            .ifPresent(sources -> remove.addAll(
                sources.stream().filter(SnapshotDataSource::isClosed).collect(Collectors.toList())));
        if (!CollectionUtils.isEmpty(snapshotDataSources) && !CollectionUtils.isEmpty(remove)) {
            snapshotDataSources.removeAll(remove);
        }
    }

    /**
     * 比较新旧属性，按逻辑选择数据源操作
     *
     * @param lastProperties 最新属性
     */
    private void refresh(DynamicSourceProperties lastProperties) {
        Map<String, MasterSlaveProperty> lastPropertyMap = lastProperties.getDataSourceMap();
        Set<String> effectiveSet = dataSourceGroupMap.keySet();
        Set<String> unionSet = new HashSet<>(lastPropertyMap.keySet());
        unionSet.addAll(effectiveSet);
        unionSet.forEach(groupName -> {
            MasterSlaveProperty lastProperty = lastPropertyMap.get(groupName);
            // 当前生效数据源的属性配置判断是否支持修改，如果支持，新配置缺失，则删除数据源
            if (lastProperty == null || lastPropertyMap.get(groupName).isInValid()) {
                try {
                    unRegisterDataSource(dataSourceGroupMap.get(groupName));
                } catch (IOException e) {
                    log.error("DataSourceGroup {} close error", groupName);
                    throw new RoutingException(groupName + " close error");
                }
                return;
            }

            if (effectiveSet.contains(groupName)) {
                // 不允许修改已生效数据源
                if (!lastPropertyMap.get(groupName).isModifyEnable()) {
                    return;
                }
                // 修改了schema隔离模式，需要重建数据源，重置连接
                if (lastPropertyMap.get(groupName).isSchemaIsolationEnable() != dataSourceGroupMap.get(groupName)
                    .isSchemaIsolationEnable()) {
                    try {
                        unRegisterDataSource(dataSourceGroupMap.get(groupName));
                        registerDataSource(groupName, lastPropertyMap.get(groupName));
                    } catch (IOException e) {
                        log.error(
                            "Failed to modify the schema isolation mode for {}. Failed to recreate the data source",
                            groupName);
                        throw new RoutingException(groupName + " rebuild datasourceGroup error");
                    }
                    return;
                }
                modify(lastPropertyMap.get(groupName));
                return;
            }
            registerDataSource(groupName, lastPropertyMap.get(groupName));
        });
    }

    private void modify(MasterSlaveProperty last) {
        if (last == null) {
            return;
        }
        List<DataSourceProperty> properties = new ArrayList<>(10);
        properties.add(last.getMaster());
        doRefresh(last.getGroupName(), Constants.DB_MASTER, properties,
            dataSourceGroupMap.get(last.getGroupName()).getMasterGroup());
        doRefresh(last.getGroupName(), Constants.DB_SLAVE, last.getSlave(),
            dataSourceGroupMap.get(last.getGroupName()).getSlaveGroup());
    }

    private void doRefresh(String groupName, String dbType, List<DataSourceProperty> last,
        List<SnapshotDataSource> effective) {
        Map<String, SnapshotDataSource> effectiveMap = effective != null ? effective.stream()
            .filter(item -> item.getProperty() != null)
            .collect(Collectors.toMap(item -> StringUtil.getUri(item.getProperty().getUrl()), item -> item,
                (itemA, itemB) -> itemA)) : new HashMap<>(0);
        Map<String, DataSourceProperty> lastMap = last != null
            ? last.stream()
            .collect(Collectors.toMap(item -> StringUtil.getUri(item.getUrl()), item -> item, (itemA, itemB) -> itemA))
            : new HashMap<>(0);
        Set<String> keyUnionSet = new HashSet<>();
        keyUnionSet.addAll(new HashSet<>(effectiveMap.keySet()));
        keyUnionSet.addAll(new HashSet<>(lastMap.keySet()));
        keyUnionSet.forEach(url -> doRefresh(groupName, dbType, lastMap.get(url), effectiveMap.get(url)));
    }

    /**
     * 执行刷新
     *
     * @param groupName 组名
     * @param dbType 主从类型
     * @param dataSource 对应生效的池化数据源
     * @param lastedProperty 最新配置
     */
    private void doRefresh(String groupName, String dbType, DataSourceProperty lastedProperty,
        SnapshotDataSource dataSource) {
        // 关闭数据源
        if (lastedProperty == null || !lastedProperty.isEnable()) {
            try {
                // 关闭连接池,如果关闭异常，则修改属性失败，需要重新触发事件重建连接池
                unRegisterDataSource(dataSource);
            } catch (IOException e) {
                log.error("Failed to close the data source {}", groupName);
            }
            return;
        }

        // 新增数据源
        if (dataSource == null) {
            addDataSource(groupName, dbType, lastedProperty);
            return;
        }

        // 修改数据源
        if (dataSource.getProperty() != null && (!lastedProperty.dbPropertiesModified(dataSource.getProperty())
            && !dataSource.getProperty().poolRefresh(lastedProperty, poolRefreshProcessorMap))) {
            // 连接属性&池属性均未修改
            return;
        }
        try {
            // 关闭组内指定数据源连接池,如果关闭异常，则修改属性失败，需要重新触发事件重建连接池
            unRegisterDataSource(dataSource);

            // 关闭之后，使用最新配置构建数据源连接池，加入数据源组，同时记录对应的配置属性
            addDataSource(groupName, dbType, lastedProperty);
        } catch (IOException e) {
            log.error("Failed to close the data source {}",
                getIdentity(groupName, dbType, dataSource.getProperty().getUrl()));
        }
    }

    /**
     * 已存在数据源组，组内新增数据源
     *
     * @param groupName 组名
     * @param dbType 类型
     * @param dataSourceProperty 属性
     */
    private void addDataSource(String groupName, String dbType, DataSourceProperty dataSourceProperty) {
        DataSourceGroup dataSourceGroup = dataSourceGroupMap.get(groupName);
        if (dataSourceGroup == null) {
            return;
        }
        List<SnapshotDataSource> dataSource = createDataSource(dataSourceProperty, dbType, groupName);
        if (CollectionUtils.isEmpty(dataSource)) {
            return;
        }
        if (Constants.DB_MASTER.equalsIgnoreCase(dbType)) {
            if (dataSourceGroup.getMasterGroup() == null) {
                dataSourceGroup.setMasterGroup(new ArrayList<>());
            }
            dataSourceGroup.getMasterGroup().addAll(dataSource);
            return;
        }
        if (dataSourceGroup.getSlaveGroup() == null) {
            dataSourceGroup.setSlaveGroup(new ArrayList<>());
        }
        dataSourceGroup.getSlaveGroup().addAll(dataSource);
    }

    /**
     * 连接池&数据库属性初始化
     *
     * @param properties 动态数据源配置
     * @param poolRegistry 已注册池类型列表
     */
    private void poolBeanInit(DynamicSourceProperties properties, Map<String, JdbcPool> poolRegistry) {
        Map<String, MasterSlaveProperty> map = properties.getDataSourceMap();
        if (map == null || map.isEmpty()) {
            return;
        }
        for (Map.Entry<String, MasterSlaveProperty> entry : map.entrySet()) {
            MasterSlaveProperty masterSlaveProperty = entry.getValue();
            // 全局连接池配置合并到组级别配置
            masterSlaveProperty.mergeConfig(properties);
            if (masterSlaveProperty.getMaster() != null) {
                Optional.ofNullable(masterSlaveProperty.getMaster()).ifPresent(master -> {
                    master.mergeConfig(masterSlaveProperty);
                    master.poolTemplateInit(poolRegistry);
                });
                Optional.ofNullable(masterSlaveProperty.getSlave()).ifPresent(slaves -> slaves.forEach(slave -> {
                    slave.mergeConfig(masterSlaveProperty);
                    slave.poolTemplateInit(poolRegistry);
                }));
            }
        }
    }

    private SnapshotDataSource getDataSource() {
        DataSourceGroup dataSourceGroup = getDataSourceGroup(TenantContext.getDomain());
        TenantContext.setSchemaIsolation(dataSourceGroup.isSchemaIsolationEnable());
        SnapshotDataSource dataSource = dataSourceGroup.getDataSource();
        Optional.ofNullable(dataSourceGroup)
            .orElseThrow(() -> new RuntimeException(TenantContext.getDomain() + " datasourceGroup not configured"));
        Optional.ofNullable(dataSource)
            .orElseThrow(() -> new NullArgumentException(TenantContext.getDomain() + " datasource not configured"));
        Optional.ofNullable(dataSource.getSource())
            .orElseThrow(() -> new NullArgumentException(TenantContext.getDomain() + " datasource closed"));
        return dataSource;
    }

    private List<SnapshotDataSource> createDataSource(DataSourceProperty dataSourceProperty, String dbType,
        String sourceName) {
        List<DataSourceProperty> list = new ArrayList<>();
        list.add(dataSourceProperty);
        return createDataSource(list, dbType, sourceName);
    }

    private List<SnapshotDataSource> createDataSource(List<DataSourceProperty> dataSourceProperties, String dbType,
        String groupName) {
        if (CollectionUtils.isEmpty(dataSourceProperties)) {
            return new ArrayList<>();
        }

        // 创建连接池列表
        List<SnapshotDataSource> dataSources = new ArrayList<>(dataSourceProperties.size());
        dataSourceProperties.forEach(item -> {
            // 同一组配置中，库地址重复
            if (!item.isEnable()) {
                log.warn("Data source {} is set to false and will not be created.",
                    getIdentity(groupName, dbType, item.getUrl()));
                return;
            }

            // 默认使用hikari连接池
            String poolName = item.getEffectivePoolName().toLowerCase(Locale.ENGLISH);
            DataSourceCreator creator = creatorMap.get(poolName);

            // 指定了不存在的连接池类型
            if (creator == null) {
                log.error("Jdbc Pool {} does not exists", poolName);
                return;
            }

            try {
                dataSources.add(createDataSource(item, creator));
            } catch (CloneNotSupportedException e) {
                log.error("DataSource {} create error {}", getIdentity(groupName, dbType, item.getUrl()),
                    ExceptionUtils.getFullStackTrace(e));
                return;
            }
            log.warn("Jdbc Pool {} create success", getIdentity(groupName, dbType, item.getUrl()));
        });
        return dataSources;
    }

    private SnapshotDataSource createDataSource(DataSourceProperty property, DataSourceCreator creator)
        throws CloneNotSupportedException {
        List<AbstractDataSourceProcessor> validProcessors = dataSourceProcessorList.stream()
            .filter(item -> StringUtils.isBlank(item.getPoolName()) || StringUtils.equals(item.getPoolName(),
                creator.getPoolName()))
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(validProcessors)) {
            return new SnapshotDataSource(property.clone(), creator.createDataSource(property), false);
        }

        // 针对单个数据源的前后置处理器，before() 执行顺序是order 高 -> 低，after() 是 低 -> 高
        for (AbstractDataSourceProcessor processor : validProcessors) {
            processor.beforeCreate(property);
        }
        DataSource dataSource = creator.createDataSource(property);
        int size = validProcessors.size();
        for (int index = size - 1; index > 0; index--) {
            dataSource = validProcessors.get(index).afterCreate(property, dataSource);
        }
        return new SnapshotDataSource(property.clone(), dataSource, false);
    }

    private String getIdentity(String sourceName, String dbType, String url) {
        return String.join(Constants.UNDERLINE, sourceName, dbType,
            String.valueOf(StringUtils.defaultIfBlank(url, "").hashCode()));
    }
}
