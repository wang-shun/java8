package io.terminus.doctor.web.core.export.property;

import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Created by xjn on 16/12/29.
 */
@Data
@ConfigurationProperties(prefix = "export", locations = {"classpath:export.yml", "classpath*:application*.yml"})
public class ExportTables {
    private Map<String, ExportTable> tables = Maps.newHashMap();
}
