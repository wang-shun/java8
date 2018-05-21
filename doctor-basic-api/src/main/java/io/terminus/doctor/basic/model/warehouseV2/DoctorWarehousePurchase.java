package io.terminus.doctor.basic.model.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-27 12:08:23
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class DoctorWarehousePurchase implements Serializable {

    private static final long serialVersionUID = 643396208751334565L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 猪厂编号
     */
    private Long farmId;
    
    /**
     * 仓库编号
     */
    private Long warehouseId;
    
    /**
     * 仓库名称
     */
    private String warehouseName;
    
    /**
     * 仓库类型
     */
    private Integer warehouseType;
    
    /**
     * 物料编号
     */
    private Long materialId;
    
    /**
     * 物料供应商名称
     */
    private String vendorName;
    
    /**
     * 单价，单位分
     */
    private Long unitPrice;
    
    /**
     * 数量
     */
    private java.math.BigDecimal quantity;
    
    /**
     * 处理日期
     */
    private Date handleDate;
    
    /**
     * 处理年
     */
    private Integer handleYear;
    
    /**
     * 处理月份
     */
    private Integer handleMonth;
    
    /**
     * 已出库的数量
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