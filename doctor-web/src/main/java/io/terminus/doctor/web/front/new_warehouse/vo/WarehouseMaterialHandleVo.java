package io.terminus.doctor.web.front.new_warehouse.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 物料变动报表
 * Created by sunbo@terminus.io on 2017/8/24.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseMaterialHandleVo {


    private String materialName;

    private Integer type;

    private Date handleDate;

    private BigDecimal quantity;

    private Long unitPrice;

    private String pigBarnName;

    private String pigGroupName;

    private String warehouseName;

}
