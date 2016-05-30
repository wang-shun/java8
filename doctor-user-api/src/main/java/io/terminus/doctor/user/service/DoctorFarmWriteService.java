package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorFarm;

/**
 * Desc: 猪场信息写接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorFarmWriteService {

    Response<Long> createFarm(DoctorFarm farm);

    Response<Boolean> updateFarm(DoctorFarm farm);

    Response<Boolean> deleteFarm(Long farmId);
}
