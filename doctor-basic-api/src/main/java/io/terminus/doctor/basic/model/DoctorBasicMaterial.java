package io.terminus.doctor.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 基础物料表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-16
 */
@Data
public class DoctorBasicMaterial implements Serializable {
    private static final long serialVersionUID = 1748858530290377442L;

    private Long id;
    
    /**
     * 物料类型
     * @see io.terminus.doctor.common.enums.WareHouseType
     */
    private Integer type;
    
    /**
     * 物料名称
     */
    private String name;

    /**
     * 输入码
     */
    private String srm;
    
    /**
     * 单位组id
     */
    private Long unitGroupId;
    
    /**
     * 单位组名称
     */
    private String unitGroupName;
    
    /**
     * 单位id
     */
    private Long unitId;
    
    /**
     * 单位名称
     */
    private String unitName;
    
    /**
     * 默认消耗数量
     */
    private Long defaultConsumeCount;
    
    /**
     * 价格(元)
     */
    private Long price;
    
    /**
     * 标注
     */
    private String remark;
    
    private Date createdAt;
    
    private Date updatedAt;
}
