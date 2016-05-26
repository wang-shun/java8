package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBreed;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorChangeType;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.model.DoctorDisease;
import io.terminus.doctor.basic.model.DoctorGenetic;
import io.terminus.doctor.basic.model.DoctorUnit;

import javax.validation.constraints.NotNull;

/**
 * Desc: 基础数据写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */

public interface DoctorBasicWriteService {

    /**
     * 创建猪场时, 初始化一些基础数据
     * @param farmId 猪场id
     * @return 是否成功
     */
    Response<Boolean> initFarmBasic(@NotNull(message = "farmId.not.null") Long farmId);

    /**
     * 创建DoctorBreed
     * @return 主键id
     */
    Response<Long> createBreed(DoctorBreed breed);

    /**
     * 更新DoctorBreed
     * @return 是否成功
     */
    Response<Boolean> updateBreed(DoctorBreed breed);

    /**
     * 根据主键id删除DoctorBreed
     * @return 是否成功
     */
    Response<Boolean> deleteBreedById(Long breedId);

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

    /**
     * 创建DoctorDisease
     * @return 主键id
     */
    Response<Long> createDisease(DoctorDisease disease);

    /**
     * 更新DoctorDisease
     * @return 是否成功
     */
    Response<Boolean> updateDisease(DoctorDisease disease);

    /**
     * 根据主键id删除DoctorDisease
     * @return 是否成功
     */
    Response<Boolean> deleteDiseaseById(Long diseaseId);

    /**
     * 创建DoctorGenetic
     * @return 主键id
     */
    Response<Long> createGenetic(DoctorGenetic genetic);

    /**
     * 更新DoctorGenetic
     * @return 是否成功
     */
    Response<Boolean> updateGenetic(DoctorGenetic genetic);

    /**
     * 根据主键id删除DoctorGenetic
     * @return 是否成功
     */
    Response<Boolean> deleteGeneticById(Long geneticId);

    /**
     * 创建DoctorUnit
     * @return 主键id
     */
    Response<Long> createUnit(DoctorUnit unit);

    /**
     * 更新DoctorUnit
     * @return 是否成功
     */
    Response<Boolean> updateUnit(DoctorUnit unit);

    /**
     * 根据主键id删除DoctorUnit
     * @return 是否成功
     */
    Response<Boolean> deleteUnitById(Long unitId);
}
