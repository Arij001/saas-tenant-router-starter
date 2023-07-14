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

package com.huawei.saas.interceptor;

import com.huawei.saas.config.binding.SchemaBindingStrategy;
import com.huawei.saas.config.context.TenantContext;
import com.huawei.saas.exception.RoutingException;

import lombok.extern.log4j.Log4j2;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.util.Locale;
import java.util.Optional;

/**
 * mybatis拦截，获取connection
 *
 * @since 2022-02-14
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
@Log4j2
public class MybatisSchemaInterceptor implements Interceptor {
    @Autowired
    private SchemaBindingStrategy schemaBindingStrategy;

    @Override
    public Object intercept(Invocation invocation) throws Exception {
        // 当前租户绑定的数据源未开通schema隔离，不切换schema
        if (!TenantContext.isSchemaIsolation()) {
            return invocation.proceed();
        }

        // 获取传递的租户标识
        String domain = TenantContext.getDomain();
        String catalog = schemaBindingStrategy.getSchema(domain);

        // 当租户绑定的数据源开通了schema隔离时，如果schema为空，则直接抛出异常
        Optional.ofNullable(catalog)
            .orElseThrow(() -> new RoutingException(String.format(Locale.ENGLISH,
                "The schema isolation function is enabled for the data source bound to tenant %s, but no schema is bound",
                domain)));
        log.warn("{} select schema {}", domain, catalog);
        Connection conn = (Connection) invocation.getArgs()[0];
        conn.setCatalog(catalog);
        return invocation.proceed();
    }
}
