package io.terminus.doctor.open.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 16/12/22.
 * 断奶日报对外封装
 */
@Data
public class DoctorWeanDailyReportOpen implements Serializable {
    private static final long serialVersionUID = -832266249012462774L;

    /**
     * 断奶数(断奶仔猪数)
     */
    private int count;

    /**
     * 断奶均重(kg)
     */
    private double weight;
}
