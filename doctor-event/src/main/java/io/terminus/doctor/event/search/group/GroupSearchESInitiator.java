package io.terminus.doctor.event.search.group;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.terminus.search.core.ESClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Desc: 猪群主搜索ES初始化
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Component
@Slf4j
public class GroupSearchESInitiator {

    @Autowired
    private ESClient esClient;

    @Autowired
    private GroupSearchProperties groupSearchProperties;

    @PostConstruct
    public void init() throws Exception {
        if (!esClient.health()) {
            log.error("[group search initiator] -> elasticsearch is not available ");
            return;
        }
        // 1. 建立索引
        esClient.createIndexIfNotExists(groupSearchProperties.getIndexName());

        // 2. 建立索引类型
        String mappingPath = groupSearchProperties.getMappingPath();
        if(StringUtils.isBlank(mappingPath)) {
            mappingPath = groupSearchProperties.getIndexType() + "_mapping.json";
        }
        esClient.createMappingIfNotExists(
                groupSearchProperties.getIndexName(),
                groupSearchProperties.getIndexType(),
                Resources.toString(Resources.getResource(mappingPath), Charsets.UTF_8));
    }
}
