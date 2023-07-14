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

package com.huawei.saas.exception;

/**
 * 路由失败异常
 *
 * @since 2022-04-19
 */
public class RoutingException extends RuntimeException {

    private static final long serialVersionUID = -8572529625761748960L;

    private int code;

    /**
     * 构造器
     *
     * @param code 错误码
     * @param message 信息
     * @param cause 异常
     */
    public RoutingException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 构造器
     *
     * @param message 信息
     */
    public RoutingException(String message) {
        super(message);
    }

    /**
     * 构造器
     *
     * @param code 错误码
     * @param message 信息
     */
    public RoutingException(int code, String message) {
        this(code, message, null);
    }
}
