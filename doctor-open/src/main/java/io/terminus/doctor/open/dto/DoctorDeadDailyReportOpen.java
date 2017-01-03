package io.terminus.doctor.open.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 16/12/23.
 * 死淘日报对外封装
 */
@Data
public class DoctorDeadDailyReportOpen implements Serializable{
    private static final long serialVersionUID = 2648791211433030773L;

    /**
     * 公猪
     */
    private int boar;

    /**
     * 母猪
     */
    private int sow;

    /**
     * 产房仔猪
     */
    private int farrow;

    /**
     * 保育猪
     */
    private int nursery;

    /**
     * 育肥猪
     */
    private int fatten;
}
