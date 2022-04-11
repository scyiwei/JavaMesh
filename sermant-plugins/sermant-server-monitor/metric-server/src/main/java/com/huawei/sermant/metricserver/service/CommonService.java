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

package com.huawei.sermant.metricserver.service;

import com.huawei.sermant.metricserver.dao.influxdb.InfluxDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * 通用服务
 */
@Service
public class CommonService {

    private final InfluxDao influxDao;

    @Autowired
    public CommonService(InfluxDao influxDao) {
        this.influxDao = influxDao;
    }

    /**
     * 删除指定时间段的数据，谨慎使用
     *
     * @param start 开始时间
     * @param stop  结束时间
     */
    public void delete(OffsetDateTime start, OffsetDateTime stop) {
        influxDao.delete(start, stop);
    }
}
