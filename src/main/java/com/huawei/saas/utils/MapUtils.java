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

package com.huawei.saas.utils;

import java.util.Map;

/**
 * map工具类
 *
 * @author lWX1156935
 * @since 2022-06-22
 */
public class MapUtils {
    /**
     * 是否为空
     *
     * @param map map
     * @return 结果
     */
    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    /**
     * map属性合并，target覆盖source
     *
     * @param target 目标
     * @param source 数据源
     * @return 合并map
     */
    public static Map mergeIn(Map<String, Object> target, Map<String, Object> source) {
        if (target == null) {
            return source;
        }
        if (source == null) {
            return target;
        }
        source.putAll(target);
        return source;
    }

}
