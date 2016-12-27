package io.terminus.doctor.event.dto.report.daily;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 存栏日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorLiveStockDailyReport implements Serializable {
    private static final long serialVersionUID = 2428304354860017632L;

    /**
     * 后备母猪(后备舍的母猪)
     */
    private int houbeiSow;

    /**
     * 配怀母猪(配种舍 + 妊娠舍)
     */
    private int peihuaiSow;

    /**
     * 产房
     */
    private int buruSow;

    /**
     * 空怀母猪
     * 作废掉, 现在是按照猪舍统计
     */
    @Deprecated
    private int konghuaiSow;

    /**
     * 后备公猪
     */
    private int houbeiBoar;

    /**
     * 公猪
     */
    private int boar;

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
