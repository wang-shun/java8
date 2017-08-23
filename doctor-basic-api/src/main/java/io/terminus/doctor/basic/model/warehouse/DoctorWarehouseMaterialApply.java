package io.terminus.doctor.basic.model.warehouse;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-22 17:44:57
 * Created by [ your name ]
 */
@Data
public class DoctorWarehouseMaterialApply implements Serializable {

    private static final long serialVersionUID = -3262141915541470851L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 
     */
    private Long warehouseId;
    
    /**
     * 
     */
    private Long pigHouseId;
    
    /**
     * 
     */
    private String pigHouseName;
    
    /**
     * 
     */
    private Long pigGroupId;
    
    /**
     * 
     */
    private String pigGroupName;
    
    /**
     * 
     */
    private Long materialId;
    
    /**
     * 
     */
    private Date applyDate;
    
    /**
     * 
     */
    private String applyPersonName;
    
    /**
     * 
     */
    private Integer applyYear;
    
    /**
     * 
     */
    private Integer applyMonth;
    
    /**
     * 
     */
    private String materialName;
    
    /**
     * 
     */
    private Integer type;
    
    /**
     * 
     */
    private String unit;
    
    /**
     * 
     */
    private java.math.BigDecimal quantity;
    
    /**
     * 
     */
    private Long unitPrice;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}