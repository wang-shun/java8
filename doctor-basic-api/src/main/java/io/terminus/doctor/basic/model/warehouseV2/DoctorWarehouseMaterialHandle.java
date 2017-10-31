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
 * Date: 2017-10-31 16:15:20
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseMaterialHandle implements Serializable {

    private static final long serialVersionUID = 5840930610284126017L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 库存处理ID
     */
    private Long stockHandleId;
    
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
     * 另一条调拨物料处理单的编号
     */
    private Long otherTransferHandleId;
    
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
     * 单位
     */
    private String unit;
    
    /**
     * 删除标志
     */
    private Integer deleteFlag;
    
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
     * 操作人编号
     */
    private Long operatorId;
    
    /**
     * 操作人名
     */
    private String operatorName;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}