/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.common.utils;

import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.util.Date;

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

    private static final DateTimeFormatter DATE = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final DateTimeFormatter DATE_TIME = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");


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

    public static Date toDate(String date) {
        if (Strings.isNullOrEmpty(date)) return null;
        return DATE.parseDateTime(date).toDate();
    }

    public static Date toDate(String date, Date defaultValue) {
        try {
            return DATE.parseDateTime(date).toDate();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Date toDateTime(String dateTime) {
        if (Strings.isNullOrEmpty(dateTime)) return null;
        return DATE_TIME.parseDateTime(dateTime).toDate();
    }

    public static Date toDateTime(String dateTime, Date defaultValue) {
        try {
            return DATE_TIME.parseDateTime(dateTime).toDate();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String toDateString(Date date) {
        if (date == null) return null;
        return new DateTime(date).toString(DATE);
    }

    public static String toDateTimeString(Date date) {
        if (date == null) return null;
        return new DateTime(date).toString(DATE_TIME);
    }


}
