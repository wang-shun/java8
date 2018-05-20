package io.terminus.doctor.web.front.warehouseV2.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/8/21.
 */
@Data
public class WarehouseReportVo {

    private String monthAndType;

    private List<WarehouseReportMonthDetail> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseReportMonthDetail {

        private String name;
        private BigDecimal amount;

    }


}
