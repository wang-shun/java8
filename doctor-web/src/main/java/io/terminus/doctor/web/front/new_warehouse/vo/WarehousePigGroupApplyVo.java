package io.terminus.doctor.web.front.new_warehouse.vo;

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

    private Date createDate;

    private Date closeDate;

    private String pigBarnName;

    private String applyStaffName;


    private Integer materialType;

    private String materialName;

    private String unit;

    private BigDecimal quantity;

    private Long unitPrice;

    private Long amount;
}
