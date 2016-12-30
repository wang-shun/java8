package io.terminus.doctor.web.core.export;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by xjn on 16/12/29.
 */
@Slf4j
@Component
public class ColumnFormatterRegistry {
    @Autowired
    private ApplicationContext applicationContext;

    private Map<String, ColumnFormatter> formatters = Maps.newHashMap();

    private final String DEFAULT = "default";

    @PostConstruct
    public void registerFormatters(){
        formatters.put(DEFAULT, new DefaultColumnFormatter());
        formatters.putAll(applicationContext.getBeansOfType(ColumnFormatter.class));
    }

    public void register(String name , ColumnFormatter formatter) {
        formatters.put(name, formatter);
    }

    public ColumnFormatter getFormatter(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return new DefaultColumnFormatter();
        }
        ColumnFormatter formatter = formatters.get(name);
        if (formatter == null) {
            log.warn("no column formatter (name={}) configured, return defaultFormatter", name);
            return new DefaultColumnFormatter();
        }
        return formatter;
    }

    /**
     * 默认的列类型转换器
     */
    class DefaultColumnFormatter implements ColumnFormatter {

        @Override
        public String format(Object value) {
            return value == null? "" : value.toString() ;
        }
    }
}
