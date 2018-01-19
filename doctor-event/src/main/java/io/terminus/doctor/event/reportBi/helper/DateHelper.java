package io.terminus.doctor.event.reportBi.helper;

import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.event.enums.DateDimension;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.Month;
import java.util.Date;

/**
 * Created by xjn on 18/1/12.
 * email:xiaojiannan@terminus.io
 */
public class DateHelper {
    private static final DateTimeFormatter DAY = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MMDD = DateTimeFormat.forPattern("MMdd");
    private static final DateTimeFormatter WEEK = DateTimeFormat.forPattern("x年第ww周");
    private static final DateTimeFormatter MONTH = DateTimeFormat.forPattern("yyyy年MM月");
    private static final DateTimeFormatter YEAR = DateTimeFormat.forPattern("yyyy年");


    public static Date withDateStartDay(Date date, DateDimension dateDimension) {
        DateTime dateTime = new DateTime(date);
        switch (dateDimension) {
            case DAY:
                return date;
            case WEEK:
                return dateTime.withDayOfWeek(1).toDate();
            case MONTH:
                return dateTime.withDayOfMonth(1).toDate();
            case QUARTER:
                int month = dateTime.getMonthOfYear();
                if (month >= 10) {
                    return dateTime.withMonthOfYear(10).withDayOfMonth(1).toDate();
                } else if (month >= 7) {
                    return dateTime.withMonthOfYear(7).withDayOfMonth(1).toDate();
                } else if (month >= 4) {
                    return dateTime.withMonthOfYear(4).withDayOfMonth(1).toDate();
                } else {
                    return dateTime.withMonthOfYear(1).withDayOfMonth(1).toDate();
                }
            case YEAR:
                return dateTime.withDayOfYear(1).toDate();
            default:
                throw new InvalidException("with.date.start.day.failed", date);
        }
    }

    public static Date withDateEndDay(Date date, DateDimension dateDimension) {
        DateTime dateTime = new DateTime(date);
        switch (dateDimension) {
            case DAY:
                return date;
            case WEEK:
                return dateTime.withDayOfWeek(7).toDate();
            case MONTH:
                return dateTime.withDayOfMonth(1).plusMonths(1).minusMillis(1).toDate();
            case QUARTER:
                int month = dateTime.getMonthOfYear();
                if (month >= 10) {
                    return dateTime.withMonthOfYear(12).withDayOfMonth(31).toDate();
                } else if (month >= 7) {
                    return dateTime.withMonthOfYear(9).withDayOfMonth(30).toDate();
                } else if (month >= 4) {
                    return dateTime.withMonthOfYear(6).withDayOfMonth(30).toDate();
                } else {
                    return dateTime.withMonthOfYear(3).withDayOfMonth(31).toDate();
                }
            case YEAR:
                return dateTime.withDayOfYear(1).plusYears(1).minusMillis(1).toDate();
            default:
                throw new InvalidException("with.date.start.day.failed", date);
        }
    }

    public static String dateCN(Date date, DateDimension dateDimension) {
        DateTime dateTime = new DateTime(date);
        switch (dateDimension) {
            case DAY:
                return dateTime.toString(DAY);
            case WEEK:
                DateTime weekStart = dateTime.withDayOfWeek(1);
                DateTime weekEnd = dateTime.withDayOfWeek(7);
                String week = dateTime.toString(WEEK);
                return week + "（" + weekStart.toString(MMDD) + "-" + weekEnd.toString(MMDD) + "）";
            case MONTH:
                return dateTime.toString(MONTH);
            case QUARTER:
                int quarter;
                int month = dateTime.getMonthOfYear();
                if (month >= 10) {
                    quarter = 4;
                } else if (month >= 7) {
                    quarter = 3;
                } else if (month >= 4) {
                    quarter = 2;
                } else {
                    quarter = 1;
                }
                return dateTime.toString(YEAR) + "第" + quarter + "季度";
            case YEAR:
                return dateTime.toString(YEAR);
            default:
                throw new InvalidException("date.CN.failed", date);
        }
    }


}
