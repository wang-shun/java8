package io.terminus.doctor.open.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 16/12/23.
 * 存栏日好对外封装
 */
@Data
public class DoctorLiveStockDailyReportOpen implements Serializable{
    private static final long serialVersionUID = -324694216387736049L;

    /**
     * 现在后备舍没有母猪
     * 后备母猪(后备舍的母猪)
     */
    @Deprecated
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
     * 母猪数量
     */
    private int sowTotal;

    /**
     * 猪群数
     */
    private int group;

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

    /**
     * 当前待出栏
     */
    private int fattenOut;

    /**
     * 当前存栏总数
     */
    private int liveStockTotal;
}
