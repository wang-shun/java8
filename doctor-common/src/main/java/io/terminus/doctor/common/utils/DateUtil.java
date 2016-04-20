/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.common.utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-22
 */
public class DateUtil {

    private static final DateTimeParser[] parsers = {
            DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
            DateTimeFormat.forPattern("yyyyMMdd").getParser()
    };
    private static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

    /**
     * 检查输入的日期是否为有效格式
     *
     * @param value  输入的日期
     * @return  是否有效
     */
    public static boolean isValidDate(String value) {
        try {
            dateFormatter.parseDateTime(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
