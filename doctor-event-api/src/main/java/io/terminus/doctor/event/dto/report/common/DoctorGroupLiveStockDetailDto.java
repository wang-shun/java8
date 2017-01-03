package io.terminus.doctor.event.dto.report.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 16/12/23.
 * 猪群存栏明细
 */
@Data
public class DoctorGroupLiveStockDetailDto implements Serializable{
    private static final long serialVersionUID = 804590291384382842L;

    /**
     *  猪场名称
     */
    private String farmName;

    /**
     * 统计时间
     */
    private Date sumAt;

    /**
     * 猪群号
     */
    private String groupCode;

    /**
     * 日龄
     */
    private int dayAge;

    /**
     * 存栏数
     */
    private int liveStocks;

    /**
     * 类型
     */
    private String type;
}
