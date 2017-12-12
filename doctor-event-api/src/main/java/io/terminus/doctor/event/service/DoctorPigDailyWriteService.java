package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorPigDaily;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-12 17:33:52
 * Created by [ your name ]
 */
public interface DoctorPigDailyWriteService {

    /**
     * 创建
     * @param doctorPigDaily
     * @return Boolean
     */
    Response<Long> create(DoctorPigDaily doctorPigDaily);

    /**
     * 更新
     * @param doctorPigDaily
     * @return Boolean
     */
    Response<Boolean> update(DoctorPigDaily doctorPigDaily);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}