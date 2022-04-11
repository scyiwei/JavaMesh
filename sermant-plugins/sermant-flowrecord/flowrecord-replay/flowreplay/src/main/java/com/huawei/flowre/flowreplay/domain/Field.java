/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowre.flowreplay.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 回放结果中的字段的数据结构
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-12
 */
@Getter
@Setter
public class Field {
    /**
     * 接口下的字段名
     */
    private String name;

    /**
     * 字段的忽略情况
     */
    private boolean ignore;
}
