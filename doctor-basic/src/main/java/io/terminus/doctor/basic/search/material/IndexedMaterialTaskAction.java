package io.terminus.doctor.basic.search.material;

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
 * Date: 16/6/16
 */
@Component
@Slf4j
public class IndexedMaterialTaskAction {

    @Autowired
    private MaterialSearchProperties materialSearchProperties;

    @Autowired
    private IndexTaskBuilder indexTaskBuilder;

    /**
     * 建立索引任务
     * @param indexedMaterial    索引对象
     * @return
     */
    public IndexTask indexTask(IndexedMaterial indexedMaterial) {
        return indexTaskBuilder
                .indexName(materialSearchProperties.getIndexName())
                .indexType(materialSearchProperties.getIndexType())
                .indexAction(IndexAction.INDEX)
                .build(indexedMaterial.getId(), indexedMaterial);
    }

    /**
     * 删除索引任务
     * @param materialId     索引对象id
     * @return
     */
    public IndexTask deleteTask(Long materialId) {
        return indexTaskBuilder
                .indexName(materialSearchProperties.getIndexName())
                .indexType(materialSearchProperties.getIndexType())
                .indexAction(IndexAction.DELETE)
                .build(materialId, null);
    }
}
