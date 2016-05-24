package io.terminus.doctor.event.search.group;

import io.terminus.common.model.Response;

/**
 * Desc: 猪群 搜索写Service
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
public interface GroupSearchWriteService {

    /**
     * 索引猪群
     *
     * @param groupId 猪群id
     */
    Response<Boolean> index(Long groupId);

    /**
     * 删除猪群
     *
     * @param groupId 猪群id
     */
    Response<Boolean> delete(Long groupId);

    /**
     * 索引或者删除猪群
     *
     * @param groupId  猪群id
     */
    Response<Boolean> update(Long groupId);

}
