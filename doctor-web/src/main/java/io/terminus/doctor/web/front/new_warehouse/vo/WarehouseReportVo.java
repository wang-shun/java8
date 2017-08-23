package io.terminus.doctor.web.front.new_warehouse.vo;

import lombok.Data;

import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/8/21.
 */
@Data
public class WarehouseReportVo {

    private String warehouseName;

    private List<WarehouseReportMonthDetail> details;

    @Data
    public static class WarehouseReportMonthDetail {

        private String month;

        private Long in;
        private Long out;
        private Long balance;

    }


}
