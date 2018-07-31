package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorOrg;

public interface DoctorOrgWriteService {

    Response<Long> createOrg(DoctorOrg org);

    Response<Boolean> updateOrg(DoctorOrg org);

    Response<Boolean> deleteOrg(Long orgId);

    Response<Boolean> updateOrgName(Long id,String name);

    Response<Boolean> updateBarnName(Long id,String name);

    Response<Boolean> updateFarmName(Long id,String name);

    Response<Boolean> updateGroupEventName(Long id,String name);

    Response<Boolean> updateGroupName(Long id,String name);

    Response<Boolean> updatePigEventsName(Long id,String name);

    Response<Boolean> updatePigScoreApplyName(Long id,String name);

    Response<Boolean> updatePigName(Long id,String name);

    Response<Boolean> updateGroupDaileName(Long id,String name);

    Response<Boolean> updatePigDailieName(Long id,String name);

    Response<DoctorOrg> findName(Long id);
}
