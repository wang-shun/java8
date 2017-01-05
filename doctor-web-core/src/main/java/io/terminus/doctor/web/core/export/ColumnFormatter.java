package io.terminus.doctor.web.core.export;

/**
 * Created by xjn on 16/12/29.
 */
public interface ColumnFormatter {
    /**
     * 转换成字符类型
     */
    public String format(Object value);
}
