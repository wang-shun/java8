package io.terminus.doctor.web.front.new_warehouse.vo;

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

    @JsonView(WarehouseView.class)
    private Long id;

    @JsonView(WarehouseView.class)
    private String name;

    @JsonView(WarehouseWithOutStatisticsView.class)
    private Integer type;

    @JsonView(WarehouseView.class)
    private String managerName;

    @JsonView(WarehouseView.class)
    private Long managerId;

    @JsonView(WarehouseWithOutStatisticsView.class)
    private Date lastApplyDate;

    @JsonView(WarehouseWithOutStatisticsView.class)
    private BigDecimal balanceQuantity;


    public interface WarehouseView extends WarehouseWithOutStatisticsView {

    }

    public interface WarehouseWithOutStatisticsView {

    }
}
