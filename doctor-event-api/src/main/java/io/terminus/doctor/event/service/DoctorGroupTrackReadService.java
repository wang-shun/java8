package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorGroupTrack;

/**
 * Desc: 猪群卡片明细表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupTrackReadService {

    /**
     * 根据id查询猪群卡片明细表
     * @param groupTrackId 主键id
     * @return 猪群卡片明细表
     */
    Response<DoctorGroupTrack> findGroupTrackById(Long groupTrackId);

}
