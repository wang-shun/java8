package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBreed;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorChangeType;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.model.DoctorDisease;
import io.terminus.doctor.basic.model.DoctorFosterReason;
import io.terminus.doctor.basic.model.DoctorGenetic;
import io.terminus.doctor.basic.model.DoctorUnit;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Desc: 基础数据读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */

public interface DoctorBasicReadService {

    //////////////////////////// 品种品系相关 ////////////////////////////
    /**
     * 根据id查询品种表
     * @param breedId 主键id
     * @return 品种表
     */
    Response<DoctorBreed> findBreedById(@NotNull(message = "breedId.not.null") Long breedId);

    /**
     * 查询所有品种
     * @return 品种列表
     */
    Response<List<DoctorBreed>> findAllBreeds();

    /**
     * 根据id查询品系表
     * @param geneticId 主键id
     * @return 品系表
     */
    Response<DoctorGenetic> findGeneticById(@NotNull(message = "geneticId.not.null") Long geneticId);

    /**
     * 查询所有品系
     * @return 品系列表
     */
    Response<List<DoctorGenetic>> findAllGenetics();

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

    //////////////////////////// 疾病防疫相关 ////////////////////////////
    /**
     * 根据id查询疾病表
     * @param diseaseId 主键id
     * @return 疾病表
     */
    Response<DoctorDisease> findDiseaseById(@NotNull(message = "diseaseId.not.null") Long diseaseId);

    /**
     * 根据farmId查询疾病表
     * @param farmId 猪场id
     * @return 疾病表
     */
    Response<List<DoctorDisease>> findDiseasesByFarmId(@NotNull(message = "farmId.not.null") Long farmId);

    //////////////////////////// 计量单位相关 ////////////////////////////
    /**
     * 根据id查询计量单位表
     * @param unitId 主键id
     * @return 计量单位表
     */
    Response<DoctorUnit> findUnitById(@NotNull(message = "unitId.not.null") Long unitId);

    /**
     * 查询所有计量单位
     * @return 计量单位列表
     */
    Response<List<DoctorUnit>> findAllUnits();

    //////////////////////////// 寄养原因 //////////////////////////////
    /**
     * 根据id查询寄养原因表
     * @param fosterReasonId 主键id
     * @return 寄养原因表
     */
    Response<DoctorFosterReason> findFosterReasonById(@NotNull(message = "fosterReasonId.not.null") Long fosterReasonId);

    /**
     * 根据farmId查询寄养原因表
     * @param farmId 猪场id
     * @return 寄养原因表
     */
    Response<List<DoctorFosterReason>> findFosterReasonsByFarmId(@NotNull(message = "farmId.not.null") Long farmId);
}
