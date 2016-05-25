package io.terminus.doctor.workflow.utils;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * Desc: String类型帮助类
 * 1. blank判断
 * 2. empty判断
 * 3. 首字母大小写
 * 4. 转义特殊字符
 * 5. 编译判断表达式
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/15
 */
public class StringHelper {

    /**
     * 判断是否为空白类型, 如果是返回true, 否则返回false
     *
     * @param cs
     * @return
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 判断是否为空类型, 如果是返回true, 否则返回false
     *
     * @param cs
     * @return
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * 首字母小写
     *
     * @param str
     * @return
     */
    public static String uncapitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        char firstChar = str.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            return str;
        }
        return new StringBuilder(strLen)
                .append(Character.toLowerCase(firstChar))
                .append(str.substring(1))
                .toString();
    }

    /**
     * 首字母大写
     *
     * @param str
     * @return
     */
    public static String capitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        char firstChar = str.charAt(0);
        if (Character.isTitleCase(firstChar)) {
            return str;
        }
        return new StringBuilder(strLen)
                .append(Character.toTitleCase(firstChar))
                .append(str.substring(1))
                .toString();
    }

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     *
     * @param message
     * @return
     */
    public static String escape(String message) {
        if (StringHelper.isNotBlank(message)) {
            String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
            for (String key : fbsArr) {
                if (message.contains(key)) {
                    message = message.replace(key, "\\" + key);
                }
            }
        }
        return message;
    }

    /**
     * 编译表达式, 并返回布尔类型
     * @param expression        表达式
     * @param vars     表达式参数
     * @return
     */
    public static boolean parseExpression(String expression, Map<String, Object> vars) {
        return parseExpression(boolean.class, expression, vars);
    }

    /** 编译表达式, 并返回指定类型
     * @param clazz             指定类型
     * @param expression        表达式
     * @param vars     表达式参数
     * @return
     */
    public static <T> T parseExpression(Class<T> clazz, String expression, Map<String, Object> vars) {
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        if (vars != null && vars.size() > 0) {
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                String key = entry.getKey();
                // 1. 替换表达式的所有 ${} 或者 #{} 占位符
                String regex = "(\\$\\{" + key + "\\}(?!.*['\"])|(?<!['\"].{0,1000000})\\$\\{" + key + "\\})";
                expression = expression.replaceAll(regex, "#" + key);
                regex = "(#\\{" + key + "\\}(?!.*['\"])|(?<!['\"].{0,1000000})#\\{" + key + "\\})";
                expression = expression.replaceAll(regex, "#" + key);
                // 存入context中
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }
        // 2. 替换 equals 关键字
        String regex = "(equals(?!.*['\"])|(?<!['\"].{0,1000000})equals)";
        expression = expression.replaceAll(regex, "matches");
        return parser.parseExpression(expression).getValue(context, clazz);
    }

}
