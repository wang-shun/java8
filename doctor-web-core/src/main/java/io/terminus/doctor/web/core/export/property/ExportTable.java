package io.terminus.doctor.web.core.export.property;

import com.google.api.client.util.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by xjn on 16/12/29.
 */
@Data
public class ExportTable {
    private String name;
    private String display;
    private List<ExportColumn> columns = Lists.newArrayList();
}
