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
 * Date: 2017-08-24 13:34:00
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseMaterialHandle implements Serializable {

    private static final long serialVersionUID = -760508433193325235L;

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
     * 仓库类型
     */
    private Integer warehouseType;
    
    /**
     * 仓库名称
     */
    private String warehouseName;
    
    /**
     * 调拨，调入仓库编号
     */
    private Long targetWarehouseId;
    
    /**
     * 物料供应商名称
     */
    private String vendorName;
    
    /**
     * 物料编号
     */
    private Long materialId;
    
    /**
     * 物料名称
     */
    private String materialName;
    
    /**
     * 处理类别，入库，出库，调拨，盘点
     */
    private Integer type;
    
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
     * 处理月
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