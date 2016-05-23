package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBreed;

/**
 * Desc: 品种表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorBreedWriteService {

    /**
     * 创建DoctorBreed
     * @param breed
     * @return 主键id
     */
    Response<Long> createBreed(DoctorBreed breed);

    /**
     * 更新DoctorBreed
     * @param breed
     * @return 是否成功
     */
    Response<Boolean> updateBreed(DoctorBreed breed);

    /**
     * 根据主键id删除DoctorBreed
     * @param breedId
     * @return 是否成功
     */
    Response<Boolean> deleteBreedById(Long breedId);
}