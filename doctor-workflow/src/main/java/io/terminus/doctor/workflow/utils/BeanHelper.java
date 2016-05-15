package io.terminus.doctor.workflow.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.dozer.DozerBeanMapper;

import java.util.Map;

/**
 * Desc: Bean处理的一些方法
 * 1. bean转Map
 * 2. 拷贝bean的属性
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/10
 */
public class BeanHelper {

    private static DozerBeanMapper dozer = new DozerBeanMapper();

    /**
     * bean转换成map对象
     *
     * @param source 源对象
     * @return
     */
    public static Map bean2Map(Object source) {
        return bean2Map(source, false);
    }

    /**
     * bean转换成map对象
     *
     * @param source     源对象
     * @param filterNull 是否过滤空value值, 默认不过滤
     * @return
     */
    public static Map bean2Map(Object source, boolean filterNull) {
        Map map = Maps.newHashMap();
        dozer.map(source, map);
        if (filterNull) {
            Map mapTemp = Maps.newHashMap();
            map.forEach((k, v) -> {
                if (v instanceof String ? !Strings.isNullOrEmpty((String) v) : v != null) {
                    mapTemp.put(k, v);
                }
            });
            map = mapTemp;
        }
        return map;
    }

    /**
     * 拷贝bean的属性
     * @param dest      拷贝的目标对象
     * @param origin    拷贝的源对象
     */
    public static void copy(Object dest, Object origin) {
        dozer.map(origin, dest);
    }

}
