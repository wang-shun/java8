package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorGroupTrack;

/**
 * Desc: 猪群卡片明细表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupTrackWriteService {

    /**
     * 创建DoctorGroupTrack
     * @param groupTrack
     * @return 主键id
     */
    Response<Long> createGroupTrack(DoctorGroupTrack groupTrack);

    /**
     * 更新DoctorGroupTrack
     * @param groupTrack
     * @return 是否成功
     */
    Response<Boolean> updateGroupTrack(DoctorGroupTrack groupTrack);

    /**
     * 根据主键id删除DoctorGroupTrack
     * @param groupTrackId
     * @return 是否成功
     */
    Response<Boolean> deleteGroupTrackById(Long groupTrackId);
}