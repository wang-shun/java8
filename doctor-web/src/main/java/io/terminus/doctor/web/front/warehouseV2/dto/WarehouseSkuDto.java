package io.terminus.doctor.web.front.warehouseV2.dto;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import lombok.Data;
import org.apache.xmlbeans.impl.jam.mutable.MElement;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/10/26.
 */
@Data
public class WarehouseSkuDto extends BasicDto {


    /**
     * 自增主键
     */
    @NotNull(message = "warehouse.sku.id.null", groups = UpdateValid.class)
    private Long id;

    /**
     * 公司编号
     */
//    @NotNull(message = "warehouse.sku.org.id.null", groups = CreateValid.class)
    private Long orgId;

    private Long farmId;

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
    @NotBlank(message = "warehouse.sku.name.null", groups = CreateValid.class)
    @Size(message = "warehouse.sku.name.too.long", max = 120, groups = CreateValid.class)
    private String name;

    /**
     * 编码,用于跨厂调拨
     */
    @NotBlank(message = "warehouse.sku.code.null", groups = CreateValid.class)
    private String code;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 短码,用于查询
     */
    @Size(message = "warehouse.sku.srm.too.lang", max = 30, groups = CreateValid.class)
    private String srm;

    /**
     * 供应商编号
     */
    private Long vendorId;

    /**
     * 供应商名称
     */
    private String vendorName;

    /**
     * 单位
     */
    private String unit;

    private Long unitId;

    /**
     * 规格
     */
    @Size(message = "warehouse.sku.specification.too.lang", max = 60, groups = CreateValid.class)
    private String specification;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    public interface UpdateValid extends CreateValid {

    }

    public interface CreateValid {

    }


    @Override
    public void copyFrom(Object source) {
        BeanUtils.copyProperties(source, this);
    }
}
