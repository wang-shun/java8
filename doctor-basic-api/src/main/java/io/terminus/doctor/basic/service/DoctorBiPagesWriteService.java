package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.model.DoctorBiPages;

import io.terminus.common.model.Response;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-05 13:13:41
 * Created by [ your name ]
 */
public interface DoctorBiPagesWriteService {

    /**
     * 创建
     * @param doctorBiPages
     * @return Boolean
     */
    Response<Long> create(DoctorBiPages doctorBiPages);

    /**
     * 更新
     * @param doctorBiPages
     * @return Boolean
     */
    Response<Boolean> update(DoctorBiPages doctorBiPages);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}