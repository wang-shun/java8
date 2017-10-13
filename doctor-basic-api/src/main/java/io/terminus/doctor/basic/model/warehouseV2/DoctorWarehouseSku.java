package io.terminus.doctor.basic.model.warehouseV2;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-13 13:53:41
 * Created by [ your name ]
 */
@Data
public class DoctorWarehouseSku implements Serializable {

    private static final long serialVersionUID = 673328646593822103L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 公司编号
     */
    private Long orgId;
    
    /**
     * 猪厂编号
     */
    private Long farmId;
    
    /**
     * 物料名称
     */
    private String name;
    
    /**
     * 编码,用于跨厂调拨
     */
    private String code;
    
    /**
     * 短码,用于查询
     */
    private String srm;
    
    /**
     * 供应商编号
     */
    private Long vendorId;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 规格
     */
    private String specification;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}