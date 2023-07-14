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

import com.huawei.saas.exception.CloseNotSupportException;
import com.huawei.saas.properties.DataSourceProperty;
import com.huawei.saas.utils.StringUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Closeable;
import java.io.IOException;

import javax.sql.DataSource;

/**
 * 数据源快照
 *
 * @since 2022-4-22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SnapshotDataSource {
    private DataSourceProperty property;

    private DataSource source;

    private boolean closed = false;

    /**
     * 关闭数据源
     *
     * @throws IOException 异常
     */
    public void close() throws IOException {
        if (source == null) {
            closed = true;
            return;
        }
        if (!(source instanceof Closeable)) {
            throw new CloseNotSupportException(StringUtil.getUri(property != null ? property.getUrl() : "")
                + "  The connection pool of dataSource {} does not inherit the closeable interface. Therefore, the connection pool cannot be closed");
        }
        ((Closeable) source).close();
        closed = true;
    }
}
