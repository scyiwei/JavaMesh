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

package com.huawei.hercules.controller.websocket;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述：WebSocket控制相关接口
 *
 * 
 * @since 2021-11-03
 */
@RestController
@RequestMapping("/api")
public class WebSocketController {
    /**
     * 日志接口
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private List<WebSocketEventHandler> webSocketEventHandlers;

    /**
     * 通知websocket事件处理接口处理task事件
     *
     * @param message 事件消息
     * @return success成功
     */
    @RequestMapping(value = "/task/ws", method = RequestMethod.POST)
    public Map<String, Object> notifyWebSocket(@RequestBody Map<String, Object> message) {
        String messageJson = JSON.toJSONString(message);
        LOGGER.info("Receive message:{}", message);
        for (WebSocketEventHandler webSocketEventHandler : webSocketEventHandlers) {
            webSocketEventHandler.handleEvent(messageJson);
        }
        message.put("result", "success");
        return message;
    }
}
