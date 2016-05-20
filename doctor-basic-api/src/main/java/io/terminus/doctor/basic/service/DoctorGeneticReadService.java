package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorGenetic;

/**
 * Desc: 品系表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGeneticReadService {

    /**
     * 根据id查询品系表
     * @param geneticId 主键id
     * @return 品系表
     */
    Response<DoctorGenetic> findGeneticById(Long geneticId);

}
