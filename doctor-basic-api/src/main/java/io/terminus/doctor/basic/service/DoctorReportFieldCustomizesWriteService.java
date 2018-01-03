package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.dto.DoctorReportFieldDto;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorReportFieldCustomizes;

import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-27 17:11:01
 * Created by [ your name ]
 */
public interface DoctorReportFieldCustomizesWriteService {

    /**
     * 创建
     *
     * @param doctorReportFieldCustomizes
     * @return Boolean
     */
    Response<Long> create(DoctorReportFieldCustomizes doctorReportFieldCustomizes);

    /**
     * 更新
     *
     * @param doctorReportFieldCustomizes
     * @return Boolean
     */
    Response<Boolean> update(DoctorReportFieldCustomizes doctorReportFieldCustomizes);

    /**
     * 删除
     *
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);


    Response<Boolean> customize(Long farmId, DoctorReportFieldDto fieldDto);

    Response<Boolean> customize(Long farmId, List<DoctorReportFieldDto> fieldDto);

}