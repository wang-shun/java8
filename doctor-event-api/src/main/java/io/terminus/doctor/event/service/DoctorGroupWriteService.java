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
     * 新建猪群
     * @return 主键id
     */
    Response<Long> createNewGroup(DoctorGroup group);

}