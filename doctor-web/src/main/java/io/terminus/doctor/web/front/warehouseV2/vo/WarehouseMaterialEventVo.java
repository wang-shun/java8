package io.terminus.doctor.web.front.warehouseV2.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 仓库事件视图
 * Created by sunbo@terminus.io on 2017/8/25.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseMaterialEventVo {


    private Long id;

    private String materialName;

    private String warehouseName;

    private Date handleDate;

    private BigDecimal quantity;

    private Integer type;

    private String unit;

    private Long unitPrice;

    private Long amount;

    private String vendorName;

    private boolean allowDelete;

    private Long operatorId;

    private String operatorName;

}
