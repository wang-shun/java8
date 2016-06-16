package io.terminus.doctor.warehouse.search.material;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.terminus.search.core.ESClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Desc: 这些东西应该抽象出来
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Component
public class MaterialSearchESInitiator {

    @Autowired
    private ESClient esClient;

    @Autowired
    private MaterialSearchProperties materialSearchProperties;

    @PostConstruct
    public void init() throws Exception {
        if (!esClient.health()) {
            log.error("[materialSearchProperties search initiator] -> elasticsearch is not available ");
            return;
        }

        if (materialSearchProperties.getIndexName() == null || materialSearchProperties.getIndexType() == null) {
            log.error("[material search initiator] -> materialSearchProperties indexName or indexType is null");
            return;
        }

        // 1. 建立索引
        esClient.createIndexIfNotExists(materialSearchProperties.getIndexName());

        // 2. 建立索引类型
        String mappingPath = materialSearchProperties.getMappingPath();
        if(StringUtils.isBlank(mappingPath)) {
            mappingPath = materialSearchProperties.getIndexType() + "_mapping.json";
        }
        esClient.createMappingIfNotExists(
                materialSearchProperties.getIndexName(),
                materialSearchProperties.getIndexType(),
                Resources.toString(Resources.getResource(mappingPath), Charsets.UTF_8));
    }
}
