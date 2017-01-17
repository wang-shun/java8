package io.terminus.doctor.open.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 16/12/22.
 * 分娩日报对外封装
 */
@Data
public class DoctorDeliverDailyReportOpen implements Serializable {
    private static final long serialVersionUID = -2639700569210164439L;

    /**
     * 分娩窝数
     */
    private int nest;

    /**
     * 活仔数
     */
    private int live;

    /**
     * 健仔数
     */
    private int health;

    /**
     * 弱仔数
     */
    private int weak;

    /**
     * 死黑木畸
     */
    private int black;
}
