package io.terminus.doctor.web.front.warehouseV2.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/24.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseStockVo {

    private Long materialId;

    private String materialName;

    private BigDecimal quantity;

    private String unit;

    private Long unitId;

    private String vendorName;

    private String code;

    private String specification;
}
