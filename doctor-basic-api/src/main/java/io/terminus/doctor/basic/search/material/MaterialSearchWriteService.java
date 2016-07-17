package io.terminus.doctor.basic.search.material;

import io.terminus.common.model.Response;

/**
 * Desc: 物料ElasticSearch搜索写接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */

public interface MaterialSearchWriteService {

    /**
     * 索引物料
     *
     * @param materialId 物料id
     */
    Response<Boolean> index(Long materialId);

    /**
     * 删除物料
     *
     * @param materialId 物料id
     */
    Response<Boolean> delete(Long materialId);

    /**
     * 索引或者删除物料
     *
     * @param materialId  物料id
     */
    Response<Boolean> update(Long materialId);
}
