package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorFarmBasic;

/**
 * Desc: 猪场基础数据关联表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-11-21
 */
public interface DoctorFarmBasicWriteService {

    /**
     * 创建DoctorFarmBasic
     * @param farmBasic
     * @return 主键id
     */
    Response<Long> createFarmBasic(DoctorFarmBasic farmBasic);

    /**
     * 更新DoctorFarmBasic
     * @param farmBasic
     * @return 是否成功
     */
    Response<Boolean> updateFarmBasic(DoctorFarmBasic farmBasic);

    /**
     * 根据主键id删除DoctorFarmBasic
     * @param farmBasicId
     * @return 是否成功
     */
    Response<Boolean> deleteFarmBasicById(Long farmBasicId);
}