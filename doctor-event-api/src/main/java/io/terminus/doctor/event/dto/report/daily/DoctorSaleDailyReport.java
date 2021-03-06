package io.terminus.doctor.event.dto.report.daily;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 销售日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorSaleDailyReport implements Serializable {
    private static final long serialVersionUID = -1170948612908091614L;

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
     * 后备猪
     */
    private double houbei;

    /**
     * 10kg基础价格
     */
    private long basePrice10;

    /**
     * 15kg基础价格
     */
    private long basePrice15;

    /**
     * 育肥猪单价
     */
    private long fattenPrice;
}
