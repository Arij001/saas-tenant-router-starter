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

import com.huawei.saas.constants.Constants;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * bean工具类
 *
 * @author lWX1156935
 * @since 2022-07-26
 */
@Log4j2
public class BeanUtil {

    /**
     * 反射创建实例
     *
     * @param type 接口类型
     * @param classLoader 类加载器
     * @return 实现实例列表
     */
    public static <T> List<T> getImplementationList(Class type, ClassLoader classLoader) {
        return SpringFactoriesLoader.loadFactories(type, classLoader);
    }

    /**
     * 复杂结构复制
     *
     * @param object 数据源
     * @param clazz 类型
     * @return 复制结果
     */
    public static Object copy(Object object, Class clazz) {
        return JSONObject.parseObject(JSONObject.toJSONString(object), clazz);
    }

    /**
     * 属性覆盖
     *
     * @param source 源
     * @param target 目标
     */
    public static void merge(Object source, Object target) {
        Field[] fields = target.getClass().getDeclaredFields();
        Method[] methods = target.getClass().getMethods();
        List<String> methodNames = Arrays.stream(methods).map(Method::getName).collect(Collectors.toList());
        List<String> ignoreFields = new LinkedList<>();
        Arrays.stream(fields).forEach(item -> {
            String methodName = Constants.METHOD_GET_PREFIX + StringUtils.capitalize(item.getName());
            if (!methodNames.contains(methodName)) {
                return;
            }
            try {
                Object result = target.getClass().getMethod(methodName).invoke(target);
                if (result != null) {
                    ignoreFields.add(item.getName());
                }
            } catch (NoSuchMethodException e) {
                log.error("Method {} not exits", methodName);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Method {} invoke error", methodName);
            }
        });
        BeanUtils.copyProperties(source, target, ignoreFields.toArray(new String[ignoreFields.size()]));
    }

    /**
     * 属性复制
     *
     * @param source 数据源
     * @param target 目标
     * @param ignoreProperties 忽略属性
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        if (source == null || target == null) {
            return;
        }

        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(source.getClass());
        Set<String> ignoreSet = new HashSet<>();
        if (ignoreProperties != null) {
            ignoreSet.addAll(Arrays.asList(ignoreProperties));
        }
        for (PropertyDescriptor pd : propertyDescriptors) {
            if (ignoreSet.contains(pd.getName())) {
                continue;
            }
            try {
                propertyClone(source, target, pd);
            } catch (IntrospectionException | ReflectiveOperationException e) {
                log.error("Failed to copy attribute {}. Please check the configuration.", pd.getName());
            }
        }
    }

    private static void propertyClone(Object source, Object target, PropertyDescriptor pd)
        throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        String name = pd.getName();
        Method readMethod = pd.getReadMethod();
        Object value = readMethod.invoke(source);
        if (value == null) {
            return;
        }
        if (value instanceof Map) {
            mergeMap(name, target, (Map) value);
            return;
        }
        setField(target, name, value);
    }

    private static void mergeMap(String name, Object obj, Map source)
        throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(name, obj.getClass());
        Method readMethod = propertyDescriptor.getReadMethod();
        Method writeMethod = propertyDescriptor.getWriteMethod();
        Map target = (Map) readMethod.invoke(obj);
        if (target == null) {
            writeMethod.invoke(obj, source);
            return;
        }
        target.putAll(source);
        writeMethod.invoke(obj, source);
    }

    private static void setField(Object target, String name, Object value)
        throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(name, target.getClass());
        Method writeMethod = propertyDescriptor.getWriteMethod();
        writeMethod.invoke(target, value);
    }
}
