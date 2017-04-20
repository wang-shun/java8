package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 11:36 2017/4/20
 */
@Data
public class DoctorIndicatorReport implements Serializable{
    private static final long serialVersionUID = -8907919612943207115L;

    /**
     * 估算受胎率
     */
    private Double mateEstimatePregRate;

    /**
     * 实际受胎率
     */
    private Double mateRealPregRate;

    /**
     * 估算配种分娩率
     */
    private Double mateEstimateFarrowingRate;

    /**
     * 实际配种分娩率
     */
    private Double mateRealFarrowingRate;

    /**
     * 窝均断奶数
     */
    private Double weanAvgCount;

    /**
     * 窝均断奶日龄
     */
    private Double weanAvgDayAge;

    /**
     * 产房死淘率
     */
    private Double deadFarrowRate;

    /**
     * 保育死淘率
     */
    private Double deadNurseryRate;

    /**
     * 育肥死淘率
     */
    private Double deadFattenRate;

    /**
     * 非生产天数
     */
    private Double npd;

    /**
     * psy
     */
    private Double psy;

    /**
     * 断奶七天配种率
     */
    private Double mateInSeven;
}
