/*
 * Copyright (c) 2015 杭州端点网络科技有限公司
 */

package io.terminus.doctor.web.core.component;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 14-6-7
 */
@Slf4j
public class Fields {
    public static FieldGetter createGetter(Class clazz, String fieldName) {
        if (Map.class.isAssignableFrom(clazz)) {
            return new MapFieldGetter(fieldName);
        } else {
            return new ObjectFieldGetter(clazz, fieldName);
        }
    }

    public static interface FieldGetter {
        public Object get(Object o);
    }

    public static class MapFieldGetter implements FieldGetter {
        private Object key;

        public MapFieldGetter(Object key) {
            this.key = key;
        }

        @Override
        public Object get(Object o) {
            return ((Map) o).get(key);
        }
    }

    public static class ObjectFieldGetter implements FieldGetter {
        private Method method;

        public ObjectFieldGetter(Class clazz, String fieldName) {
            String methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            try {
                //noinspection unchecked
                method = clazz.getMethod(methodName);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("no such method found for field " + fieldName + " in class " + clazz.getName());
            }
        }

        @Override
        public Object get(Object o) {
            try {
                return method.invoke(o);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.warn("error when get field,cause:{}", Throwables.getStackTraceAsString(e)); // ignore this
            }
            return null;
        }
    }
}
