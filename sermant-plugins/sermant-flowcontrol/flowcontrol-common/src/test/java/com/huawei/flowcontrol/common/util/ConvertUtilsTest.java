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

package com.huawei.flowcontrol.common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * 转换工具类测试
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class ConvertUtilsTest {
    /**
     * 测试构建api路径
     */
    @Test
    public void testBuildPath() {
        Assert.assertEquals(
            ConvertUtils.buildApiPath("com.test.interface", "1.0.0", "methodName"),
            "com.test.interface:1.0.0.methodName");
        Assert.assertEquals(
            ConvertUtils.buildApiPath("com.test.interface", "", "methodName"),
            "com.test.interface.methodName");
    }
}
