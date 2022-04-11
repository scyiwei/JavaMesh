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

package com.huawei.sermant.stresstest.kafka;

import static com.huawei.sermant.stresstest.config.Constant.SHADOW;

import com.huawei.sermant.core.utils.StringUtils;
import com.huawei.sermant.stresstest.core.Reflection;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Consumer builder
 *
 * @author yiwei
 * @since 2021-10-25
 */
public class ConsumerBuilder {
    private ConsumerBuilder() {
    }

    /**
     * 创建测试consumer
     *
     * @param originalConsumer 原始的kafka consumer
     * @param <K> Key
     * @param <V> Value
     * @return 影子kafka consumer
     */
    @SuppressWarnings("unchecked")
    public static <K, V> KafkaConsumer<K, V> initTestConsumer(KafkaConsumer<K, V> originalConsumer) {
        Properties props = new Properties();
        updateMetadata(props, originalConsumer);
        updateNormal(props, originalConsumer);
        updateClient(props, originalConsumer);
        updateCoordinator(props, originalConsumer);
        updateFetcher(props, originalConsumer);
        updateInterceptors(props, originalConsumer);
        updateSubscriptions(props, originalConsumer);
        Object keyObject = Reflection.getDeclaredValue("keyDeserializer", originalConsumer).orElse(null);
        Deserializer<K> keyDeserializer = null;
        if (keyObject instanceof Deserializer) {
            keyDeserializer = (Deserializer<K>)keyObject;
        }
        Object valueObject = Reflection.getDeclaredValue("valueDeserializer", originalConsumer).orElse(null);
        Deserializer<V> valueDeserializer = null;
        if (keyObject instanceof Deserializer) {
            valueDeserializer = (Deserializer<V>)valueObject;
        }
        return new KafkaConsumer<>(props, keyDeserializer, valueDeserializer);
    }

    private static void updateNormal(Properties props, Object instance) {
        Reflection.getDeclaredValue("clientId", instance)
            .ifPresent(clientId -> props.put(ConsumerConfig.CLIENT_ID_CONFIG, SHADOW + clientId));
        Reflection.getDeclaredValue("groupId", instance).ifPresent(groupId -> {
            Object result;
            if (groupId instanceof Optional && ((Optional<?>)groupId).isPresent()) {
                result = ((Optional<?>)groupId).get();
            } else {
                result = groupId;
            }
            props.put(ConsumerConfig.GROUP_ID_CONFIG, SHADOW + result);
        });
        Reflection.getDeclaredValue("retryBackoffMs", instance)
            .ifPresent(retryBackoffMs -> props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs));
        Reflection.getDeclaredValue("requestTimeoutMs", instance).ifPresent(requestTimeoutMs -> {
            if (requestTimeoutMs instanceof Long) {
                props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, ((Long)requestTimeoutMs).intValue());
            }
        });
    }

    private static void updateSubscriptions(Properties props, Object instance) {
        Reflection.getDeclaredValue("subscriptions", instance)
            .flatMap(subscriptions -> Reflection.getDeclaredValue("defaultResetStrategy", subscriptions))
            .ifPresent(defaultResetStrategy -> props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                defaultResetStrategy.toString().toLowerCase(Locale.ROOT)));
    }

    private static void updateInterceptors(Properties props, Object instance) {
        Reflection.getDeclaredValue("interceptors", instance)
            .flatMap(interceptors -> Reflection.getDeclaredValue("interceptors", interceptors)).ifPresent(list -> {
                if (list instanceof List<?> && ((List<?>)list).size() > 0) {
                    List<Class<?>> classList = new ArrayList<>();
                    for (Object item : (List<?>)list) {
                        classList.add(item.getClass());
                    }
                    props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, classList.toString());
                }
            });
    }

    private static void updateMetadata(Properties props, Object instance) {
        Reflection.getDeclaredValue("metadata", instance).ifPresent(metadata -> {
            updateProps(props, ConsumerConfig.METADATA_MAX_AGE_CONFIG, metadata, "metadataExpireMs");
            oldUpdateBootServers(props, metadata);
            if (props.containsKey(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)) {
                return;
            }
            newUpdateBootServers(props, metadata);
        });
    }

    private static void oldUpdateBootServers(Properties props, Object value) {
        Reflection.getDeclaredValue("cluster", value).flatMap(cluster -> Reflection.getDeclaredValue("nodes", cluster))
            .ifPresent(nodes -> {
                if (nodes instanceof Collection) {
                    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                        getBootStrapServers((Collection<Node>)nodes));
                }
            });
    }

    private static void newUpdateBootServers(Properties props, Object value) {
        Reflection.getDeclaredValue("cache", value).flatMap(cache -> Reflection.getDeclaredValue("nodes", cache))
            .ifPresent(nodes -> {
                if (nodes instanceof Map) {
                    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                        getBootStrapServers(((Map<Integer, Node>)nodes).values()));
                }
            });
    }

    private static void updateClient(Properties props, Object instance) {
        Reflection.getDeclaredValue("client", instance).flatMap(client -> Reflection.getDeclaredValue("client", client))
            .ifPresent(kafkaClient -> {
                updateProps(props, ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, kafkaClient, "reconnectBackoffMs");
                updateProps(props, ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, kafkaClient, "reconnectBackoffMax");
                updateProps(props, ConsumerConfig.SEND_BUFFER_CONFIG, kafkaClient, "socketSendBuffer");
                updateProps(props, ConsumerConfig.RECEIVE_BUFFER_CONFIG, kafkaClient, "socketReceiveBuffer");
            });
    }

    private static void updateFetcher(Properties props, Object instance) {
        Reflection.getDeclaredValue("fetcher", instance).ifPresent(fetcher -> {
            updateProps(props, ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetcher, "minBytes");
            updateProps(props, ConsumerConfig.FETCH_MAX_BYTES_CONFIG, fetcher, "maxBytes");
            updateProps(props, ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetcher, "maxWaitMs");
            updateProps(props, ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, fetcher, "fetchSize");
            updateProps(props, ConsumerConfig.MAX_POLL_RECORDS_CONFIG, fetcher, "maxPollRecords");
            updateProps(props, ConsumerConfig.CHECK_CRCS_CONFIG, fetcher, "checkCrcs");
        });
    }

    private static void updateCoordinator(Properties props, Object instance) {
        Reflection.getDeclaredValue("coordinator", instance).ifPresent(coordinator -> {
            updateProps(props, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, coordinator, "autoCommitEnabled");
            updateProps(props, ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, coordinator, "autoCommitIntervalMs");
            updateProps(props, ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG, coordinator, "excludeInternalTopics");
            updateProps(props, ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, coordinator, "sessionTimeoutMs");
            if (StringUtils.isBlank(props.getProperty(ConsumerConfig.GROUP_ID_CONFIG))) {
                updateProps(props, ConsumerConfig.GROUP_ID_CONFIG, coordinator, "groupId");
            }
        });
    }

    private static void updateProps(Properties props, String configName, Object instance, String fieldName) {
        Reflection.getDeclaredValue(fieldName, instance).ifPresent(value -> props.put(configName, value));
    }

    private static String getBootStrapServers(Collection<Node> nodes) {
        StringBuilder builder = new StringBuilder();
        for (Node node : nodes) {
            builder.append(node.host()).append(":").append(node.port()).append(",");
        }
        return builder.substring(0, builder.length() - 1);
    }
}
