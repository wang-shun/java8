package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;

import javax.validation.constraints.NotNull;

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

    /**
     * 录入事件时录入客户
     * @param farmId        猪场id
     * @param farmName      猪场名称
     * @param customerId    客户id(根据此字段判断是否create)
     * @param customerName  客户名称
     * @return 是否成功
     */
    Response<Long> addCustomerWhenInput(@NotNull(message = "farmId.not.null") Long farmId,
                                        String farmName, Long customerId, String customerName,
                                        Long creatorId, String creatorName);
}
