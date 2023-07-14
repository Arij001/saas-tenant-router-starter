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

package com.huawei.saas.constants;

/**
 * 日志埋点常量
 *
 * @author wx1136431
 * @since 2022-04-21
 */
public interface Constants {
    /**
     * 租户id
     */
    String TENANT_ID = "tenantId";

    /**
     * 租户标识
     */
    String TENANT_DOMAIN = "tenantDomain";

    /**
     * 主库
     */
    String DB_MASTER = "master";

    /**
     * 从库
     */
    String DB_SLAVE = "slave";

    /**
     * get方法前缀
     */
    String METHOD_GET_PREFIX = "get";

    /**
     * 下划线
     */
    String UNDERLINE = "_";

    /**
     * 多种连接池配置属性路径
     */
    String MULTI_POOL_PROPERTY_APPEND = "pools";

    /**
     * 数据源组配置路径
     */
    String DATA_SOURCE_MAP = "data-source-map";

    /**
     * 分隔符
     */
    String DOT = ".";

    /**
     * 问号
     */
    String QUEST_MARK = "\\u003F";
}
