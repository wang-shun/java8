/*
 *
 *  * Copyright (c) 2014 杭州端点网络科技有限公司
 *
 */

package io.terminus.doctor.common.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;
import static com.google.common.base.Preconditions.*;
/**
 * Author: haolin
 * On: 12/1/14
 */
public final class Params {

    /**
     * 过滤Map中的NULL或""值
     */
    public static Map<String, Object> filterNullOrEmpty(Map<String, Object> criteria) {
        return Maps.filterEntries(criteria, entry -> {
            Object v = entry.getValue();
            if (v instanceof String) {
                return !Strings.isNullOrEmpty((String) v);
            }
            return v != null;
        });
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

}
