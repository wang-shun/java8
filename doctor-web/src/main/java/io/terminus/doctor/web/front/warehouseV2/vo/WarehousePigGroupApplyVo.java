package io.terminus.doctor.web.front.warehouseV2.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/8/24.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehousePigGroupApplyVo {

    private Long pigGroupId;
    private String pigGroupName;

    private Date openDate;

    private Date closeDate;

    private Date applyDate;

    private String pigBarnName;

    private String applyStaffName;


    private Integer materialType;

    private String materialName;

    private String unit;

    private String vendorName;

    private String code;

    private String specification;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;
}
