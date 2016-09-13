package io.terminus.doctor.event.search.barn;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Desc: 猪舍搜索参数
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@ConfigurationProperties(prefix = "esearch.barn")
@Data
public class BarnSearchProperties {

    /**
     * 猪舍索引名称
     */
    private String indexName;

    /**
     * 猪舍索引类型
     */
    private String indexType;

    /**
     * 猪舍类型索引类型文件的路径, 初始化索引的mapping, 默认为 ${indexType}_mapping.json
     */
    private String mappingPath;

    /**
     * 全量dump, 多少天更新的数据
     */
    private Integer fullDumpRange = 30;

    /**
     * 每次批量处理的数量
     */
    private Integer batchSize = 100;
}
