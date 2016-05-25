package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;

import java.util.List;

/**
 * Desc: 猪群想过读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupReadService {

    /**
     * 根据id查询猪群卡片表
     * @param groupId 主键id
     * @return 猪群卡片表
     */
    Response<DoctorGroup> findGroupById(Long groupId);

    /**
     * 根据farmId查询猪群卡片表
     * @param farmId 猪场id
     * @return 猪群卡片表
     */
    Response<List<DoctorGroup>> findGroupsByFarmId(Long farmId);

    /**
     * 根据id查询猪群事件表
     * @param groupEventId 主键id
     * @return 猪群事件表
     */
    Response<DoctorGroupEvent> findGroupEventById(Long groupEventId);

    /**
     * 根据farmId查询猪群事件表
     * @param farmId 猪场id
     * @return 猪群事件表
     */
    Response<List<DoctorGroupEvent>> findGroupEventsByFarmId(Long farmId);

    /**
     * 根据id查询猪群快照表
     * @param groupSnapshotId 主键id
     * @return 猪群快照表
     */
    Response<DoctorGroupSnapshot> findGroupSnapshotById(Long groupSnapshotId);

    /**
     * 根据id查询猪群卡片明细表
     * @param groupTrackId 主键id
     * @return 猪群卡片明细表
     */
    Response<DoctorGroupTrack> findGroupTrackById(Long groupTrackId);
}
