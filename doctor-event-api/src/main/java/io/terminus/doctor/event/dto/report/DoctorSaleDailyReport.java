package io.terminus.doctor.event.dto.report;

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
    private int boar;

    /**
     * 母猪
     */
    private int sow;

    /**
     * 保育猪(产房 + 保育)
     */
    private int nursery;

    /**
     * 育肥猪
     */
    private int fatten;
}
