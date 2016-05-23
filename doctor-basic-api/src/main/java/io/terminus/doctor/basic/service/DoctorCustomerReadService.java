package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorCustomer;

import java.util.List;

/**
 * Desc: 变动类型表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorCustomerReadService {

    /**
     * 根据id查询变动类型表
     * @param customerId 主键id
     * @return 变动类型表
     */
    Response<DoctorCustomer> findCustomerById(Long customerId);

    /**
     * 根据farmId查询变动类型表
     * @param farmId 猪场id
     * @return 变动类型表
     */
    Response<List<DoctorCustomer>> findCustomersByFarmId(Long farmId);
}
