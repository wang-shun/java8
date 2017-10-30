package io.terminus.doctor.web.admin.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/9/14.
 */
@Slf4j
public class BeanCopyUtil {


    public static void copy(Object source, Object target) {
        for (Field field : target.getClass().getDeclaredFields()) {

            field.setAccessible(true);
            if (Map.class.isAssignableFrom(source.getClass())) {
                Map s = (Map) source;
                if (s.containsKey(field.getName())) {
                    try {
                        System.out.println(field.getName());
                        System.out.println(s.get(field.getName()));
                        if (field.getType() == Long.class || field.getType() == long.class)
                            field.set(target, Long.parseLong(s.get(field.getName()).toString()));
                        else if (field.getType() == Integer.class || field.getType() == int.class)
                            field.set(target, Integer.parseInt(s.get(field.getName()).toString()));
                        else
                            field.set(target, s.get(field.getName()));
                    } catch (IllegalAccessException e) {

                        e.printStackTrace();
                    }
                }

            } else {
                try {
                    Field s = source.getClass().getDeclaredField(field.getName());
                    field.set(target, s.get(source));
                } catch (NoSuchFieldException e) {

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
