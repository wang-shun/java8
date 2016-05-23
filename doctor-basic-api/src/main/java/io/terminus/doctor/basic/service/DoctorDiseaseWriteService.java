package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorDisease;

/**
 * Desc: 变动类型表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorDiseaseWriteService {

    /**
     * 创建DoctorDisease
     * @param disease
     * @return 主键id
     */
    Response<Long> createDisease(DoctorDisease disease);

    /**
     * 更新DoctorDisease
     * @param disease
     * @return 是否成功
     */
    Response<Boolean> updateDisease(DoctorDisease disease);

    /**
     * 根据主键id删除DoctorDisease
     * @param diseaseId
     * @return 是否成功
     */
    Response<Boolean> deleteDiseaseById(Long diseaseId);
}