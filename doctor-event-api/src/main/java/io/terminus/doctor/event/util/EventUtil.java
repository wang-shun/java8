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
        return (oldWeight + newWeight) / allQty;
    }

    public static int plusQuantity(int aq, int bq) {
        return aq + bq;
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
     * 重新计算下日龄差(四舍五入)
     * @param oldAge 旧猪日龄
     * @param oldQty 旧猪只数
     * @param newAge 新进猪日龄
     * @param newQty 新进猪只数
     * @return 日龄差
     */
    public static int deltaDayAge(int oldAge, int oldQty, int newAge, int newQty) {
        int allAge = oldAge * oldQty + newAge * newQty;
        return new BigDecimal(allAge).divide(new BigDecimal(oldQty + newQty), BigDecimal.ROUND_HALF_UP).intValue() - oldAge;
    }

    /**
     * 重新计算出生日期
     * @param birthDate 出生日期
     * @param deltaAge  日龄差
     * @return 出生日期
     */
    public static Date getBirthDate(Date birthDate, int deltaAge) {
        return new DateTime(birthDate).minusDays(deltaAge).toDate();
    }

    public static long getPrice(long amount, int quantity) {
        return amount / quantity;
    }
}
