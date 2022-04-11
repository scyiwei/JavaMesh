/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights
 * reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package com.huawei.registry.service.client;

import com.huawei.registry.config.ConfigConstants;
import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.utils.HostUtils;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.common.PluginConstant;
import com.huawei.sermant.core.plugin.common.PluginSchemaValidator;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.utils.JarFileUtils;
import com.huawei.sermant.core.utils.StringUtils;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.http.client.common.HttpConfiguration;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceRegistrationEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery.SubscriptionKey;
import org.apache.servicecomb.service.center.client.ServiceCenterOperation;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.model.DataCenterInfo;
import org.apache.servicecomb.service.center.client.model.Framework;
import org.apache.servicecomb.service.center.client.model.HealthCheck;
import org.apache.servicecomb.service.center.client.model.HealthCheckMode;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroserviceStatus;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.apache.servicecomb.service.center.client.model.ServiceCenterConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 基于注册任务注册服务实例
 *
 * @author zhouss
 * @since 2022-02-25
 */
public class ScClient {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 事件栈
     */
    private static final EventBus EVENT_BUS = new EventBus();

    /**
     * http url前缀
     */
    private static final String HTTP_URL_PREFIX = "http://";

    /**
     * https url前缀
     */
    private static final String HTTPS_URL_PREFIX = "https://";

    /**
     * 注册版本号
     */
    private static final String REG_VERSION_KEY = "reg.version";

    private static final int FLAG = -1;

    private ServiceCenterConfiguration serviceCenterConfiguration;

    private ServiceCenterClient serviceCenterClient;

    private RegisterConfig registerConfig;

    private Microservice microservice;

    private MicroserviceInstance microserviceInstance;

    private ServiceCenterDiscovery serviceCenterDiscovery;

    /**
     * 初始化
     */
    public void init() {
        registerConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        initScClient();
        initServiceCenterConfiguration();
    }

    /**
     * 服务实例注册
     */
    public void register() {
        startServiceCenterRegistration();
    }

    /**
     * 查询所有实例
     *
     * @param serviceName 服务名
     * @return 实例列表
     */
    public List<MicroserviceInstance> queryInstancesByServiceId(String serviceName) {
        List<MicroserviceInstance> instances;
        if (registerConfig.isAllowCrossApp()) {
            instances = queryAllAppInstances(serviceName);
        } else {
            instances = getInstanceByCurApp(serviceName);
        }
        if (instances == null) {
            return Collections.emptyList();
        }
        if (registerConfig.isEnableZoneAware()) {
            return zoneAwareFilter(instances);
        }
        return instances;
    }

    private List<MicroserviceInstance> queryAllAppInstances(String serviceName) {
        final MicroservicesResponse response = serviceCenterClient.getMicroserviceList();
        if (response == null || response.getServices() == null) {
            return Collections.emptyList();
        }
        final List<Microservice> allServices = response.getServices();
        final List<String> allServiceIds =
            allServices.stream().filter(service -> StringUtils.equals(service.getServiceName(), serviceName))
                .map(Microservice::getServiceId).distinct().collect(Collectors.toList());
        List<MicroserviceInstance> microserviceInstances = new ArrayList<>();
        allServiceIds.forEach(serviceId -> {
            final MicroserviceInstancesResponse instanceResponse =
                serviceCenterClient.getMicroserviceInstanceList(serviceId);
            if (instanceResponse != null && instanceResponse.getInstances() != null) {
                microserviceInstances.addAll(instanceResponse.getInstances());
            }
        });
        return microserviceInstances;
    }

    private List<MicroserviceInstance> getInstanceByCurApp(String serviceName) {
        final SubscriptionKey subscriptionKey = buildSubscriptionKey(serviceName);
        serviceCenterDiscovery.registerIfNotPresent(subscriptionKey);
        return serviceCenterDiscovery.getInstanceCache(subscriptionKey);
    }

    private List<MicroserviceInstance> zoneAwareFilter(List<MicroserviceInstance> instances) {
        List<MicroserviceInstance> regionAndAzMatchList = new ArrayList<>();
        List<MicroserviceInstance> regionMatchList = new ArrayList<>();
        instances.forEach(instance -> {
            if (regionAndAzMatch(microserviceInstance, instance)) {
                // 匹配region与zone
                regionAndAzMatchList.add(instance);
            } else if (regionMatch(microserviceInstance, instance)) {
                // 仅匹配region
                regionMatchList.add(instance);
            }
        });

        // 优先使用匹配区域的实例
        if (!regionAndAzMatchList.isEmpty()) {
            return regionAndAzMatchList;
        }
        if (!regionMatchList.isEmpty()) {
            return regionMatchList;
        }
        return instances;
    }

    private boolean regionAndAzMatch(MicroserviceInstance myself, MicroserviceInstance target) {
        if (myself.getDataCenterInfo() != null && target.getDataCenterInfo() != null) {
            return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion())
                && myself.getDataCenterInfo().getAvailableZone().equals(target.getDataCenterInfo().getAvailableZone());
        }
        return false;
    }

    private boolean regionMatch(MicroserviceInstance myself, MicroserviceInstance target) {
        if (target.getDataCenterInfo() != null) {
            return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion());
        }
        return false;
    }

    private SubscriptionKey buildSubscriptionKey(String serviceId) {
        int index = serviceId.indexOf(ConfigConstants.APP_SERVICE_SEPARATOR);
        if (index == FLAG) {
            return new SubscriptionKey(microservice.getAppId(), serviceId);
        }
        return new SubscriptionKey(serviceId.substring(0, index), serviceId.substring(index + 1));
    }

    /**
     * 获取内部client
     *
     * @return ServiceCenterOperation
     */
    public ServiceCenterOperation getRawClient() {
        return serviceCenterClient;
    }

    /**
     * 心跳事件
     *
     * @param event 心跳事件
     */
    @Subscribe
    public void onHeartBeatEvent(HeartBeatEvent event) {
        if (event.isSuccess()) {
            LOGGER.fine("Service center post heartbeat success!");
        }
    }

    /**
     * 注册事件
     *
     * @param event 注册事件
     */
    @Subscribe
    public void onMicroserviceRegistrationEvent(MicroserviceRegistrationEvent event) {
        if (event.isSuccess()) {
            if (serviceCenterDiscovery == null) {
                serviceCenterDiscovery = new ServiceCenterDiscovery(serviceCenterClient, EVENT_BUS);
                serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
                serviceCenterDiscovery.setPollInterval(registerConfig.getPullInterval());
                serviceCenterDiscovery.startDiscovery();
            } else {
                serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
            }
        }
    }

    private void startServiceCenterRegistration() {
        if (serviceCenterClient == null) {
            return;
        }
        ServiceCenterRegistration serviceCenterRegistration =
            new ServiceCenterRegistration(serviceCenterClient, serviceCenterConfiguration, EVENT_BUS);
        EVENT_BUS.register(this);
        serviceCenterRegistration.setMicroservice(buildMicroService());
        buildMicroServiceInstance();
        serviceCenterRegistration.setHeartBeatInterval(microserviceInstance.getHealthCheck().getInterval());
        serviceCenterRegistration.setMicroserviceInstance(microserviceInstance);
        serviceCenterRegistration.startRegistration();
    }

    private void initServiceCenterConfiguration() {
        serviceCenterConfiguration = new ServiceCenterConfiguration();
        serviceCenterConfiguration.setIgnoreSwaggerDifferent(false);
    }

    private void initScClient() {
        serviceCenterClient = new ServiceCenterClient(createAddressManager(registerConfig.getProject(), getScUrls()),
            createSslProperties(registerConfig.isSslEnabled()), signRequest -> Collections.emptyMap(), "default",
            Collections.emptyMap());
    }

    private List<String> buildEndpoints() {
        return Collections.singletonList(String.format(Locale.ENGLISH, "rest://%s:%d", HostUtils.getMachineIp(),
            RegisterContext.INSTANCE.getClientInfo().getPort()));
    }

    private void buildMicroServiceInstance() {
        microserviceInstance = new MicroserviceInstance();
        microserviceInstance.setStatus(MicroserviceInstanceStatus.UP);
        microserviceInstance.setHostName(HostUtils.getHostName());
        microserviceInstance.setEndpoints(buildEndpoints());
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setMode(HealthCheckMode.pull);
        healthCheck.setInterval(registerConfig.getHeartbeatInterval());
        healthCheck.setTimes(registerConfig.getHeartbeatRetryTimes());
        microserviceInstance.setHealthCheck(healthCheck);
        final String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        microserviceInstance.setTimestamp(currentTimeMillis);
        microserviceInstance.setModTimestamp(currentTimeMillis);
        Map<String, String> meta = new HashMap<>(RegisterContext.INSTANCE.getClientInfo().getMeta());
        meta.put(REG_VERSION_KEY, registerConfig.getVersion());
        microserviceInstance.setProperties(meta);
        if (registerConfig.isEnableZoneAware()) {
            // 设置数据中心信息
            fillDataCenterInfo();
        }
    }

    private void fillDataCenterInfo() {
        final DataCenterInfo dataCenterInfo = new DataCenterInfo();
        dataCenterInfo.setName(registerConfig.getDataCenterName());
        dataCenterInfo.setAvailableZone(registerConfig.getDataCenterAvailableZone());
        dataCenterInfo.setRegion(registerConfig.getDataCenterRegion());
        microserviceInstance.setDataCenterInfo(dataCenterInfo);
    }

    private String getVersion() {
        try (JarFile jarFile = new JarFile(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())) {
            final Object pluginName = JarFileUtils.getManifestAttr(jarFile, PluginConstant.PLUGIN_NAME_KEY);
            if (pluginName instanceof String) {
                return PluginSchemaValidator.getPluginVersionMap().get(pluginName);
            }
        } catch (IOException e) {
            LOGGER.warning("Cannot not get the version.");
        }
        return "";
    }

    private Microservice buildMicroService() {
        microservice = new Microservice();
        if (registerConfig.isAllowCrossApp()) {
            microservice.setAlias(registerConfig.getApplication() + ConfigConstants.APP_SERVICE_SEPARATOR
                + RegisterContext.INSTANCE.getClientInfo().getServiceId());
        }
        microservice.setAppId(registerConfig.getApplication());
        microservice.setEnvironment(registerConfig.getEnvironment());

        // agent相关信息
        final Framework framework = new Framework();
        framework.setName(registerConfig.getFramework());
        framework.setVersion(getVersion());
        microservice.setFramework(framework);
        microservice.setVersion(registerConfig.getVersion());
        microservice.setServiceName(RegisterContext.INSTANCE.getClientInfo().getServiceId());
        microservice.setStatus(MicroserviceStatus.UP);
        return microservice;
    }

    private List<String> getScUrls() {
        final List<String> urlList = registerConfig.getAddressList();
        if (urlList == null || urlList.isEmpty()) {
            throw new IllegalArgumentException("Kie url must not be empty!");
        }
        Iterator<String> it = urlList.iterator();
        while (it.hasNext()) {
            String url = it.next();
            if (!isUrlValid(url)) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Invalid url : %s", url));
                it.remove();
            }
        }
        return urlList;
    }

    private boolean isUrlValid(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        final String trimUrl = url.trim();
        return trimUrl.startsWith(HTTP_URL_PREFIX) || trimUrl.startsWith(HTTPS_URL_PREFIX);
    }

    private HttpConfiguration.SSLProperties createSslProperties(boolean isEnabled) {
        final HttpConfiguration.SSLProperties sslProperties = new HttpConfiguration.SSLProperties();
        sslProperties.setSslOption(SSLOption.DEFAULT_OPTION);
        sslProperties.setSslCustom(SSLCustom.defaultSSLCustom());
        sslProperties.setEnabled(isEnabled);
        return sslProperties;
    }

    private AddressManager createAddressManager(String project, List<String> scUrls) {
        return new AddressManager(project, scUrls);
    }
}
