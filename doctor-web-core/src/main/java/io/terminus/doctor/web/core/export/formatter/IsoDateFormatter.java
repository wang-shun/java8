package io.terminus.doctor.web.core.export.formatter;

import io.terminus.doctor.web.core.export.ColumnFormatter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * DATE: 16/11/21 下午5:12 <br>
 * MAIL: zhanghecheng@terminus.io <br>
 * AUTHOR: zhanghecheng
 */
@Component
public class IsoDateFormatter implements ColumnFormatter {

    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(Object value) {
        if(value==null){
            return "";
        }
        if(value instanceof Date) {
            return DFT.print(new DateTime(value));
        }else if (value instanceof String){
            return (String)value;
        }else if(value instanceof Long){
            return DFT.print(new DateTime((long)value));
        }else{
            return "";
        }
    }
}
