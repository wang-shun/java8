package io.terminus.doctor.workflow.utils;

import io.terminus.doctor.workflow.core.WorkFlowException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Desc: 断言校验类
 *      1. null 异常
 *      2. 空白blank 异常
 *      3. not equals 异常
 *      4. Map包含key 异常
 *      5. 统一异常抛出方法
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/26
 */
public class AssertHelper {

    /**
     * 校验一个对象是否为null,如果为空则抛出异常信息
     * @param t         校验对象
     * @param message   异常信息
     * @param args      模板填充数据
     */
    public static <T> T isNull(T t, String message, Object ... args) {
        if(t == null) {
            throwException(getExceptionMessage(message,
                    "[Assert Error] => the argument can not be null"), args);
        }
        return t;
    }
    public static <T> T isNull(T t) {
        return isNull(t, null);
    }

    /**
     * 校验一个对象是否为 blank 或 null,如果是则抛出异常信息
     * @param object    校验对象
     * @param message   异常信息
     * @param args      模板填充数据
     */
    public static String isBlank(String object, String message, Object ... args) {
        if(StringUtils.isBlank(object)) {
            throwException(getExceptionMessage(message,
                    "[Assert Error] => the argument can not be blank or null"), args);
        }
        return object;
    }
    public static String isBlank(String object) {
        return isBlank(object, null);
    }


    /**
     * 校验两个对象是否相等, 如果不相等则抛出异常
     * @param src       源对象
     * @param tar       目标对象
     * @param message   异常信息
     * @param args      模板填充数据
     */
    public static <T> void notEquals(T src, T tar, String message, Object ... args) {
        if(!src.equals(tar)) {
            throwException(getExceptionMessage(message,
                    "[Assert Error] => the source should be equals to target, source is {}, target is {}"),
                    src, tar,
                    args
            );
        }
    }

    /**
     * 检验map是否包含了key, 如果包含了, 则抛出异常
     * @param map       map对象
     * @param key       key
     * @param message   异常信息
     * @param args      模板填充数据
     */
    public static void mapContainsKey(Map map, Object key, String message, Object ... args) {
        if(map.containsKey(key)) {
            throwException(getExceptionMessage(message,
                    "[Assert Error] => the map should not contains key, the key is: {}"), key, args);
        }
    }

    /**
     * * 公共抛出异常类, 处理带模板的异常信息
     * @param message   异常信息, 模板填充是 `{}`
     * @param args      填充参数
     */
    public static void throwException(String message, Object ... args) {
        Pattern p = Pattern.compile("\\{\\}");
        Matcher matcher = p.matcher(message);
        for (int i = 0; args != null && i < args.length && matcher.find(); i++) {
            message = matcher.replaceFirst(args[i].toString());
            matcher = matcher.reset(message);
        }
        throw new WorkFlowException(message);
    }

    /**
     * 获取存在异常信息
     * @param ifMessage     如果存在的异常信息
     * @param willMessage   代替的异常信息
     */
    private static String getExceptionMessage(String ifMessage, String willMessage) {
        if(StringUtils.isBlank(ifMessage)) {
            ifMessage = willMessage;
        }
        return ifMessage;
    }
}
