/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.interceptor;

import java.lang.reflect.Method;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.example.demo.common.DemoLogger;

/**
 * 实例方法的拦截器示例，本示例将展示如何对实例方法进行增强
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoInstInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        DemoLogger.println(obj + ": [DemoInstInterceptor]-before");
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        DemoLogger.println(obj + ": [DemoInstInterceptor]-after");
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        DemoLogger.println(obj + ": [DemoInstInterceptor]-onThrow");
    }
}