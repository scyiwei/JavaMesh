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

package com.huawei.sermant.metricserver.dao.influxdb.entity;

import com.influxdb.annotations.Column;
import lombok.Data;

import java.time.Instant;

/**
 * 通用Influxdb持久化实体
 */
@Data
public abstract class CommonMetricInfluxEntity {
    @Column(timestamp = true)
    private Instant time;

    @Column(tag = true, name = "service")
    private String service;

    @Column(tag = true, name = "service_instance")
    private String serviceInstance;
}
