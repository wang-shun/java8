package io.terminus.doctor.basic.model.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-16 22:14:22
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseSku implements Serializable {

    private static final long serialVersionUID = 8742028251011930610L;

    /**
     * 自增主键
     */
    @NotNull(message = "warehouse.sku.id.null", groups = UpdateValid.class)
    private Long id;
    
    /**
     * 公司编号
     */
    @NotNull(message = "warehouse.sku.org.id.null", groups = CreateValid.class)
    private Long orgId;
    
    /**
     * 猪厂编号
     */
//    @NotNull(message = "warehouse.sku.farm.id.null", groups = CreateValid.class)
    private Long farmId;
    
    /**
     * 仓库编号
     */
//    @NotNull(message = "warehouse.sku.warehouse.id.null", groups = CreateValid.class)
    private Long warehouseId;
    
    /**
     * 仓库名称
     */
    private String warehouseName;
    
    /**
     * 物料类型编号
     */
    @NotNull(message = "warehouse.sku.item.id.null", groups = CreateValid.class)
    private Long itemId;
    
    /**
     * 基础物料名称
     */
    private String itemName;
    
    /**
     * 基础物料类型
     */
    private Integer type;
    
    /**
     * 物料名称
     */
    @NotNull(message = "warehouse.sku.name.null", groups = CreateValid.class)
    private String name;
    
    /**
     * 编码,用于跨厂调拨
     */
    @NotNull(message = "warehouse.sku.code.null", groups = CreateValid.class)
    private String code;
    
    /**
     * 短码,用于查询
     */
    private String srm;
    
    /**
     * 供应商
     */
    private String vendorName;
    
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


    public static interface UpdateValid extends CreateValid {

    }

    public static interface CreateValid {

    }
}