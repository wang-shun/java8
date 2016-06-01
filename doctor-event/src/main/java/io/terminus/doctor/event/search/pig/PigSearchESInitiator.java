package io.terminus.doctor.event.search.pig;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.terminus.search.core.ESClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Desc: 猪主搜索ES初始化
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Component
@Slf4j
public class PigSearchESInitiator {

    @Autowired
    private ESClient esClient;

    @Autowired
    private PigSearchProperties pigSearchProperties;

    @PostConstruct
    public void init() throws Exception {
        if (!esClient.health()) {
            log.error("[pig search initiator] -> elasticsearch is not available ");
            return;
        }
        if (pigSearchProperties.getIndexName() == null || pigSearchProperties.getIndexType() == null) {
            log.error("[pig search initiator] -> pigSearchProperties indexName or indexType is null");
            return;
        }
        // 1. 建立索引
        esClient.createIndexIfNotExists(pigSearchProperties.getIndexName());

        // 2. 建立索引类型
        String mappingPath = pigSearchProperties.getMappingPath();
        if(StringUtils.isBlank(mappingPath)) {
            mappingPath = pigSearchProperties.getIndexType() + "_mapping.json";
        }
        esClient.createMappingIfNotExists(
                pigSearchProperties.getIndexName(),
                pigSearchProperties.getIndexType(),
                Resources.toString(Resources.getResource(mappingPath), Charsets.UTF_8));
    }
}
