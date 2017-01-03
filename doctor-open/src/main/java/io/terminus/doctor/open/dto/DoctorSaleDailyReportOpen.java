package io.terminus.doctor.open.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 16/12/23.
 * 销售日报对外封装
 */
@Data
public class DoctorSaleDailyReportOpen implements Serializable {
    private static final long serialVersionUID = 9000249194639579580L;

    /**
     * 公猪
     */
    private double boar;

    /**
     * 母猪
     */
    private double sow;

    /**
     * 保育猪(产房 + 保育)
     */
    private double nursery;

    /**
     * 育肥猪
     */
    private double fatten;

    /**
     * 销售金额
     */
    private double amount; //// TODO: 16/12/23 缺少
}
