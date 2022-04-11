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

package com.huawei.gray.dubbo.interceptor;

import com.huawei.gray.dubbo.service.AbstractDirectoryService;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huawei.sermant.core.service.ServiceManager;

/**
 * 增强AbstractDirectory的子类的doList方法，筛选灰度应用的地址
 *
 * @author provenceee
 * @since 2021-06-28
 */
public class AbstractDirectoryInterceptor extends AbstractInterceptor {
    private final AbstractDirectoryService abstractDirectoryService;

    /**
     * 构造方法
     */
    public AbstractDirectoryInterceptor() {
        abstractDirectoryService = ServiceManager.getService(AbstractDirectoryService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        context.changeResult(abstractDirectoryService.selectInvokers(context.getObject(), context.getArguments(),
            context.getResult()));
        return context;
    }
}