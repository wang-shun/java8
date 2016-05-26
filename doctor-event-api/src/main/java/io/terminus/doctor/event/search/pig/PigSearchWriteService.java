package io.terminus.doctor.event.search.pig;

import io.terminus.common.model.Response;

/**
 * Desc: 猪 搜索写Service
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
public interface PigSearchWriteService {

    /**
     * 索引猪
     *
     * @param pigId 猪id
     */
    Response<Boolean> index(Long pigId);

    /**
     * 删除猪
     *
     * @param pigId 猪id
     */
    Response<Boolean> delete(Long pigId);

    /**
     * 索引或者删除猪
     *
     * @param pigId  猪id
     */
    Response<Boolean> update(Long pigId);

}
