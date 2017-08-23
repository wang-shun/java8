package io.terminus.doctor.basic.model.warehouse;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 22:58:24
 * Created by [ your name ]
 */
@Data
public class DoctorWarehousePurchase implements Serializable {

    private static final long serialVersionUID = -8633163849850228561L;

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
    private String warehouseName;
    
    /**
     * 物料编号
     */
    private Long materialId;
    
    /**
     * 
     */
    private String vendorName;
    
    /**
     * 单价，单位分
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
     * 
     */
    private java.math.BigDecimal handleQuantity;
    
    /**
     * 是否该批入库已出库完。0出库完，1未出库完。handle_quantity&#x3D;quantity就表示出库完
     */
    private Integer handleFinishFlag;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}