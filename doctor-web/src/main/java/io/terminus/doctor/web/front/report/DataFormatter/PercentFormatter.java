package io.terminus.doctor.web.front.report.DataFormatter;

import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by xjn on 18/1/25.
 * email:xiaojiannan@terminus.io
 */
@Component
public class PercentFormatter implements DataFormatter{
    @Override
    public String format(@NotNull String value) {
        return String.format("%s%%", new BigDecimal(value).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_UP).toString());

    }
}
