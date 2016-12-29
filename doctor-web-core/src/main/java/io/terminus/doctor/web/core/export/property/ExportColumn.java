package io.terminus.doctor.web.core.export.property;

import lombok.Data;

/**
 * Created by xjn on 16/12/29.
 */
@Data
public class ExportColumn {
    private String name;
    private String display;
    private int width = 2000;
    private String format;
}
