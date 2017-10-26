package io.terminus.doctor.basic.model.warehouseV2;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-26 15:57:14
 * Created by [ your name ]
 */
@Data
public class DoctorWarehouseVendor implements Serializable {

    private static final long serialVersionUID = -5551900989292774937L;

    /**
     * 自增主键
     */
    @NotNull(message = "warehouse.vendor.id.null", groups = UpdateValid.class)
    private Long id;

    /**
     * 供应商名称
     */
    @NotBlank(message = "warehouse.vendor.name.blank", groups = CreateValid.class)
    private String name;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;


    public static interface CreateValid {

    }

    public static interface UpdateValid extends CreateValid {

    }

}