package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBreed;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorChangeType;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.model.DoctorDisease;
import io.terminus.doctor.basic.model.DoctorGenetic;
import io.terminus.doctor.basic.model.DoctorUnit;

import java.util.List;

/**
 * Desc: 基础数据读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */

public interface DoctorBasicReadService {

    /**
     * 根据id查询品种表
     * @param breedId 主键id
     * @return 品种表
     */
    Response<DoctorBreed> findBreedById(Long breedId);

    /**
     * 根据id查询变动类型表
     * @param changeReasonId 主键id
     * @return 变动类型表
     */
    Response<DoctorChangeReason> findChangeReasonById(Long changeReasonId);

    /**
     * 根据id查询变动类型表
     * @param changeTypeId 主键id
     * @return 变动类型表
     */
    Response<DoctorChangeType> findChangeTypeById(Long changeTypeId);

    /**
     * 根据farmId查询变动类型表
     * @param farmId 猪场id
     * @return 变动类型表
     */
    Response<List<DoctorChangeType>> findChangeTypesByFarmId(Long farmId);

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

    /**
     * 根据id查询变动类型表
     * @param diseaseId 主键id
     * @return 变动类型表
     */
    Response<DoctorDisease> findDiseaseById(Long diseaseId);

    /**
     * 根据farmId查询变动类型表
     * @param farmId 猪场id
     * @return 变动类型表
     */
    Response<List<DoctorDisease>> findDiseasesByFarmId(Long farmId);

    /**
     * 根据id查询品系表
     * @param geneticId 主键id
     * @return 品系表
     */
    Response<DoctorGenetic> findGeneticById(Long geneticId);

    /**
     * 根据id查询计量单位表
     * @param unitId 主键id
     * @return 计量单位表
     */
    Response<DoctorUnit> findUnitById(Long unitId);
}
