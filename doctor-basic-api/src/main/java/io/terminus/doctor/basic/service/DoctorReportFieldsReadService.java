package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.dto.DoctorReportFieldDto;
import io.terminus.doctor.basic.model.DoctorReportFields;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;

import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-27 16:19:39
 * Created by [ your name ]
 */
public interface DoctorReportFieldsReadService {

    /**
     * 查询
     *
     * @param id
     * @return doctorReportFields
     */
    Response<DoctorReportFields> findById(Long id);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorReportFields>
     */
    Response<Paging<DoctorReportFields>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorReportFields>
     */
    Response<List<DoctorReportFields>> list(Map<String, Object> criteria);


    Response<List<DoctorReportFieldDto>> listAll();
}