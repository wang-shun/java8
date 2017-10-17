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
 * Date: 2017-10-13 17:14:31
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseStock implements Serializable {

    private static final long serialVersionUID = 7498736433565620548L;

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
     * 猪厂编号
     */
    private Long farmId;
    
    /**
     * 物料名称
     */
    private String skuName;
    
    /**
     * 物料编号
     */
    private Long skuId;
    
    /**
     * 数量
     */
    private java.math.BigDecimal quantity;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}