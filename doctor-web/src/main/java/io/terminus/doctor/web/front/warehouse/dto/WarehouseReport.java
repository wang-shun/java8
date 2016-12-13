package io.terminus.doctor.web.front.warehouse.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzenghui on 16/9/26.
 */
@Data
public class WarehouseReport implements Serializable {
    private static final long serialVersionUID = 8759233171464123607L;

    /**
     * 各仓库的报表
     */
    private List<Report> warehouseReports = new ArrayList<>();
    /**
     * 合计报表
     */
    private Report totalReport = new Report();

    @Data
    public static class Report implements Serializable{
        private static final long serialVersionUID = 5979734774482050633L;

        /**
         * 当前库存
         */
        private Double currentStock;
        /**
         * 当前库存的金额
         */
        private Double currentStockAmount;

        /**
         * 入库数量
         */
        private Double inCount;
        private Double inAmount;

        /**
         * 出库数量
         */
        private Double outCount;
        private Double outAmount;

        private Long warehouseId;
        private String warehouseName;
    }
}
