package io.terminus.doctor.web.front.warehouseV2.vo;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/8/22.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseVo {

    @JsonView(WarehouseWithOutStatisticsView.class)
    private Long farmId;

    @JsonView(WarehouseWithOutStatisticsView.class)
    private String farmName;

    @JsonView(WarehouseWithOutStatisticsView.class)
    private Long id;

    @JsonView(WarehouseWithOutStatisticsView.class)
    private String name;

    @JsonView({WarehouseWithOutStatisticsView.class, WarehouseStatisticsView.class})
    private Integer type;

    @JsonView(WarehouseWithOutStatisticsView.class)
    private String managerName;

    @JsonView(WarehouseWithOutStatisticsView.class)
    private Long managerId;

    @JsonView(WarehouseWithOutStatisticsView.class)
    private String address;

    @JsonView({WarehouseView.class, WarehouseStatisticsView.class})
    private Date lastApplyDate;

    @JsonView({WarehouseView.class, WarehouseStatisticsView.class})
    private BigDecimal balanceQuantity;


    public interface WarehouseView extends WarehouseWithOutStatisticsView {

    }

    public interface WarehouseWithOutStatisticsView {

    }

    public interface WarehouseStatisticsView {

    }
}
