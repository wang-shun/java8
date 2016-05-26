package io.terminus.doctor.event.search.barn;

import io.terminus.search.api.IndexTaskBuilder;
import io.terminus.search.api.model.IndexAction;
import io.terminus.search.api.model.IndexTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Component
public class IndexedBarnTaskAction {

    @Autowired
    private BarnSearchProperties barnSearchProperties;

    @Autowired
    private IndexTaskBuilder indexTaskBuilder;

    /**
     * 建立索引任务
     * @param indexedBarn    索引对象
     * @return
     */
    public IndexTask indexTask(IndexedBarn indexedBarn) {
        return indexTaskBuilder
                .indexName(barnSearchProperties.getIndexName())
                .indexType(barnSearchProperties.getIndexType())
                .indexAction(IndexAction.INDEX)
                .build(indexedBarn.getId(), indexedBarn);
    }

    /**
     * 删除索引任务
     * @param groupId     索引对象id
     * @return
     */
    public IndexTask deleteTask(Long groupId) {
        return indexTaskBuilder
                .indexName(barnSearchProperties.getIndexName())
                .indexType(barnSearchProperties.getIndexType())
                .indexAction(IndexAction.DELETE)
                .build(groupId, null);
    }
}
