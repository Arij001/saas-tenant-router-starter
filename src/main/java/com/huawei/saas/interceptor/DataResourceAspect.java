/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.saas.interceptor;

import com.huawei.saas.config.context.TenantContext;
import com.huawei.saas.constants.Constants;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 数据源切面
 *
 * @author nwx1102896
 * @since 2022-09-08
 */

@Order(-1)
@Aspect
@Component
public class DataResourceAspect {

    /**
     * 使用事务时，数据源切换为主库
     *
     * @param joinPoint 切点
     */
    @Before("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void beforeAdvice(JoinPoint joinPoint) {
        TenantContext.setDbStrategyType(Constants.DB_MASTER);
    }
}
