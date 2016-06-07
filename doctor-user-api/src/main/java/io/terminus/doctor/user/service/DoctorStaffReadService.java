package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorStaff;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface DoctorStaffReadService {

    Response<DoctorStaff> findStaffByUserId(Long userId);

    Response<List<DoctorStaff>> findStaffByOrgId(Long orgId);

    /**
     * 根据id查询员工表
     * @param staffId  员工id
     * @return  员工
     */
    Response<DoctorStaff> findStaffById(@NotNull(message = "id.not.null") Long staffId);
}
