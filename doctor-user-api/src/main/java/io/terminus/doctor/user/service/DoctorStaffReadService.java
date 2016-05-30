package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorStaff;

import java.util.List;

public interface DoctorStaffReadService {

    Response<DoctorStaff> findStaffByUserId(Long userId);

    Response<List<DoctorStaff>> findStaffByOrgId(Long orgId);
}
