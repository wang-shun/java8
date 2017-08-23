package io.terminus.doctor.basic.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-22 01:46:48
 * Created by [ your name ]
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWarehouseMaterialHandle implements Serializable {

    private static final long serialVersionUID = -7044474816310444397L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 
     */
    private Long farmId;
    
    /**
     * 
     */
    private Long warehouseId;
    
    /**
     * 
     */
    private Integer warehouseType;
    
    /**
     * 
     */
    private Long targetWarehouseId;
    
    /**
     * 
     */
    private String vendorName;
    
    /**
     * 
     */
    private Long materialId;
    
    /**
     * 
     */
    private String materialName;
    
    /**
     * 处理类别，入库，出库，调拨，盘点
     */
    private Integer type;
    
    /**
     * 
     */
    private Long unitPrice;
    
    /**
     * 
     */
    private java.math.BigDecimal quantity;
    
    /**
     * 
     */
    private Date handleDate;
    
    /**
     * 
     */
    private Integer handleYear;
    
    /**
     * 
     */
    private Integer handleMonth;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}