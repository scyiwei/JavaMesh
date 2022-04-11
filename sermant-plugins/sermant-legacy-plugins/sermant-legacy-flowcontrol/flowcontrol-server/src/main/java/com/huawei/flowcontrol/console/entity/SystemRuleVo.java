/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Based on com/alibaba/csp/sentinel/dashboard/entity/rule/SystemRuleEntity.java
 * from the Alibaba Sentinel project.
 */

package com.huawei.flowcontrol.console.entity;

import com.alibaba.csp.sentinel.slots.system.SystemRule;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemRuleVo extends BaseRule<SystemRule> {
    private Double highestSystemLoad;
    private Long avgRt;
    private Long maxThread;
    private Double qps;
    private Double highestCpuUsage;

    public static SystemRuleVo fromSystemRule(String app, String ip, Integer port, SystemRule rule) {
        SystemRuleVo entity = new SystemRuleVo();
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        entity.setHighestSystemLoad(rule.getHighestSystemLoad());
        entity.setHighestCpuUsage(rule.getHighestCpuUsage());
        entity.setAvgRt(rule.getAvgRt());
        entity.setMaxThread(rule.getMaxThread());
        entity.setQps(rule.getQps());
        return entity;
    }

    @Override
    public SystemRule toRule() {
        SystemRule rule = new SystemRule();
        rule.setHighestSystemLoad(highestSystemLoad);
        rule.setAvgRt(avgRt);
        rule.setMaxThread(maxThread);
        rule.setQps(qps);
        rule.setHighestCpuUsage(highestCpuUsage);
        return rule;
    }
}
