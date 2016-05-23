package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBreed;

/**
 * Desc: 品种表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorBreedReadService {

    /**
     * 根据id查询品种表
     * @param breedId 主键id
     * @return 品种表
     */
    Response<DoctorBreed> findBreedById(Long breedId);

}
