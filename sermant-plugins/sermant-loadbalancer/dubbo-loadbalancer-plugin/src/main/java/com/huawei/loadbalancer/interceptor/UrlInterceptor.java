/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.loadbalancer.interceptor;

import com.huawei.loadbalancer.config.LoadbalancerConfig;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import java.util.Locale;
import java.util.Optional;

/**
 * URL增强类
 *
 * @author provenceee
 * @since 2022-01-20
 */
public class UrlInterceptor extends AbstractInterceptor {
    private static final String LOAD_BALANCE_KEY = "loadbalance";

    private final LoadbalancerConfig config;

    /**
     * 构造方法
     */
    public UrlInterceptor() {
        config = PluginConfigManager.getPluginConfig(LoadbalancerConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments != null && arguments.length > 1 && LOAD_BALANCE_KEY.equals(arguments[1])) {
            // 如果为empty，继续执行原方法，即使用宿主的负载均衡策略
            // 如果不为empty，则使用返回的type并跳过原方法
            getType().ifPresent(context::skip);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private Optional<String> getType() {
        if (config == null || config.getDubboType() == null) {
            // 没有配置的情况下return empty
            return Optional.empty();
        }
        return Optional.of(config.getDubboType().name().toLowerCase(Locale.ROOT));
    }
}