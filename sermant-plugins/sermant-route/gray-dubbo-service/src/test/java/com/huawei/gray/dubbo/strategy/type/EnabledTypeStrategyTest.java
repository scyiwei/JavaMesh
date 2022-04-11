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

package com.huawei.gray.dubbo.strategy.type;

import com.huawei.gray.dubbo.strategy.TypeStrategy;
import com.huawei.sermant.core.common.CommonConstant;
import com.huawei.sermant.core.common.LoggerFactory;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * enabled匹配策略测试
 *
 * @author provenceee
 * @since 2021-12-01
 */
public class EnabledTypeStrategyTest {
    /**
     * 初始化
     */
    @BeforeClass
    public static void init() {
        Map<String, Object> map = new HashMap<>();
        map.put(CommonConstant.LOG_SETTING_FILE_KEY,
            EnabledTypeStrategyTest.class.getResource("/logback-test.xml").getPath());
        LoggerFactory.init(map);
    }

    /**
     * 测试enabled策略
     */
    @Test
    public void testValue() {
        TypeStrategy strategy = new EnabledTypeStrategy();
        Entity entity = new Entity();
        entity.setEnabled(true);

        // 正常情况
        Assert.assertEquals(Boolean.TRUE.toString(), strategy.getValue(entity, ".isEnabled()").orElse(null));

        // 测试找不到方法
        Assert.assertEquals(Boolean.FALSE.toString(), strategy.getValue(entity, ".foo()").orElse(null));

        // 测试null
        Assert.assertNotEquals(Boolean.TRUE.toString(), strategy.getValue(new Entity(), ".isEnabled()").orElse(null));
    }
}