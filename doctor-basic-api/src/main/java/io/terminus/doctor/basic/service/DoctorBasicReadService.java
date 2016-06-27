package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorChangeType;
import io.terminus.doctor.basic.model.DoctorCustomer;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Desc: 基础数据读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */

public interface DoctorBasicReadService {

    /////////////////////////// 基础数据表 ///////////////////////////

    /**
     * 根据id查询基础数据表
     * @param basicId 主键id
     * @return 基础数据表
     */
    Response<DoctorBasic> findBasicById(@NotNull(message = "basicId.not.null") Long basicId);

    /**
     * 根据基础数据类型和输入码查询(非缓存)
     * @param type  类型
     * @see io.terminus.doctor.basic.model.DoctorBasic.Type
     * @param srm   输入码
     * @return 基础数据信息
     */
    Response<List<DoctorBasic>> findBasicByTypeAndSrm(@NotNull(message = "type.not.null") Integer type, @Nullable String srm);

    /**
     * 根据基础数据类型和输入码查询(缓存)
     * @param type  类型
     * @see io.terminus.doctor.basic.model.DoctorBasic.Type
     * @param srm   输入码
     * @return 基础数据信息
     */
    Response<List<DoctorBasic>> findBasicByTypeAndSrmWithCache(@NotNull(message = "type.not.null") Integer type, @Nullable String srm);

    //////////////////////////// 猪群变动相关 ////////////////////////////
    /**
     * 根据id查询变动原因表
     * @param changeReasonId 主键id
     * @return 变动类型表
     */
    Response<DoctorChangeReason> findChangeReasonById(@NotNull(message = "changeReasonId.not.null") Long changeReasonId);

    /**
     * 根据变动类型id查询变动原因表
     * @param changeTypeId 变动类型id
     * @return 变动原因列表
     */
    Response<List<DoctorChangeReason>> findChangeReasonByChangeTypeId(@NotNull(message = "changeTypeId.not.null") Long changeTypeId);

    /**
     * 根据id查询变动类型表
     * @param changeTypeId 主键id
     * @return 变动类型表
     */
    Response<DoctorChangeType> findChangeTypeById(@NotNull(message = "changeTypeId.not.null") Long changeTypeId);

    /**
     * 根据farmId查询变动类型表
     * @param farmId 猪场id
     * @return 变动类型表
     */
    Response<List<DoctorChangeType>> findChangeTypesByFarmId(@NotNull(message = "farmId.not.null") Long farmId);

    //////////////////////////// 猪场客户相关 ////////////////////////////
    /**
     * 根据id查询客户表
     * @param customerId 主键id
     * @return 客户表
     */
    Response<DoctorCustomer> findCustomerById(@NotNull(message = "customerId.not.null") Long customerId);

    /**
     * 根据farmId查询客户表
     * @param farmId 猪场id
     * @return 客户表
     */
    Response<List<DoctorCustomer>> findCustomersByFarmId(@NotNull(message = "farmId.not.null") Long farmId);
}
