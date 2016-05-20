package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorCustomer;

/**
 * Desc: 变动类型表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorCustomerWriteService {

    /**
     * 创建DoctorCustomer
     * @param customer
     * @return 主键id
     */
    Response<Long> createCustomer(DoctorCustomer customer);

    /**
     * 更新DoctorCustomer
     * @param customer
     * @return 是否成功
     */
    Response<Boolean> updateCustomer(DoctorCustomer customer);

    /**
     * 根据主键id删除DoctorCustomer
     * @param customerId
     * @return 是否成功
     */
    Response<Boolean> deleteCustomerById(Long customerId);
}