package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorDemo;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-03-29 10:49:19
 * Created by [ your name ]
 */
public interface DoctorDemoWriteService {

    /**
     * 创建
     * @param doctorDemo
     * @return Boolean
     */
    Response<Long> create(DoctorDemo doctorDemo);

    /**
     * 更新
     * @param doctorDemo
     * @return Boolean
     */
    Response<Boolean> update(DoctorDemo doctorDemo);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}