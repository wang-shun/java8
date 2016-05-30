package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorOrg;

public interface DoctorOrgWriteService {

    Response<Long> createOrg(DoctorOrg org);

    Response<Boolean> updateOrg(DoctorOrg org);

    Response<Boolean> deleteOrg(Long orgId);

}
