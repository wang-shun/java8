package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorBarn;

import java.util.List;

/**
 * Desc: 猪舍表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorBarnReadService {

    /**
     * 根据id查询猪舍表
     * @param barnId 主键id
     * @return 猪舍表
     */
    Response<DoctorBarn> findBarnById(Long barnId);

    /**
     * 根据farmId查询猪舍表
     * @param farmId 猪场id
     * @return 猪舍表
     */
    Response<List<DoctorBarn>> findBarnsByFarmId(Long farmId);
}
