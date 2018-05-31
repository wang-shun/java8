package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/24.
 */
@Data
public class AmountAndQuantityDto implements Serializable {

    private static final long serialVersionUID = 9069885794858381029L;

    private final BigDecimal amount;

    private final BigDecimal quantity;


    public AmountAndQuantityDto() {
        this.amount = new BigDecimal(0);
        this.quantity = new BigDecimal(0);
    }

    public AmountAndQuantityDto(BigDecimal amount, BigDecimal quantity) {
        this.amount = amount;
        this.quantity = quantity;
    }
}
