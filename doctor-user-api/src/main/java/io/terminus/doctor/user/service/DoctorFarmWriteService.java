package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;

/**
 * Desc: 猪场信息写接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorFarmWriteService {

    Response<Boolean> createOrg(DoctorOrg org);

    Response<Boolean> updateOrg(DoctorOrg org);

    Response<Boolean> deleteOrg(Long orgId);

    Response<Boolean> createFarm(DoctorFarm farm);

    Response<Boolean> updateFarm(DoctorFarm farm);

    Response<Boolean> deleteFarm(Long farmId);
}
