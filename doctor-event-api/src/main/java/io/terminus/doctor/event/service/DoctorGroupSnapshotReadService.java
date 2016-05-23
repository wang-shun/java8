package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;

/**
 * Desc: 猪群快照表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupSnapshotReadService {

    /**
     * 根据id查询猪群快照表
     * @param groupSnapshotId 主键id
     * @return 猪群快照表
     */
    Response<DoctorGroupSnapshot> findGroupSnapshotById(Long groupSnapshotId);

}
