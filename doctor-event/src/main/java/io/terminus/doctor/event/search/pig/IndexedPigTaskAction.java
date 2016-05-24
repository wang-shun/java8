package io.terminus.doctor.event.search.pig;

import io.terminus.search.api.IndexTaskBuilder;
import io.terminus.search.api.model.IndexAction;
import io.terminus.search.api.model.IndexTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc: 创建任务Action类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Component
@Slf4j
public class IndexedPigTaskAction {

    @Autowired
    private PigSearchProperties pigSearchProperties;

    @Autowired
    private IndexTaskBuilder indexTaskBuilder;

    /**
     * 建立索引任务
     * @param indexedPig    索引对象
     * @return
     */
    public IndexTask indexTask(IndexedPig indexedPig) {
        return indexTaskBuilder
                .indexName(pigSearchProperties.getIndexName())
                .indexType(pigSearchProperties.getIndexType())
                .indexAction(IndexAction.INDEX)
                .build(indexedPig.getId(), indexedPig);
    }

    /**
     * 删除索引任务
     * @param pigId     索引对象id
     * @return
     */
    public IndexTask deleteTask(Long pigId) {
        return indexTaskBuilder
                .indexName(pigSearchProperties.getIndexName())
                .indexType(pigSearchProperties.getIndexType())
                .indexAction(IndexAction.DELETE)
                .build(pigId, null);
    }
}
