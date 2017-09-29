package io.terminus.doctor.web.front.warehouseV2.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * Created by sunbo@terminus.io on 2017/8/8.
 */
@Data
public class WarehouseDto {

    private Long id;

    @NotNull(message = "warehouse.type.null")
    private Integer type;

    @NotNull(message = "farm.id.null")
    private Long farmId;

    @NotBlank(message = "warehouse.name.blank")
    private String name;

    @NotNull(message = "warehouse.manager.id.null")
    private Long managerId;

    private String address;


}
