package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorGenetic;

/**
 * Desc: 品系表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGeneticWriteService {

    /**
     * 创建DoctorGenetic
     * @param genetic
     * @return 主键id
     */
    Response<Long> createGenetic(DoctorGenetic genetic);

    /**
     * 更新DoctorGenetic
     * @param genetic
     * @return 是否成功
     */
    Response<Boolean> updateGenetic(DoctorGenetic genetic);

    /**
     * 根据主键id删除DoctorGenetic
     * @param geneticId
     * @return 是否成功
     */
    Response<Boolean> deleteGeneticById(Long geneticId);
}