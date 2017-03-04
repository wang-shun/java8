package io.terminus.doctor.event.dto.report.daily;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 死淘日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorDeadDailyReport implements Serializable {
    private static final long serialVersionUID = 4988732219317374200L;

    /**
     * 公猪
     */
    private int boar;

    /**
     * 公猪淘汰
     */
    private int weedOutBoar;

    /**
     * 母猪
     */
    private int sow;

    /**
     * 母猪淘汰
     */
    private int weedOutSow;

    /**
     * 产房仔猪
     */
    private int farrow;

    /**
     * 产房仔猪淘汰
     */
    private int weedOutFarrow;

    /**
     * 保育猪
     */
    private int nursery;

    /**
     * 保育猪淘汰
     */
    private int WeedOutNursery;

    /**
     * 育肥猪
     */
    private int fatten;

    /**
     * 育肥猪淘汰
     */
    private int weedOutFatten;

    /**
     * 后备猪
     */
    private int houbei;

    /**
     * 后备猪淘汰
     */
    private int weedOutHoubei;
}
