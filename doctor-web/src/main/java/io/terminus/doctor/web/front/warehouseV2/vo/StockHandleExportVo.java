package io.terminus.doctor.web.front.warehouseV2.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/11/5.
 */
@Data
public class StockHandleExportVo {


    private String materialName;

    private String vendorName;

    private String materialCode;

    private String materialSpecification;

    private String unit;

    private BigDecimal quantity;

    private Double unitPrice;

    private Double amount;

    private String remark;
}
