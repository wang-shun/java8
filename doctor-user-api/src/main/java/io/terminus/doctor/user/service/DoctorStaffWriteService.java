package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorStaff;

public interface DoctorStaffWriteService {

    /**
     * 创建staff
     * @param staff DoctorStaff  model类
     * @return 新增数据的id
     */
    Response<Long> createDoctorStaff(DoctorStaff staff);

    /**
     * 更新staff
     * @param staff DoctorStaff  model类
     * @return
     */
    Response<Boolean> updateDoctorStaff(DoctorStaff staff);

    /**
     * 删除staff
     * @param staffId  DoctorStaff 的 id
     * @return
     */
    Response<Boolean> deleteDoctorStaff(Long staffId);
}
