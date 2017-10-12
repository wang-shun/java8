package io.terminus.doctor.web.front.warehouseV2.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/10/11.
 */
@Data
public class WarehouseEventExportVo {

    private String materialName;

    private String providerFactoryName;

    private String unitName;

    private Long unitPrice;

    private String wareHouseName;

    private Date eventTime;

    private Long amount;

}
