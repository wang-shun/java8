package io.terminus.doctor.event.search.barn;

import io.terminus.common.model.Response;

/**
 * Desc: 猪舍ElasticSearch搜索写接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */

public interface BarnSearchWriteService {

    /**
     * 索引猪舍
     *
     * @param barnId 猪舍id
     */
    Response<Boolean> index(Long barnId);

    /**
     * 删除猪舍
     *
     * @param barnId 猪舍id
     */
    Response<Boolean> delete(Long barnId);

    /**
     * 索引或者删除猪舍
     *
     * @param barnId  猪舍id
     */
    Response<Boolean> update(Long barnId);
}
