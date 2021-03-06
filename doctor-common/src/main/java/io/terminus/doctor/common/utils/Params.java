/*
 *
 *  * Copyright (c) 2014 杭州端点网络科技有限公司
 *
 */

package io.terminus.doctor.common.utils;

import com.google.common.base.Strings;
import io.terminus.doctor.common.constants.JacksonType;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author: haolin
 * On: 12/1/14
 */
public final class Params {

    private static final JsonMapperUtil JSON = JsonMapperUtil.nonEmptyMapper();

    /**
     * 过滤Map中的NULL或""值
     */
    public static Map<String, Object> filterNullOrEmpty(Map<String, Object> criteria) {
        return criteria.entrySet().stream()
                .filter(entry -> {
                    Object v = entry.getValue();
                    if (v instanceof String) {
                        return !Strings.isNullOrEmpty(String.valueOf(v));
                    }
                    return v != null;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static @Nullable String trimToNull(@Nullable String str) {
        return str != null ? Strings.emptyToNull(str.trim()) : null;
    }

    public static @Nullable String trimToNull(@Nullable Object obj) {
        return obj != null ? trimToNull(obj.toString()) : null;
    }

    public static <T> T get(Map<String, ?> source, String key){
        return (T)source.get(key);
    }

    public static <T> T getWithOutNull(Map<String, ?> source, String key){
        checkNotNull(source.get(key));
        return (T)source.get(key);
    }

    public static <T> T getNullDefualt(Map<String, ?> source, String key, T defaultValue){
        if(source.get(key) == null){
            return defaultValue;
        }
        return (T)source.get(key);
    }

    public static <K, V> V getNullDefault(Map<K, V> source, K key, V defaultValue){
        if(source.get(key) == null){
            return defaultValue;
        }
        return source.get(key);
    }

    public static <T, F> T getWithConvert(Map<String, F> source, String key, Function<F, T> convert){
        checkNotNull(source.get(key));
        return convert.apply(source.get(key));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> objToMap(Object obj) {
        return JSON.getMapper()
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .convertValue(obj, JacksonType.MAP_OF_OBJECT);
    }

    public static boolean containsNotEmpty(Map<String, ?> params, String key) {
        return params.containsKey(key) && params.get(key) != null && StringUtils.hasText(params.get(key).toString());
    }
}
