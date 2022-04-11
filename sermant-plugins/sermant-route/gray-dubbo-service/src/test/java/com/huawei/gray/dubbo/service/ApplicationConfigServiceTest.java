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

package com.huawei.gray.dubbo.service;

import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.route.common.gray.config.GrayConfig;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;

import com.alibaba.dubbo.config.ApplicationConfig;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试ApplicationConfigServiceImpl
 *
 * @author provenceee
 * @since 2022-03-21
 */
public class ApplicationConfigServiceTest {
    private static final String BAR = "bar";

    private static final String FOO = "foo";

    private final ApplicationConfig alibabaConfig;

    private final org.apache.dubbo.config.ApplicationConfig apacheConfig;

    private final ApplicationConfigService service;

    private final GrayConfig config;

    /**
     * 构造方法
     */
    public ApplicationConfigServiceTest() throws IllegalAccessException, NoSuchFieldException {
        alibabaConfig = new ApplicationConfig();
        apacheConfig = new org.apache.dubbo.config.ApplicationConfig();
        service = new ApplicationConfigServiceImpl();
        config = new GrayConfig();
        Field field = service.getClass().getDeclaredField("grayConfig");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(service, config);
    }

    /**
     * 测试alibaba ApplicationConfig
     *
     * @see com.alibaba.dubbo.config.ApplicationConfig
     */
    @Test
    public void testAlibaba() {
        // 清空缓存
        DubboCache.INSTANCE.setAppName(null);

        // 应用名为空
        service.getName(alibabaConfig);
        Assert.assertNull(DubboCache.INSTANCE.getAppName());

        // 应用名不为null，parameters为null
        alibabaConfig.setName(BAR);
        Assert.assertNull(alibabaConfig.getParameters());
        service.getName(alibabaConfig);
        Assert.assertEquals(BAR, DubboCache.INSTANCE.getAppName());
        Map<String, String> parameters = alibabaConfig.getParameters();
        testParameters(parameters);
        Assert.assertEquals(2, parameters.size());
        testGrayConfiguration();

        // 应用名不为null，parameters不为null
        Map<String, String> map = new HashMap<>();
        map.put(BAR, FOO);
        alibabaConfig.setParameters(map);
        service.getName(alibabaConfig);
        Assert.assertEquals(BAR, DubboCache.INSTANCE.getAppName());
        parameters = alibabaConfig.getParameters();
        testParameters(parameters);
        Assert.assertEquals(FOO, parameters.get(BAR));
        Assert.assertEquals(3, parameters.size());
        Assert.assertEquals(map, parameters);
        testGrayConfiguration();
    }

    /**
     * 测试apache ApplicationConfig
     *
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    @Test
    public void testApache() {
        // 清空缓存
        DubboCache.INSTANCE.setAppName(null);

        // 应用名为空
        service.getName(apacheConfig);
        Assert.assertNull(DubboCache.INSTANCE.getAppName());

        // 应用名不为null，parameters为null
        apacheConfig.setName(FOO);
        Assert.assertNull(apacheConfig.getParameters());
        service.getName(apacheConfig);
        Assert.assertEquals(FOO, DubboCache.INSTANCE.getAppName());
        Map<String, String> parameters = apacheConfig.getParameters();
        testParameters(parameters);
        Assert.assertEquals(2, parameters.size());
        testGrayConfiguration();

        // 应用名不为null，parameters不为null
        Map<String, String> map = new HashMap<>();
        map.put(FOO, BAR);
        apacheConfig.setParameters(map);
        service.getName(apacheConfig);
        Assert.assertEquals(FOO, DubboCache.INSTANCE.getAppName());
        parameters = apacheConfig.getParameters();
        testParameters(parameters);
        Assert.assertEquals(BAR, parameters.get(FOO));
        Assert.assertEquals(3, parameters.size());
        Assert.assertEquals(map, parameters);
        testGrayConfiguration();
    }

    private void testParameters(Map<String, String> parameters) {
        Assert.assertNotNull(parameters);
        Assert.assertEquals(config.getGrayVersion(), parameters.get(GrayConstant.GRAY_VERSION_KEY));
        Assert.assertEquals(config.getLdc(), parameters.get(GrayConstant.GRAY_LDC_KEY));
    }

    private void testGrayConfiguration() {
        GrayConfiguration grayConfiguration = LabelCache.getLabel(GrayConstant.GRAY_LABEL_CACHE_NAME);
        CurrentTag currentTag = grayConfiguration.getCurrentTag();
        Assert.assertNotNull(currentTag);
        Assert.assertEquals(config.getGrayVersion(), currentTag.getVersion());
        Assert.assertEquals(config.getLdc(), currentTag.getLdc());
    }
}