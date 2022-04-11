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

package com.huawei.sermant.stresstest.redis;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.utils.StringUtils;
import com.huawei.sermant.stresstest.config.ConfigFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Redis 拦截器，修改key值，增加shadow前缀。
 *
 * @author yiwei
 * @since 2021-11-01
 */
@SuppressWarnings({"JavadocStyle", "JavadocMethod", "RegexpMultiline","MagicNumber"})
public class RedisUtils {
    private static final String PATTERN = "(redis://)*([A-Za-z0-9.]+:*\\d*?$)";
    private static final String KEY = ConfigFactory.getConfig().getTestRedisPrefix();
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String ADDRESS = "address";
    private static final String SLAVE = "slave";

    private RedisUtils() {
    }

    public static String getJedisAddress() {
        return getRedisAddress(ADDRESS, 2).get(0);
    }

    /**
     *
     * @return 首个redis address的值
     */
    public static String getMasterAddress() {
        return getRedisAddress(ADDRESS, 0).get(0);
    }

    /**
     *
     * @return redis address的所有值。
     */
    public static List<String> getNodeAddress() {
        return getRedisAddress(ADDRESS, 0);
    }

    /**
     *
     * @return slave 地址
     */
    public static Set<String> getSalveRedisAddress() {
        return new HashSet<>(getRedisAddress(SLAVE, 0));
    }

    private static List<String> getRedisAddress(String name, int index) {
        Object addresses = ConfigFactory.getConfig().getShadowRedis().get(name);
        Pattern pattern = Pattern.compile(PATTERN);
        if (addresses instanceof List<?> && ((List<?>)addresses).size() > 0) {
            List<String> list = new ArrayList<>();
            for (Object address : (List<?>)addresses) {
                Matcher matcher = pattern.matcher(address.toString());
                if (matcher.find()) {
                    list.add(matcher.group(index));
                }
            }
            return list;
        }
        return addresses == null ? Collections.emptyList() : Collections.singletonList(addresses.toString());
    }

    /**
     * 修改单个key或者collection， 加上影子前缀
     * @param key 要修改的key
     * @return 修改后的key
     */
    @SuppressWarnings("unchecked")
    public static <T> T modifyKey(final T key) {
        if (key instanceof Iterable) {
            return (T)modifyIterable((Iterable<?>)key);
        }
        if (key != null && key.getClass().isArray()) {
            return (T)modifyArray((Object[])key);
        }
        return modifySingleKey(key);
    }

    /**
     * 修改单个key， 加上影子前缀
     * @param key 要修改的key
     * @return 修改后的key
     */
    @SuppressWarnings("unchecked")
    public static <T> T modifySingleKey(final T key) {
        if (key instanceof String) {
            return (T)modifyString((String)key);
        }
        if (key instanceof byte[]) {
            return (T)modifyBytes((byte[])key);
        }
        return key;
    }

    /**
     * 修改bytes， 加上影子前缀
     * @param key 要修改的key
     * @return 修改后的key
     */
    public static byte[] modifyBytes(final byte[] key) {
        try {
            String str = new String(key, DEFAULT_CHARSET);
            return modifyKey(str).getBytes(DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            LOGGER.severe("Don't support UTF-8.");
        }
        return key;
    }

    /**
     * 修改String， 加上影子前缀
     * @param key 要修改的key
     * @return 修改后的key
     */
    public static String modifyString(final String key) {
        return StringUtils.isExist(key) && !key.startsWith(KEY) ? KEY + key : key;
    }

    private static <T> Iterable<T> modifyIterable(final Iterable<T> keys) {
        List<T> results = new ArrayList<>();
        for (T key : keys) {
            results.add(modifySingleKey(key));
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] modifyArray(final T[] keys) {
        int length = keys.length;
        T[] results = (T[])new Object[length];
        for (int i = 0; i < length; i++) {
            results[i] = modifySingleKey(keys[i]);
        }
        return results;
    }
}
