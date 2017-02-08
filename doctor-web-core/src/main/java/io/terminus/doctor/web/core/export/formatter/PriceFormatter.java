package io.terminus.doctor.web.core.export.formatter;

import io.terminus.doctor.web.core.export.ColumnFormatter;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

/**
 * DATE: 16/11/21 下午5:20 <br>
 * MAIL: zhanghecheng@terminus.io <br>
 * AUTHOR: zhanghecheng
 */
@Component
public class PriceFormatter implements ColumnFormatter {

    /**
     * 2位小数
     */
    private static final DecimalFormat DECIMAL_FMT_2 = new DecimalFormat("0.00");

    @Override
    public String format(Object price) {
        if (price == null) {
            return "";
        }
        if(price instanceof Number){
            return DECIMAL_FMT_2.format(((Number)price).doubleValue() / 100);
        }else{
            return "";
        }
    }
}
