package io.terminus.doctor.event.helper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Created by sunbo@terminus.io on 2017/9/8.
 */
public class BeanCopyUtil {


    public static void copy(Object source, Object target) {
        if (null == target || null == source)
            return;

        for (Field field : target.getClass().getDeclaredFields()) {
            String fieldName;
            if (field.isAnnotationPresent(CopySourceName.class)) {
                fieldName = field.getAnnotation(CopySourceName.class).value();
            } else
                fieldName = field.getName();

            try {
                Field sourceField = source.getClass().getDeclaredField(fieldName);
                Object value = sourceField.get(source);
                if (null == value && field.isAnnotationPresent(CopyIngoreWhenSourceNull.class))
                    continue;
                field.setAccessible(true);
                field.set(target, value);
            } catch (NoSuchFieldException e) {

            } catch (IllegalAccessException e) {

            }
        }
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CopySourceName {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CopyIngoreWhenSourceNull {

    }
}
