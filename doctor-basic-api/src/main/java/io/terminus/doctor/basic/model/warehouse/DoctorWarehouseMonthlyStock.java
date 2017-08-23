package io.terminus.doctor.basic.model.warehouse;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-17 17:50:38
 * Created by [ your name ]
 */
@Data
public class DoctorWarehouseMonthlyStock implements Serializable {

    private static final long serialVersionUID = -8405985068049793556L;

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
    private String warehouseName;
    
    /**
     * 
     */
    private Integer warehouseType;
    
    /**
     * 
     */
    private Long materialId;
    
    /**
     * 
     */
    private String materialName;
    
    /**
     * 
     */
    private Long vendorId;
    
    /**
     * 
     */
    private Integer year;
    
    /**
     * 
     */
    private Integer month;
    
    /**
     * 月初物料数量
     */
    private java.math.BigDecimal earlyNumber;
    
    /**
     * 月初物料金额
     */
    private Long earlyMoney;
    
    /**
     * 
     */
    private java.math.BigDecimal inNumber;
    
    /**
     * 
     */
    private Long inMoney;
    
    /**
     * 
     */
    private java.math.BigDecimal outNumber;
    
    /**
     * 
     */
    private Long outMoney;
    
    /**
     * 月末物料余量
     */
    private java.math.BigDecimal balanceNumber;
    
    /**
     * 月末物料余额
     */
    private Long balanceMoney;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}