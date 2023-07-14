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
 * 关闭异常
 *
 * @since 2022-07-13
 */
public class CloseNotSupportException extends RuntimeException {
    private static final long serialVersionUID = -3010107192188843973L;

    private int code;

    /**
     * 构造器
     *
     * @param code 异常码
     * @param message 异常信息
     * @param cause cause
     */
    public CloseNotSupportException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 异常
     *
     * @param message 信息
     */
    public CloseNotSupportException(String message) {
        super(message);
    }

    /**
     * 异常
     *
     * @param code 异常码
     * @param message 信息
     */
    public CloseNotSupportException(int code, String message) {
        this(code, message, null);
    }
}
