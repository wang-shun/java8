package io.terminus.doctor.event.search.pig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Desc: 猪搜索属性参数
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@ConfigurationProperties(prefix = "esearch.pig")
@Data
public class PigSearchProperties {

    /**
     * 猪索引名称
     */
    private String indexName;

    /**
     * 猪索引类型
     */
    private String indexType;

    /**
     * 猪类型索引类型文件的路径, 初始化索引的mapping, 默认为 ${indexType}_mapping.json
     */
    private String mappingPath;

    /**
     * 全量dump, 多少天更新的数据
     */
    private Integer fullDumpRange = 3;

    /**
     * 每次批量处理的数量
     */
    private Integer batchSize = 100;
}
