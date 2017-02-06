package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorStaff;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface DoctorStaffReadService {

    Response<DoctorStaff> findStaffByUserId(Long userId);

    /**
     * 查询员工
     * @param orgId  公司id
     * @param status 员工状态
     * @return
     */
    Response<List<DoctorStaff>> findStaffByOrgIdAndStatus(@NotNull(message = "orgId.not.null") Long orgId,
                                                          @Nullable Integer status);

    /**
     * 根据id查询员工表
     * @param staffId  员工id
     * @return  员工
     */
    Response<DoctorStaff> findStaffById(@NotNull(message = "id.not.null") Long staffId);
}
