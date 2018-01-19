package io.terminus.doctor.event.reportBi.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2018/1/19.
 */
@Data
public class WarehouseReportTempResult {

    private Long orgId;
    private String orgName;
    private BigDecimal quantity;
    private BigDecimal amount;
    private Integer pigType;
    private Integer type;
    private Date date;
}
