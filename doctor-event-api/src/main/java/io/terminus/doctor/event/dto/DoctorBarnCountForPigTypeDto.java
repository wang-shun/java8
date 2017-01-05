package io.terminus.doctor.event.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/1/5.
 */
@Data
public class DoctorBarnCountForPigTypeDto implements Serializable{
    private static final long serialVersionUID = -5992821579049641284L;

    /**
     * 全部数量
     */
    private long allCount;
    /**
     * 保育舍数量
     */
    private long nurseryPigletCount;
    /**
     * 育肥舍数量
     */
    private long fattenPigCount;
    /**
     *后备舍数量
     */
    private long reserveCount;
    /**
     * 配种舍数量
     */
    private long mateSowCount;
    /**
     * 妊娠舍数量
     */
    private long pregSowCount;
    /**
     * 分娩舍数量
     */
    private long deliverSowCount;

    /**
     * 公猪舍数量
     */
    private long boarCount;
}
