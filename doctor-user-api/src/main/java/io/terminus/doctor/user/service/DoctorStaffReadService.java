package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorStaff;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface DoctorStaffReadService {

    /**
     * 根据猪场id和用户id，唯一确定一个猪场员工
     */
    Response<DoctorStaff> findStaffByFarmIdAndUserId(@NotNull(message = "farmId.not.null") Long farmId,
                                                     @NotNull(message = "userId.not.null") Long userId);

    /**
     * 查询猪场员工
     * @param farmId  公司id
     * @param status 员工状态
     * @return 猪场员工列表
     */
    Response<List<DoctorStaff>> findStaffByFarmIdAndStatus(@NotNull(message = "farmId.not.null") Long farmId,
                                                           @Nullable Integer status);
}
