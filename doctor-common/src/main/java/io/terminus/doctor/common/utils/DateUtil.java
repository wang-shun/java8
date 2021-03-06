/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.common.utils;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Dates;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-22
 */
public class DateUtil {

    private static final DateTimeParser[] parsers = {
            DateTimeFormat.forPattern("yyyy-MM-dd").getParser()
    };
    private static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

    public static final DateTimeFormatter YYYYMM = DateTimeFormat.forPattern("yyyy-MM");

    public static final DateTimeFormatter DATE = DateTimeFormat.forPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DATE_CN = DateTimeFormat.forPattern("yyyy年MM月dd日");

    public static final DateTimeFormatter DATE_TIME = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    //精确到毫秒
    public static final DateTimeFormatter DATE_TIME_MSEC = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss:SSS");

    public static final DateTimeFormatter DATE_SLASH = DateTimeFormat.forPattern("yyyy/MM/dd");

    public static Date formatToDate(DateTimeFormatter formatter, String date) {
        try {
            if (formatter == null || Strings.isNullOrEmpty(date)) {
                return null;
            }
            return formatter.parseDateTime(date).toDate();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 检查输入的日期是否为有效格式
     *
     * @param value 输入的日期
     * @return 是否有效
     */
    public static boolean isYYYYMMDD(String value) {
        try {
            dateFormatter.parseDateTime(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Date toYYYYMM(String date) {
        if (Strings.isNullOrEmpty(date)) return null;
        return YYYYMM.parseDateTime(date).toDate();
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

    public static DateTime getDateEnd(DateTime date) {
        if (date == null) return null;
        return date.withTimeAtStartOfDay().plusDays(1).plusSeconds(-1);
    }

    /**
     * 获取index之前的所有天
     *
     * @param date  初始date
     * @param index 前几天
     * @return 天list
     */
    public static List<Date> getBeforeDays(Date date, Integer index) {
        List<Date> days = Lists.newArrayListWithCapacity(index);

        DateTime start = new DateTime(date).withTimeAtStartOfDay();
        for (int i = 0; i < index; i++) {
            days.add(start.toDate());
            start = start.plusDays(-1);
        }
        return days;
    }

    /**
     * 获取index之前的所有月末
     *
     * @param date  初始date
     * @param index 前几月
     * @return 月末list(本月是当前天)
     */
    public static List<Date> getBeforeMonthEnds(Date date, Integer index) {
        List<Date> months = Lists.newArrayListWithCapacity(index);
        DateTime todayEnd = getDateEnd(MoreObjects.firstNonNull(new DateTime(date), DateTime.now()));
        months.add(todayEnd.toDate());

        DateTime start = todayEnd.withDayOfMonth(1);
        for (int i = 1; i < index; i++) {
            months.add(start.plusDays(-1).toDate());
            start = start.plusMonths(-1).withDayOfMonth(1);
        }
        return months;
    }

    /**
     * 获取index之前的所有周日的日末
     *
     * @param date  初始date
     * @param index 前几月
     * @return 月末list(本月是当前天)
     */
    public static List<Date> getBeforeWeekEnds(Date date, Integer index) {
        List<Date> weeks = Lists.newArrayListWithCapacity(index);
        DateTime todayEnd = getDateEnd(MoreObjects.firstNonNull(new DateTime(date), DateTime.now()));
        weeks.add(todayEnd.toDate());

        //从上周开始
        DateTime start = todayEnd.withDayOfWeek(7).plusWeeks(-1);
        for (int i = 1; i < index; i++) {
            weeks.add(start.toDate());
            start = start.plusWeeks(-1);
        }
        return weeks;
    }


    /**
     * 获取当月的最后一天
     *
     * @param date
     * @return
     */
    public static DateTime getMonthEnd(DateTime date) {
        if (date == null) return null;
        return date.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().minusSeconds(1);
    }

    /**
     * 同一年显示 x月, 不同年显示 xxxx年x月
     * @param date 日期
     * @return 日期格式化的结果
     */
    public static String getDateStr(Date date) {
        if (date == null) {
            return "";
        }
        DateTime datetime = new DateTime(date);
        if (datetime.getYear() == DateTime.now().getYear()) {
            return datetime.getMonthOfYear() + "月";
        }
        return datetime.getYear() + "年" + datetime.getMonthOfYear() + "月";
    }


    /**
     * 求开始日期与结束日期之间的天数 deltaDays = endAt - startAt
     * @param startAt 开始时间
     * @param endAt 结束时间
     * @return 天数
     */
    public static int getDeltaDays(Date startAt, Date endAt) {
        Duration duration = new Duration(new DateTime(startAt), new DateTime(endAt));
        return (int) duration.getStandardDays();
    }

    /**
     * 求开始日期与结束日期之间的天数的绝对值 deltaDays = abs(endAt - startAt)
     * @param startAt 开始时间
     * @param endAt 结束时间
     * @return 天数
     */
    public static int getDeltaDaysAbs(Date startAt, Date endAt) {
        return Math.abs(getDeltaDays(startAt, endAt));
    }

    /**
     * 求开始日期与结束日期之间的月份间隔 deltaMonths = 12 * Δyear + Δmonth
     * @param startAt 开始时间
     * @param endAt 结束时间
     * @return 月数
     */
    public static int getDeltaMonths(Date startAt, Date endAt) {
        DateTime start = new DateTime(startAt);
        DateTime end = new DateTime(endAt);
        return 12 * (end.getYear() - start.getYear()) + (end.getMonthOfYear() - start.getMonthOfYear());
    }

    public static int getDeltaMonthsAbs(Date startAt, Date endAt) {
        return Math.abs(getDeltaMonths(startAt, endAt));
    }

    /**
     * 判断参数时间是否处在同一天
     * @return
     */
    public static boolean inSameDate(Date date1, Date date2){
        return new DateTime(date1).toString(DATE).equals(new DateTime(date2).toString(DATE));
    }

    /**
     * 判断参数事件是否处在同年同月
     * @param date1
     * @param date2
     * @return
     */
    public static boolean inSameYearMonth(Date date1, Date date2){
        return new DateTime(date1).toString(YYYYMM).equals(new DateTime(date2).toString(YYYYMM));
    }

    /**
     *
     * @param dateStr
     * @return
     */
    public static Date stringToDate(String dateStr){
        try {
            return new Date(Long.parseLong(dateStr));
        } catch (NumberFormatException e) {
            return toDateTime(dateStr);
        }
    }

    /**
     * 周初，周末，月初，月末
     */
    public static Date weekStart(Date date) {
        return new DateTime(date).withDayOfWeek(1).withTimeAtStartOfDay().toDate();
    }

    public static Date weekEnd(Date date) {
        if (!Dates.startOfDay(date).before(Dates.startOfDay(new Date()))) {
            return DateUtil.getDateEnd(DateTime.now()).toDate();
        }
        return new DateTime(date).withDayOfWeek(1).plusWeeks(1).withTimeAtStartOfDay().minusSeconds(1).toDate();
    }

    public static Date monthStart(Date date) {
        return new DateTime(date).withDayOfMonth(1).withTimeAtStartOfDay().toDate();
    }

    public static Date monthEnd(Date date) {
        if (Dates.startOfDay(date).equals(Dates.startOfDay(new Date()))) {
            return DateUtil.getDateEnd(new DateTime(date)).toDate();
        }
        return DateUtil.getMonthEnd(new DateTime(date)).toDate();
    }

    public static String getYearWeek(Date date){
        DateTime dateTime = new DateTime(date);
        StringBuffer stringBuffer = new StringBuffer();
        int week = dateTime.getWeekOfWeekyear();
        stringBuffer.append(dateTime.getWeekyear());
        stringBuffer.append("-");
        if(week < 10){
            stringBuffer.append(0);
        }
        stringBuffer.append(dateTime.getWeekOfWeekyear());
        return stringBuffer.toString();
    }

    public static String getYearMonth(Date date){
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(YYYYMM);
    }

    public static String getYearWeek(Integer year, Integer week){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(year);
        stringBuffer.append("-");
        if(week < 10){
            stringBuffer.append(0);
        }
        stringBuffer.append(week);
        return stringBuffer.toString();
    }

    /**
     * 获取指定年份和周的日期, 如果是未来时间，返回今天
     * @param year 年
     * @param week 周
     * @return 日期
     */
    public static DateTime withWeekOfYear(Integer year, Integer week) {
        if (year == null || week == null) {
            return DateUtil.getDateEnd(DateTime.now());
        }
        DateTime yearDate = new DateTime(year, 1, 1, 0, 0);

        while (true) {
            if (yearDate.getDayOfWeek() == 7) {
                break;
            }
            yearDate = yearDate.plusDays(1);
        }
        return new DateTime(DateUtil.weekEnd(yearDate.plusWeeks(week).toDate()));
    }

    public static Date getMonthEndOrToday(DateTime datetime) {
        if(datetime.isBeforeNow()){
            return DateUtil.getMonthEnd(datetime).toDate();
        }
        return new Date();
    }

    public static List<Date> getDates(Date startAt, Date endAt){
        if (startAt == null || endAt == null || startAt.after(endAt)) {
            return Lists.newArrayList();
        }

        if (startAt.equals(endAt)) {
            return Lists.newArrayList(startAt);
        }

        List<Date> list = Lists.newArrayList();
        while (startAt.before(endAt)) {
            list.add(startAt);
            startAt = new DateTime(startAt).plusDays(1).toDate();
        }
        list.add(endAt);
        return list;
    }

    public static String toDateTimeToSSSString(Date date) {
        if (date == null) return null;
        return new DateTime(date).toString(DATE_TIME_MSEC);
    }

    public static String formatDateStringForTimeorder(Date date) {
        String dateStr = toDateTimeToSSSString(date);
        if (StringUtils.isBlank(dateStr)) return null;
        return dateStr.replace("-","")
                        .replace(" ","")
                           .replace(":","");
    }

    public static Integer getYearForDate(Date date){
        Calendar  calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);//得到年
    }

    public static Integer getMonthForDate(Date date){
        Calendar  calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH + 1);//得到月
    }

    public static Integer getDayForDate(Date date){
        Calendar  calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DATE);//得到日
    }

}
