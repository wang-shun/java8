package io.terminus.doctor.web.front.new_warehouse.vo;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 仓库事件视图
 * Created by sunbo@terminus.io on 2017/8/25.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseMaterialEventVo {


    private Long id;

    private String materialName;

    private String warehouseName;

    private Date handleDate;

    private BigDecimal quantity;

    private Integer type;

    private String unit;

    private Long unitPrice;

    private Long amount;

    private String vendorName;

}
