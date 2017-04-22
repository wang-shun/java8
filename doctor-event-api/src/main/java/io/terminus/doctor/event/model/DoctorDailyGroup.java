package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Code generated by terminus code gen
 * Desc: 猪群数量每天记录表Model类
 * Date: 2017-04-18
 */
@Data
public class DoctorDailyGroup implements Serializable {
    private static final long serialVersionUID = 7619929987707327637L;

    private Long id;
    
    /**
     * 猪场id
     */
    private Long farmId;
    
    /**
     * 猪群id
     */
    private Long groupId;
    
    /**
     * 猪群类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer type;
    
    /**
     * 日期
     */
    private Date sumAt;
    
    /**
     * 期初
     */
    private Integer start;
    
    /**
     * 断奶数量
     */
    private Integer weanCount;
    
    /**
     * 未断奶数量
     */
    private Integer unweanCount;
    
    /**
     * 同类型猪群转入，后面统计不计入该类型猪群转入
     */
    private Integer innerIn;
    
    /**
     * 不同类型猪群转入，外部转入
     */
    private Integer outerIn;
    
    /**
     * 销售
     */
    private Integer sale;
    
    /**
     * 死亡
     */
    private Integer dead;
    
    /**
     * 淘汰
     */
    private Integer weedOut;
    
    /**
     * 其他变动减少
     */
    private Integer otherChange;
    
    /**
     * 转场
     */
    private Integer chgFarm;
    
    /**
     * 同类型猪群转群，不计入该类型猪	群减少
     */
    private Integer innerOut;
    
    /**
     * 不同类型猪群转群,转种猪
     */
    private Integer outerOut;
    
    /**
     * 期末
     */
    private Integer end;

    private Date updatedAt;
    private Date createdAt;
}
