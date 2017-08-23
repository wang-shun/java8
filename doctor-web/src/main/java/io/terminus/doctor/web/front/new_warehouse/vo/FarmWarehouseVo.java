package io.terminus.doctor.web.front.new_warehouse.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/22.
 */
@Data
public class FarmWarehouseVo {

    private Long id;

    private String name;

    private Integer type;

    private String managerName;

    private BigDecimal stockQuantity = new BigDecimal(0);

    private long stockMoney;

    private BigDecimal inQuantity = new BigDecimal(0);

    private long inMoney;

    private BigDecimal outQuantity = new BigDecimal(0);

    private long outMoney;

    private BigDecimal transferOutQuantity = new BigDecimal(0);

    private long transferOutMoney;

    private BigDecimal transferInQuantity = new BigDecimal(0);

    private long transferInMoney;


}
