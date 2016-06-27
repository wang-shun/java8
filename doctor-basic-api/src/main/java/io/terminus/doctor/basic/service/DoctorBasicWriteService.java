package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorChangeType;
import io.terminus.doctor.basic.model.DoctorCustomer;

/**
 * Desc: 基础数据写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */

public interface DoctorBasicWriteService {

    /**
     * 创建DoctorBasic
     * @param basic 基础数据表实例
     * @return 主键id
     */
    Response<Long> createBasic(DoctorBasic basic);

    /**
     * 更新DoctorBasic
     * @param basic 基础数据表实例
     * @return 是否成功
     */
    Response<Boolean> updateBasic(DoctorBasic basic);

    /**
     * 根据主键id删除DoctorBasic
     * @param basicId 基础数据表实例主键id
     * @return 是否成功
     */
    Response<Boolean> deleteBasicById(Long basicId);

    /**
     * 创建DoctorChangeReason
     * @return 主键id
     */
    Response<Long> createChangeReason(DoctorChangeReason changeReason);

    /**
     * 更新DoctorChangeReason
     * @return 是否成功
     */
    Response<Boolean> updateChangeReason(DoctorChangeReason changeReason);

    /**
     * 根据主键id删除DoctorChangeReason
     * @return 是否成功
     */
    Response<Boolean> deleteChangeReasonById(Long changeReasonId);

    /**
     * 创建DoctorChangeType
     * @return 主键id
     */
    Response<Long> createChangeType(DoctorChangeType changeType);

    /**
     * 更新DoctorChangeType
     * @return 是否成功
     */
    Response<Boolean> updateChangeType(DoctorChangeType changeType);

    /**
     * 根据主键id删除DoctorChangeType
     * @return 是否成功
     */
    Response<Boolean> deleteChangeTypeById(Long changeTypeId);

    /**
     * 创建DoctorCustomer
     * @return 主键id
     */
    Response<Long> createCustomer(DoctorCustomer customer);

    /**
     * 更新DoctorCustomer
     * @return 是否成功
     */
    Response<Boolean> updateCustomer(DoctorCustomer customer);

    /**
     * 根据主键id删除DoctorCustomer
     * @return 是否成功
     */
    Response<Boolean> deleteCustomerById(Long customerId);
}
