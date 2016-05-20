package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;

/**
 * Desc: 猪群快照表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupSnapshotWriteService {

    /**
     * 创建DoctorGroupSnapshot
     * @param groupSnapshot
     * @return 主键id
     */
    Response<Long> createGroupSnapshot(DoctorGroupSnapshot groupSnapshot);

    /**
     * 更新DoctorGroupSnapshot
     * @param groupSnapshot
     * @return 是否成功
     */
    Response<Boolean> updateGroupSnapshot(DoctorGroupSnapshot groupSnapshot);

    /**
     * 根据主键id删除DoctorGroupSnapshot
     * @param groupSnapshotId
     * @return 是否成功
     */
    Response<Boolean> deleteGroupSnapshotById(Long groupSnapshotId);
}