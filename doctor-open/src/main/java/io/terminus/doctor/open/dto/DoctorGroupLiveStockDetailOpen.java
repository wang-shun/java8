package io.terminus.doctor.open.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 16/12/23.
 *  猪群存栏明细对外封装
 */
@Data
public class DoctorGroupLiveStockDetailOpen implements Serializable {
    private static final long serialVersionUID = -7870228678226671146L;

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
