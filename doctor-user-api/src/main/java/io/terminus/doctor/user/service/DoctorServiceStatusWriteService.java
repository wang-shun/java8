package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorServiceStatus;

/**
 * Code generated by terminus code gen
 * Desc: 用户服务状态表写服务
 * Date: 2016-06-03
 */

public interface DoctorServiceStatusWriteService {

    /**
     * 创建DoctorServiceStatus
     * @param serviceStatus
     * @return 主键id
     */
    Response<Long> createServiceStatus(DoctorServiceStatus serviceStatus);

    /**
     * 更新DoctorServiceStatus
     * @param serviceStatus
     * @return 是否成功
     */
    Response<Boolean> updateServiceStatus(DoctorServiceStatus serviceStatus);

    /**
     * 根据主键id删除DoctorServiceStatus
     * @param serviceStatusId
     * @return 是否成功
     */
    Response<Boolean> deleteServiceStatusById(Long serviceStatusId);
}