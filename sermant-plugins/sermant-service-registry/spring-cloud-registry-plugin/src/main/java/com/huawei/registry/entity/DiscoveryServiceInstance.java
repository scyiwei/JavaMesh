/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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
 *
 */

package com.huawei.registry.entity;

import org.springframework.cloud.client.ServiceInstance;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

/**
 * ServiceInstance实现
 *
 * @author zhouss
 * @since 2022-03-29
 */
public class DiscoveryServiceInstance implements ServiceInstance {
    private final MicroServiceInstance microServiceInstance;

    private final String serviceId;

    /**
     * 针对springCloud ServiceInstance构造器
     *
     * @param microServiceInstance 实例信息
     * @param serviceId            spring应用服务名
     * @since 2022-03-29
     */
    public DiscoveryServiceInstance(final MicroServiceInstance microServiceInstance, final String serviceId) {
        this.microServiceInstance = microServiceInstance;
        this.serviceId = serviceId == null ? microServiceInstance.getServiceId() : serviceId;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getHost() {
        return microServiceInstance.getHost();
    }

    @Override
    public int getPort() {
        return microServiceInstance.getPort();
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public URI getUri() {
        return URI.create(String.format(Locale.ENGLISH, "http://%s:%s", getHost(), getPort()));
    }

    @Override
    public Map<String, String> getMetadata() {
        return microServiceInstance.getMeta();
    }
}
