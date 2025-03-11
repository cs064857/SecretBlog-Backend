package com.shijiawei.secretblog.common.utils;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JSON {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 反序列時，遇到未知的字段，不報錯。比如Json中有key1字段，Java的object中沒有key1字段，如果不設置成false，反序列時會報錯
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 數字類型統一轉成String， 因為前端Js不支持long類型的數據，前端讀取到long類型數據會丟失後三位數
        objectMapper.configure(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature(), true);
    }

    public static <T> T convert(Object obj, Class<T> returnType) {
        if (obj == null) {
            return null;
        }
        return parse(stringify(obj), returnType);
    }

    /**
     * Object to json
     *
     * @param obj
     * @return
     */
    public static String stringify(Object obj) {
        try {
            if (obj == null) {
                return null;
            } else if (obj instanceof String) {
                return obj.toString();
            }
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("對象轉化成json字符串出錯", e);
        }
    }

    /**
     * json to Object
     *
     * @param json
     * @param targetType
     * @param <T>
     * @return
     */
    public static <T> T parse(String json, Type targetType) {
        try {
            return objectMapper.readValue(json, TypeFactory.defaultInstance().constructType(targetType));
        } catch (IOException e) {
            throw new IllegalArgumentException("將JSON轉換為對象時發生錯誤:" + json, e);
        }
    }

    public static <T> T parse(String json, Class<T> targetType) {
        try {
            return objectMapper.readValue(json, TypeFactory.defaultInstance().constructType(targetType));
        } catch (IOException e) {
            throw new IllegalArgumentException("將JSON轉換為對象時發生錯誤:" + json, e);
        }
    }

    /**
     * json to Object
     */
    public static <T> T parse(String json, TypeReference<T> typeReference) {
        if (json != null && !json.isEmpty()) {
            try {
                return objectMapper.readValue(json, typeReference);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    public static Map<String, Object> parseToMap(String json) {
        return parse(json, HashMap.class);
    }
}
