package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by sunbo@terminus.io on 2017/8/24.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseMaterialDto implements Serializable {

    private static final long serialVersionUID = 2616368052906927924L;

    @NotNull(message = "stock.material.id.null")
    private Long materialId;

    @NotNull(message = "stock.unit.name.null")
    private String unitName;
}
