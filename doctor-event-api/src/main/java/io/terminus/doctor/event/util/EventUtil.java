package io.terminus.doctor.event.util;

import io.terminus.doctor.event.model.DoctorGroupTrack;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/29
 */

public class EventUtil {

    public static double getWeight(double avgWeight, int quantity) {
        return avgWeight * quantity;
    }

    public static double getAvgWeight(double weight, int quantity) {
        if (quantity == 0) {
            return 0D;
        }
        return weight / quantity;
    }

    /**
     * 重新计算均重
     * @param oldWeight 旧猪均重
     * @param newWeight 新进猪均重
     * @param allQty    猪只总数
     * @return 均重
     */
    public static double getAvgWeight(double oldWeight, double newWeight, int allQty) {
        return getAvgWeight(oldWeight + newWeight, allQty);
    }

    public static int plusQuantity(int aq, int bq) {
        return aq + bq;
    }

    public static int minusQuantity(int aq, int bq) {
        return aq - bq;
    }

    /**
     * 根据公猪母猪数 判断猪群性别
     */
    public static int getSex(int boarQty, int sowQty) {
        if (boarQty != 0 && sowQty == 0) {
            return DoctorGroupTrack.Sex.MALE.getValue();
        }
        if (boarQty == 0 && sowQty != 0) {
            return DoctorGroupTrack.Sex.FEMALE.getValue();
        }
        return DoctorGroupTrack.Sex.MIX.getValue();
    }

    /**
     * 重新计算下日龄(四舍五入)
     * @param oldAge 旧猪日龄
     * @param oldQty 旧猪只数
     * @param newAge 新进猪日龄
     * @param newQty 新进猪只数
     * @return 日龄
     */
    public static int getAvgDayAge(int oldAge, int oldQty, int newAge, int newQty) {
        int allAge = oldAge * oldQty + newAge * newQty;
        return new BigDecimal(allAge).divide(new BigDecimal(oldQty + newQty), BigDecimal.ROUND_HALF_UP).intValue();
    }

    /**
     * 重新计算出生日期(日期-日龄 + 1)
     * @param date  日期
     * @param avgDayAge  日龄
     * @return 出生日期
     */
    public static Date getBirthDate(Date date, int avgDayAge) {
        return new DateTime(date).minusDays(avgDayAge - 1).withTimeAtStartOfDay().toDate();
    }

    public static long getPrice(long amount, int quantity) {
        return amount / quantity;
    }
}
