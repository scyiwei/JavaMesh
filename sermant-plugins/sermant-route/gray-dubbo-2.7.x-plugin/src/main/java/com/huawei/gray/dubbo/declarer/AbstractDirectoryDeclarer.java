/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.gray.dubbo.declarer;

import com.huawei.sermant.core.plugin.agent.matcher.ClassMatcher;

/**
 * 增强AbstractDirectory的子类的doList方法，筛选灰度应用的地址
 *
 * @author provenceee
 * @since 2021-06-28
 */
public class AbstractDirectoryDeclarer extends AbstractDeclarer {
    private static final String APACHE_ENHANCE_CLASS = "org.apache.dubbo.rpc.cluster.directory.AbstractDirectory";

    private static final String ALIBABA_ENHANCE_CLASS = "com.alibaba.dubbo.rpc.cluster.directory.AbstractDirectory";

    private static final String INTERCEPT_CLASS = "com.huawei.gray.dubbo.interceptor.AbstractDirectoryInterceptor";

    private static final String METHOD_NAME = "doList";

    /**
     * 构造方法
     */
    public AbstractDirectoryDeclarer() {
        super(null, INTERCEPT_CLASS, METHOD_NAME);
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.isExtendedFrom(APACHE_ENHANCE_CLASS).or(ClassMatcher.isExtendedFrom(ALIBABA_ENHANCE_CLASS));
    }
}