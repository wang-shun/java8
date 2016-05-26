package io.terminus.doctor.event.search.barn;

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
public class BarnSearchESInitiator {

    @Autowired
    private ESClient esClient;

    @Autowired
    private BarnSearchProperties barnSearchProperties;

    @PostConstruct
    public void init() throws Exception {
        if (!esClient.health()) {
            log.error("[barn search initiator] -> elasticsearch is not available ");
            return;
        }
        // 1. 建立索引
        esClient.createIndexIfNotExists(barnSearchProperties.getIndexName());

        // 2. 建立索引类型
        String mappingPath = barnSearchProperties.getMappingPath();
        if(StringUtils.isBlank(mappingPath)) {
            mappingPath = barnSearchProperties.getIndexType() + "_mapping.json";
        }
        esClient.createMappingIfNotExists(
                barnSearchProperties.getIndexName(),
                barnSearchProperties.getIndexType(),
                Resources.toString(Resources.getResource(mappingPath), Charsets.UTF_8));
    }
}
