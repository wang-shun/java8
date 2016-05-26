package io.terminus.doctor.event.search.group;

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
public class IndexedGroupTaskAction {

    @Autowired
    private GroupSearchProperties groupSearchProperties;

    @Autowired
    private IndexTaskBuilder indexTaskBuilder;

    /**
     * 建立索引任务
     * @param indexedGroup    索引对象
     * @return
     */
    public IndexTask indexTask(IndexedGroup indexedGroup) {
        return indexTaskBuilder
                .indexName(groupSearchProperties.getIndexName())
                .indexType(groupSearchProperties.getIndexType())
                .indexAction(IndexAction.INDEX)
                .build(indexedGroup.getId(), indexedGroup);
    }

    /**
     * 删除索引任务
     * @param groupId     索引对象id
     * @return
     */
    public IndexTask deleteTask(Long groupId) {
        return indexTaskBuilder
                .indexName(groupSearchProperties.getIndexName())
                .indexType(groupSearchProperties.getIndexType())
                .indexAction(IndexAction.DELETE)
                .build(groupId, null);
    }
}
