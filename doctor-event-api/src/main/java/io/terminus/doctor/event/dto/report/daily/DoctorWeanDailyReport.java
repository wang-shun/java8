package io.terminus.doctor.event.dto.report.daily;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 断奶仔猪日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorWeanDailyReport implements Serializable {
    private static final long serialVersionUID = 5413097064736038911L;

    /**
     * 断奶窝数(断奶母猪数)
     */
    private int nest;

    /**
     * 断奶数(断奶仔猪数)
     */
    private int count;

    /**
     * 断奶均重(kg)
     */
    private double weight;

    /**
     * 断奶均日龄
     */
    private double avgDayAge;

    /**
     * 产房仔猪转场
     */
    private int farrowChgFarm;

    /**
     * 产房仔猪转保育
     */
    private int farrowToNursery;

    /**
     * 产房仔猪销售
     */
    private int farrowSale;
}
