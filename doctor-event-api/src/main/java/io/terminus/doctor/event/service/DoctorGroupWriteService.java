package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;

/**
 * Desc: 猪群卡片表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupWriteService {

    /**
     * 创建DoctorGroup
     * @return 主键id
     */
    Response<Long> createGroup(DoctorGroup group);

    /**
     * 更新DoctorGroup
     * @return 是否成功
     */
    Response<Boolean> updateGroup(DoctorGroup group);

    /**
     * 根据主键id删除DoctorGroup
     * @return 是否成功
     */
    Response<Boolean> deleteGroupById(Long groupId);

    /**
     * 创建DoctorGroupEvent
     * @return 主键id
     */
    Response<Long> createGroupEvent(DoctorGroupEvent groupEvent);

    /**
     * 更新DoctorGroupEvent
     * @return 是否成功
     */
    Response<Boolean> updateGroupEvent(DoctorGroupEvent groupEvent);

    /**
     * 根据主键id删除DoctorGroupEvent
     * @return 是否成功
     */
    Response<Boolean> deleteGroupEventById(Long groupEventId);

    /**
     * 创建DoctorGroupSnapshot
     * @return 主键id
     */
    Response<Long> createGroupSnapshot(DoctorGroupSnapshot groupSnapshot);

    /**
     * 更新DoctorGroupSnapshot
     * @return 是否成功
     */
    Response<Boolean> updateGroupSnapshot(DoctorGroupSnapshot groupSnapshot);

    /**
     * 根据主键id删除DoctorGroupSnapshot
     * @return 是否成功
     */
    Response<Boolean> deleteGroupSnapshotById(Long groupSnapshotId);

    /**
     * 创建DoctorGroupTrack
     * @return 主键id
     */
    Response<Long> createGroupTrack(DoctorGroupTrack groupTrack);

    /**
     * 更新DoctorGroupTrack
     * @return 是否成功
     */
    Response<Boolean> updateGroupTrack(DoctorGroupTrack groupTrack);

    /**
     * 根据主键id删除DoctorGroupTrack
     * @return 是否成功
     */
    Response<Boolean> deleteGroupTrackById(Long groupTrackId);
}