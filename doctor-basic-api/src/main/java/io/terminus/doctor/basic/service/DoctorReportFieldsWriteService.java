package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.model.DoctorReportFields;

import io.terminus.common.model.Response;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-27 16:19:39
 * Created by [ your name ]
 */
public interface DoctorReportFieldsWriteService {

    /**
     * 创建
     * @param doctorReportFields
     * @return Boolean
     */
    Response<Long> create(DoctorReportFields doctorReportFields);

    /**
     * 更新
     * @param doctorReportFields
     * @return Boolean
     */
    Response<Boolean> update(DoctorReportFields doctorReportFields);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}