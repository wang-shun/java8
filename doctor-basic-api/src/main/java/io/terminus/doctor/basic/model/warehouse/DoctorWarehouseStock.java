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
 * Date: 2017-08-21 17:52:59
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseStock implements Serializable {

    private static final long serialVersionUID = -4771297360870774453L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 仓库编号
     */
    private Long warehouseId;
    
    /**
     * 仓库名称
     */
    private String warehouseName;
    
    /**
     * 仓库类型，冗余，方便查询
     */
    private Integer warehouseType;
    
    /**
     * 物料供应商
     */
    private String vendorName;
    
    /**
     * 农场编号
     */
    private Long farmId;
    
    /**
     * 管理员编号
     */
    private Long managerId;
    
    /**
     * 物料名称
     */
    private String materialName;
    
    /**
     * 物料编号
     */
    private Long materialId;
    
    /**
     * 数量
     */
    private java.math.BigDecimal quantity;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}