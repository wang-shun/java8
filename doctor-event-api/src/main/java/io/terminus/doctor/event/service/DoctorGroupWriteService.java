package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorGroup;

/**
 * Desc: 猪群卡片表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupWriteService {

    /**
     * 创建DoctorGroup
     * @param group
     * @return 主键id
     */
    Response<Long> createGroup(DoctorGroup group);

    /**
     * 更新DoctorGroup
     * @param group
     * @return 是否成功
     */
    Response<Boolean> updateGroup(DoctorGroup group);

    /**
     * 根据主键id删除DoctorGroup
     * @param groupId
     * @return 是否成功
     */
    Response<Boolean> deleteGroupById(Long groupId);
}