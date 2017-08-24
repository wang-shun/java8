package io.terminus.doctor.basic.dto;

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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmountAndQuantityDto implements Serializable{


    private static final long serialVersionUID = 9069885794858381029L;
    private long amount;

    private BigDecimal quantity;
}
